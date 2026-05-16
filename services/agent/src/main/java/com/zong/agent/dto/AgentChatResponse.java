package com.zong.agent.dto;

import lombok.Data;

/**
 * Agent 对话响应 DTO。
 */
@Data
public class AgentChatResponse {

    /**
     * 会话 ID。
     */
    private String sessionId;

    /**
     * 回复内容。
     */
    private String content;

    /**
     * 思考过程。
     */
    private String thinking;

    /**
     * 使用的 Agent 模式。
     */
    private String mode;

    /**
     * 消耗的 token 数量。
     */
    private Integer tokensUsed;

    /**
     * 执行耗时（毫秒）。
     */
    private Long durationMs;

    /**
     * 状态：success、error。
     */
    private String status;

    /**
     * 错误信息（如果有）。
     */
    private String error;

    /**
     * DAG 工作流 JSON（如果有）。
     */
    private String workflowJson;

    public static AgentChatResponse success(String sessionId, String content) {
        AgentChatResponse response = new AgentChatResponse();
        response.setSessionId(sessionId);
        response.setContent(content);
        response.setStatus("success");
        return response;
    }

    public static AgentChatResponse error(String sessionId, String error) {
        AgentChatResponse response = new AgentChatResponse();
        response.setSessionId(sessionId);
        response.setStatus("error");
        response.setError(error);
        return response;
    }
}
