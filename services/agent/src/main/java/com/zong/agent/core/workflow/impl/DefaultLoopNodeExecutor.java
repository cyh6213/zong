package com.zong.agent.core.workflow.impl;

import com.zong.agent.core.workflow.Node;
import com.zong.agent.core.workflow.NodeData;
import com.zong.agent.core.workflow.NodeExecutor;
import com.zong.agent.core.workflow.NodeTypeEnum;
import com.zong.agent.core.workflow.VariablePool;
import com.zong.agent.llm.StreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 循环节点执行器
 * 
 * 循环节点包含一个子节点（循环体），会重复执行该子节点直到满足退出条件
 */
public class DefaultLoopNodeExecutor implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(DefaultLoopNodeExecutor.class);

    private final NodeExecutor bodyExecutor;

    public DefaultLoopNodeExecutor(NodeExecutor bodyExecutor) {
        this.bodyExecutor = bodyExecutor;
    }

    @Override
    public Object execute(Node node, VariablePool pool, StreamListener listener) {
        log.info("执行循环节点: {}", node.getId());

        NodeData data = node.getData();
        if (data == null) {
            return handleError(node, "节点数据为空", listener);
        }

        int maxLoopCount = data.getMaxLoopCount() != null ? data.getMaxLoopCount() : 10;
        String loopCondition = data.getLoopCondition();

        if (listener != null) {
            listener.onThinking("开始循环执行 (最大次数: " + maxLoopCount + ")");
        }

        try {
            int loopCount = 0;
            Object lastResult = null;

            while (loopCount < maxLoopCount) {
                loopCount++;

                if (listener != null) {
                    listener.onContent("🔄 第 " + loopCount + " 次循环\n");
                }

                // 执行循环体
                lastResult = bodyExecutor.execute(node, pool, listener);

                // 检查循环退出条件
                if (loopCondition != null && !loopCondition.isEmpty()) {
                    String resolvedCondition = pool.resolve(loopCondition);
                    if (!evaluateLoopCondition(resolvedCondition, pool)) {
                        if (listener != null) {
                            listener.onContent("🔄 循环退出 (条件不满足)\n");
                        }
                        break;
                    }
                }
            }

            if (loopCount >= maxLoopCount) {
                if (listener != null) {
                    listener.onContent("🔄 循环退出 (达到最大次数 " + maxLoopCount + ")\n");
                }
            }

            // 存储结果
            pool.put(node.getId() + ".loopCount", loopCount);
            pool.put(node.getId(), lastResult);

            return lastResult;
        } catch (Exception e) {
            log.error("循环节点执行失败: {}", e.getMessage(), e);
            return handleError(node, e.getMessage(), listener);
        }
    }

    private boolean evaluateLoopCondition(String condition, VariablePool pool) {
        // 类似条件节点的条件评估
        if (condition == null) {
            return false;
        }
        // 简单实现：condition 为 false 或 exit 时退出
        return !condition.equalsIgnoreCase("false") && !condition.equalsIgnoreCase("exit");
    }

    private String handleError(Node node, String error, StreamListener listener) {
        String errorResult = "ERROR: " + error;
        if (listener != null) {
            listener.onError(error);
        }
        return errorResult;
    }

    @Override
    public NodeTypeEnum getSupportedType() {
        return NodeTypeEnum.LOOP;
    }
}
