package com.zong.agent.dto;

import lombok.Data;

/**
 * 工作流执行请求 DTO。
 */
@Data
public class WorkflowExecuteRequest {

    /**
     * 执行 ID（可选，用于追踪）。
     */
    private String executionId;

    /**
     * DAG 工作流 JSON。
     */
    private String workflowJson;

    /**
     * 初始输入参数。
     */
    private Object input;

    /**
     * 组织标签。
     */
    private String orgTag;

    /**
     * 是否异步执行。
     */
    private Boolean async = false;
}
