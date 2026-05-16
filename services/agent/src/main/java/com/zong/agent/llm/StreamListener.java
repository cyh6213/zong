package com.zong.agent.llm;

/**
 * LLM 流式输出监听器接口。
 * <p>
 * 用于捕获 LLM 的流式输出，支持分段推送思考内容和回复内容。
 */
public interface StreamListener {

    /**
     * 发送思考内容片段。
     *
     * @param delta 思考内容片段
     */
    void onThinking(String delta);

    /**
     * 发送回复内容片段。
     *
     * @param delta 回复内容片段
     */
    void onContent(String delta);

    /**
     * 发送工作流 JSON（AI 生成流程后推送）。
     *
     * @param workflowJson 工作流 JSON
     */
    void onWorkflowJson(String workflowJson);

    /**
     * 发送完成信号。
     */
    void onDone();

    /**
     * 发送错误。
     *
     * @param error 错误信息
     */
    void onError(String error);

    /**
     * 空实现。
     */
    static StreamListener noop() {
        return new StreamListener() {
            @Override
            public void onThinking(String delta) {
            }

            @Override
            public void onContent(String delta) {
            }

            @Override
            public void onWorkflowJson(String workflowJson) {
            }

            @Override
            public void onDone() {
            }

            @Override
            public void onError(String error) {
            }
        };
    }
}
