package com.zong.agent.dto;

import lombok.Data;

/**
 * Agent 对话请求 DTO。
 */
@Data
public class AgentChatRequest {

    /**
     * 会话 ID（可选，用于多轮对话）。
     */
    private String sessionId;

    /**
     * 用户消息。
     */
    private String message;

    /**
     * Agent 模式：react（默认）、plan（Plan-and-Execute）、multi（Multi-Agent）。
     */
    private String mode = "react";

    /**
     * 前端传入的 DAG JSON（可选，用于执行预定义工作流）。
     */
    private String workflowJson;

    /**
     * 组织标签（可选）。
     */
    private String orgTag;

    /**
     * 是否启用 RAG 检索。
     */
    private Boolean enableRag = true;

    /**
     * 最大思考轮次。
     */
    private Integer maxTurns = 10;
}
