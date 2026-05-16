package com.zong.agent.core.workflow;

import com.zong.agent.core.workflow.impl.*;
import com.zong.agent.llm.StreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAG 执行引擎
 * 
 * 基于节点依赖的 DAG 执行，支持：
 * - Kahn 算法拓扑排序
 * - 环路检测
 * - 节点重试
 * - 条件分支
 */
public class WorkflowEngine {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

    // 节点执行器注册表
    private final Map<NodeTypeEnum, NodeExecutor> executors = new ConcurrentHashMap<>();

    // 重试配置（可按节点类型配置）
    private final Map<NodeTypeEnum, RetryConfig> retryConfigs = new ConcurrentHashMap<>();

    public WorkflowEngine() {
        // 注册默认执行器
        registerDefaultExecutors();
    }

    private void registerDefaultExecutors() {
        registerExecutor(new StartNodeExecutor());
        registerExecutor(new EndNodeExecutor());
        registerExecutor(new LlmNodeExecutor());
        registerExecutor(new PluginNodeExecutor());
        registerExecutor(new ConditionNodeExecutor());
        // Loop 需要子执行器
        // registerExecutor(new LoopNodeExecutor(subExecutor));
    }

    /**
     * 注册节点执行器
     */
    public void registerExecutor(NodeExecutor executor) {
        executors.put(executor.getSupportedType(), executor);
    }

    /**
     * 设置节点类型对应的重试配置
     */
    public void setRetryConfig(NodeTypeEnum type, RetryConfig config) {
        retryConfigs.put(type, config);
    }

