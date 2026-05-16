package com.zong.agent.core.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAG 图转 JSON 转换器
 * 
 * 将内存中的 DAG 结构转换为标准 JSON 格式
 */
public class DagToJsonConverter {

    private static final Logger log = LoggerFactory.getLogger(DagToJsonConverter.class);

    private final ObjectMapper objectMapper;

    public DagToJsonConverter() {
        this.objectMapper = new ObjectMapper();
        // 格式化输出
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public DagToJsonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 将 WorkflowDSL 转换为 JSON 字符串
     */
    public String toJson(WorkflowDSL workflow) {
        try {
            return objectMapper.writeValueAsString(workflow);
        } catch (JsonProcessingException e) {
            log.error("DAG 转 JSON 失败: {}", e.getMessage());
            throw new RuntimeException("DAG 转 JSON 失败", e);
        }
    }

    /**
     * 将 WorkflowDSL 转换为格式化 JSON 字符串（便于阅读）
     */
    public String toPrettyJson(WorkflowDSL workflow) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(workflow);
        } catch (JsonProcessingException e) {
            log.error("DAG 转格式化 JSON 失败: {}", e.getMessage());
            throw new RuntimeException("DAG 转格式化 JSON 失败", e);
        }
    }

    /**
     * 将 JSON 字符串解析为 WorkflowDSL（正向解析）
     */
    public WorkflowDSL fromJson(String json) {
        try {
            return objectMapper.readValue(json, WorkflowDSL.class);
        } catch (JsonProcessingException e) {
            log.error("JSON 解析为 DAG 失败: {}", e.getMessage());
            throw new RuntimeException("JSON 解析为 DAG 失败", e);
        }
    }

    /**
     * 构建简单的线性工作流（用于测试）
     */
    public static WorkflowDSL buildLinearWorkflow(String... nodeNames) {
        WorkflowDSL workflow = new WorkflowDSL();
        workflow.setId("workflow-" + System.currentTimeMillis());
        workflow.setName("线性工作流");

        String prevNodeId = null;
        for (int i = 0; i < nodeNames.length; i++) {
            // 创建节点
            Node node = new Node();
            node.setId("node-" + i);
            node.setType(i == 0 ? "start" : (i == nodeNames.length - 1 ? "end" : "llm"));
            node.setName(nodeNames[i]);

            // 设置位置（简单线性布局）
            Node.Position position = new Node.Position();
            position.setX(100 + i * 200);
            position.setY(200);
            node.setPosition(position);

            workflow.addNode(node);

            // 创建边
            if (prevNodeId != null) {
                Edge edge = new Edge(prevNodeId, node.getId());
                workflow.addEdge(edge);
            }

            prevNodeId = node.getId();
        }

        return workflow;
    }

    /**
     * 构建带条件的分支工作流
     */
    public static WorkflowDSL buildConditionalWorkflow(String condition, String trueBranch, String falseBranch) {
        WorkflowDSL workflow = new WorkflowDSL();
        workflow.setId("workflow-conditional-" + System.currentTimeMillis());
        workflow.setName("条件分支工作流");

        // 开始节点
        Node startNode = new Node("start", "start", "开始");
        Node.Position pos = new Node.Position(100, 200);
        startNode.setPosition(pos);
        workflow.addNode(startNode);

        // LLM 节点（条件判断）
        Node llmNode = new Node("llm-1", "llm", "条件判断");
        NodeData llmData = new NodeData();
        llmData.setPrompt(condition);
        llmNode.setData(llmData);
        llmNode.setPosition(new Node.Position(300, 200));
        workflow.addNode(llmNode);

        // 条件节点
        Node conditionNode = new Node("condition-1", "condition", "条件判断");
        NodeData conditionData = new NodeData();
        conditionData.setConditionExpression("{{llm-1.result}} == true");
        conditionNode.setData(conditionData);
        conditionNode.setPosition(new Node.Position(500, 200));
        workflow.addNode(conditionNode);

        // 真分支 LLM
        Node trueNode = new Node("llm-true", "llm", trueBranch);
        trueNode.setPosition(new Node.Position(700, 100));
        workflow.addNode(trueNode);

        // 假分支 LLM
        Node falseNode = new Node("llm-false", "llm", falseBranch);
        falseNode.setPosition(new Node.Position(700, 300));
        workflow.addNode(falseNode);

        // 结束节点
        Node endNode = new Node("end", "end", "结束");
        endNode.setPosition(new Node.Position(900, 200));
        workflow.addNode(endNode);

        // 创建边
        workflow.addEdge(new Edge("start", "llm-1"));
        workflow.addEdge(new Edge("llm-1", "condition-1"));

        // 条件边（真分支）
        Edge trueEdge = new Edge("condition-1", "llm-true");
        trueEdge.setSourceHandle("true");
        workflow.addEdge(trueEdge);

        // 条件边（假分支）
        Edge falseEdge = new Edge("condition-1", "llm-false");
        falseEdge.setSourceHandle("false");
        workflow.addEdge(falseEdge);

        // 合并到结束
        workflow.addEdge(new Edge("llm-true", "end"));
        workflow.addEdge(new Edge("llm-false", "end"));

        return workflow;
    }
}
