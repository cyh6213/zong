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
 * LLM 节点执行器
 */
public class LlmNodeExecutor implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(LlmNodeExecutor.class);

    // TODO: 注入 LlmClient
    // private final LlmClient llmClient;

    public LlmNodeExecutor() {
    }

    @Override
    public Object execute(Node node, VariablePool pool, StreamListener listener) {
        log.info("执行 LLM 节点: {}", node.getId());

        NodeData data = node.getData();
        if (data == null) {
            return handleError(node, "节点数据为空", listener);
        }

        // 解析 prompt（替换变量）
        String prompt = pool.resolve(data.getPrompt());
        String model = data.getModel() != null ? data.getModel() : "qwen-plus";

        if (listener != null) {
            listener.onThinking("执行 LLM 节点: " + node.getName() + " (model: " + model + ")");
        }

        try {
            // TODO: 实际调用 LLM
            // String result = llmClient.chat(prompt, model, data.getTools(), listener);

            // 模拟执行
            String result = simulateLlmCall(node.getName(), prompt, listener);

            // 存储结果到变量池
            pool.put(node.getId(), result);

            if (listener != null) {
                listener.onContent("[" + node.getName() + "] " + result + "\n");
            }

            return result;
        } catch (Exception e) {
            log.error("LLM 节点执行失败: {}", e.getMessage(), e);
            return handleError(node, e.getMessage(), listener);
        }
    }

    private String simulateLlmCall(String nodeName, String prompt, StreamListener listener) {
        // TODO: 替换为实际 LLM 调用
        // 这里模拟 LLM 返回
        return "LLM 响应 (prompt: " + prompt.substring(0, Math.min(50, prompt.length())) + "...)";
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
        return NodeTypeEnum.LLM;
    }

    @Override
    public boolean validate(Node node, VariablePool pool) {
        if (node == null || node.getType() == null) {
            return false;
        }
        NodeData data = node.getData();
        return data != null && data.getPrompt() != null;
    }
}