    /**
     * 执行工作流
     */
    public WorkflowResult execute(WorkflowDSL workflow, StreamListener listener) {
        long startTime = System.currentTimeMillis();
        String workflowId = workflow.getId();

        log.info("开始执行工作流: {}", workflowId);

        if (listener != null) {
            listener.onThinking("开始执行工作流: " + workflow.getName());
        }

        try {
            // 1. 拓扑排序（Kahn 算法）
            List<Node> executionOrder = topologicalSort(workflow);
            log.info("拓扑排序完成，执行顺序: {}", executionOrder);

            // 2. 检查环路
            if (hasCycle(workflow)) {
                throw new WorkflowException("工作流存在环路，无法执行");
            }

            // 3. 执行节点
            VariablePool pool = new VariablePool();
            Map<String, NodeExecutionResult> nodeResults = new LinkedHashMap<>();

            for (Node node : executionOrder) {
                NodeExecutionResult result = executeNode(node, workflow, pool, listener);
                nodeResults.put(node.getId(), result);

                // 存储结果到变量池
                if (result.isSuccess()) {
                    pool.put(node.getId(), result.getResult());
                }

                // 如果节点失败，且不是条件节点（条件节点失败可以继续），则停止执行
                if (!result.isSuccess() && node.getNodeType() != NodeTypeEnum.CONDITION) {
                    log.warn("节点 {} 执行失败，停止工作流执行", node.getId());
                    break;
                }

                // 处理条件分支
                if (node.getNodeType() == NodeTypeEnum.CONDITION) {
                    handleConditionalBranching(node, workflow, pool, executionOrder, listener);
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("工作流执行完成: {}, 耗时: {}ms", workflowId, duration);

            return new WorkflowResult(true, pool, nodeResults, duration, null);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("工作流执行失败: {}", e.getMessage(), e);

            if (listener != null) {
                listener.onError(e.getMessage());
                listener.onDone();
            }

            return new WorkflowResult(false, null, null, duration, e.getMessage());
        }
    }

    /**
     * 执行单个节点（带重试）
     */
    private NodeExecutionResult executeNode(Node node, WorkflowDSL workflow, 
                                           VariablePool pool, StreamListener listener) {
        NodeExecutor executor = executors.get(node.getNodeType());
        if (executor == null) {
            return NodeExecutionResult.failure(node.getId(), 
                    "不支持的节点类型: " + node.getType());
        }

        RetryConfig retryConfig = retryConfigs.getOrDefault(node.getNodeType(), RetryConfig.noRetry());

        int attempt = 0;
        Exception lastException = null;

        while (attempt < retryConfig.getMaxAttempts()) {
            attempt++;
            long nodeStartTime = System.currentTimeMillis();

            try {
                Object result = executor.execute(node, pool, listener);

                long executionTime = System.currentTimeMillis() - nodeStartTime;
                return NodeExecutionResult.success(node.getId(), result);

            } catch (Exception e) {
                lastException = e;
                log.warn("节点 {} 第 {} 次执行失败: {}", node.getId(), attempt, e.getMessage());

                if (attempt < retryConfig.getMaxAttempts()) {
                    try {
                        long delay = retryConfig.getNextDelay(attempt);
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        return NodeExecutionResult.failure(node.getId(), 
                lastException != null ? lastException.getMessage() : "未知错误");
    }

    /**
     * Kahn 算法拓扑排序
     */
    private List<Node> topologicalSort(WorkflowDSL workflow) {
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, Node> nodeMap = new HashMap<>();
        List<Node> result = new ArrayList<>();

        // 初始化入度和节点映射
        for (Node node : workflow.getNodes()) {
            inDegree.put(node.getId(), 0);
            nodeMap.put(node.getId(), node);
        }

        // 计算入度
        for (Edge edge : workflow.getEdges()) {
            inDegree.merge(edge.getTarget(), 1, Integer::sum);
        }

        // 入度为 0 的节点队列
        Queue<Node> queue = new LinkedList<>();
        for (Node node : workflow.getNodes()) {
            if (inDegree.get(node.getId()) == 0) {
                queue.offer(node);
            }
        }

        // BFS
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            result.add(current);

            // 更新邻居节点的入度
            for (Edge edge : workflow.getOutgoingEdges(current.getId())) {
                String neighborId = edge.getTarget();
                int newDegree = inDegree.get(neighborId) - 1;
                inDegree.put(neighborId, newDegree);

                if (newDegree == 0) {
                    queue.offer(nodeMap.get(neighborId));
                }
            }
        }

        // 如果结果数量不等于节点数量，说明存在环路
        if (result.size() != workflow.getNodes().size()) {
            throw new WorkflowException("工作流存在环路，无法进行拓扑排序");
        }

        return result;
    }

    /**
     * 检测环路（DFS 方法）
     */
    private boolean hasCycle(WorkflowDSL workflow) {
        Map<String, Boolean> visited = new HashMap<>();
        Map<String, Boolean> recStack = new HashMap<>();

        for (Node node : workflow.getNodes()) {
            if (hasCycleDFS(node.getId(), workflow, visited, recStack)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasCycleDFS(String nodeId, WorkflowDSL workflow, 
                                Map<String, Boolean> visited, Map<String, Boolean> recStack) {
        visited.put(nodeId, true);
        recStack.put(nodeId, true);

        for (Edge edge : workflow.getOutgoingEdges(nodeId)) {
            String neighborId = edge.getTarget();

            Boolean neighborVisited = visited.get(neighborId);
            if (neighborVisited == null || !neighborVisited) {
                if (hasCycleDFS(neighborId, workflow, visited, recStack)) {
                    return true;
                }
            } else if (Boolean.TRUE.equals(recStack.get(neighborId))) {
                return true;
            }
        }

        recStack.put(nodeId, false);
        return false;
    }

    /**
     * 处理条件分支
     */
    private void handleConditionalBranching(Node conditionNode, WorkflowDSL workflow,
                                            VariablePool pool, List<Node> executionOrder,
                                            StreamListener listener) {
        // 条件节点执行后，根据结果确定下一个要执行的节点
        // 由于使用拓扑排序，需要在执行时动态调整

        // 获取条件结果
        Object conditionResult = pool.get(conditionNode.getId());
        boolean isTrue = "TRUE".equals(conditionResult) || Boolean.parseBoolean(String.valueOf(conditionResult));

        // 查找对应的分支边
        String targetBranch = isTrue ? "true" : "false";

        // 在拓扑排序中查找下一个应该执行的节点
        int currentIndex = executionOrder.indexOf(conditionNode);
        for (int i = currentIndex + 1; i < executionOrder.size(); i++) {
            Node nextNode = executionOrder.get(i);
            // 检查是否有边连接到该节点，且边的 sourceHandle 匹配
            for (Edge edge : workflow.getIncomingEdges(nextNode.getId())) {
                if (edge.getSource().equals(conditionNode.getId())) {
                    String sourceHandle = edge.getSourceHandle();
                    if ((isTrue && "true".equals(sourceHandle)) ||
                        (!isTrue && "false".equals(sourceHandle))) {
                        // 找到匹配的分支，直接返回（后续节点会按拓扑排序执行）
                        return;
                    }
                }
            }
        }
    }

    /**
     * 获取节点执行器
     */
    public NodeExecutor getExecutor(NodeTypeEnum type) {
        return executors.get(type);
    }
}
