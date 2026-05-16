package com.zong.agent.core.workflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工作流引擎测试
 */
class WorkflowTest {

    @Test
    void testTopologicalSort() {
        // 构建简单线性工作流: start -> llm -> end
        WorkflowDSL workflow = new WorkflowDSL();
        workflow.setId("test-workflow");
        workflow.setName("测试工作流");

        Node start = new Node("start", "start", "开始");
        Node llm = new Node("llm", "llm", "LLM节点");
        Node end = new Node("end", "end", "结束");

        workflow.addNode(start);
        workflow.addNode(llm);
        workflow.addNode(end);

        workflow.addEdge(new Edge("start", "llm"));
        workflow.addEdge(new Edge("llm", "end"));

        // 验证节点顺序
        assertEquals(3, workflow.getNodes().size());
        assertEquals(2, workflow.getEdges().size());

        // 验证入边出边
        assertEquals(1, workflow.getIncomingEdges("llm").size());
        assertEquals(1, workflow.getOutgoingEdges("llm").size());
    }

    @Test
    void testFindNodeById() {
        WorkflowDSL workflow = DagToJsonConverter.buildLinearWorkflow("开始", "处理", "结束");

        Node node = workflow.findNodeById("node-1");
        assertNotNull(node);
        assertEquals("处理", node.getName());
    }

    @Test
    void testDagToJson() {
        WorkflowDSL workflow = DagToJsonConverter.buildLinearWorkflow("开始", "处理", "结束");

        DagToJsonConverter converter = new DagToJsonConverter();
        String json = converter.toJson(workflow);

        assertNotNull(json);
        assertTrue(json.contains("workflow-"));
        assertTrue(json.contains("node-0"));
        assertTrue(json.contains("node-1"));
        assertTrue(json.contains("node-2"));

        System.out.println("生成的 JSON:");
        System.out.println(json);
    }

    @Test
    void testConditionalWorkflow() {
        WorkflowDSL workflow = DagToJsonConverter.buildConditionalWorkflow(
                "判断是否大于5",
                "大于5分支",
                "小于等于5分支"
        );

        assertEquals(6, workflow.getNodes().size());
        assertTrue(workflow.getNodes().stream()
                .anyMatch(n -> "condition-1".equals(n.getId())));
    }

    @Test
    void testJsonParsing() {
        String json = """
            {
              "id": "test-123",
              "name": "测试工作流",
              "nodes": [
                {
                  "id": "start",
                  "type": "start",
                  "name": "开始"
                },
                {
                  "id": "llm-1",
                  "type": "llm",
                  "name": "LLM节点",
                  "data": {
                    "model": "qwen-plus",
                    "prompt": "你好"
                  }
                },
                {
                  "id": "end",
                  "type": "end",
                  "name": "结束"
                }
              ],
              "edges": [
                {"source": "start", "target": "llm-1"},
                {"source": "llm-1", "target": "end"}
              ]
            }
            """;

        DagToJsonConverter converter = new DagToJsonConverter();
        WorkflowDSL workflow = converter.fromJson(json);

        assertNotNull(workflow);
        assertEquals("test-123", workflow.getId());
        assertEquals("测试工作流", workflow.getName());
        assertEquals(3, workflow.getNodes().size());
        assertEquals(2, workflow.getEdges().size());

        // 验证节点数据
        Node llmNode = workflow.findNodeById("llm-1");
        assertNotNull(llmNode);
        assertNotNull(llmNode.getData());
        assertEquals("qwen-plus", llmNode.getData().getModel());
    }

    @Test
    void testExecutorRegistry() {
        WorkflowEngine engine = new WorkflowEngine();

        assertNotNull(engine.getExecutor(NodeTypeEnum.START));
        assertNotNull(engine.getExecutor(NodeTypeEnum.END));
        assertNotNull(engine.getExecutor(NodeTypeEnum.LLM));
        assertNotNull(engine.getExecutor(NodeTypeEnum.CONDITION));
        assertNull(engine.getExecutor(NodeTypeEnum.LOOP)); // 需要手动注册
    }
}
