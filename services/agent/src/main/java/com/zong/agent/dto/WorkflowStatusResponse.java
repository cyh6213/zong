package com.zong.agent.dto;

import lombok.Data;

/**
 * 工作流执行状态响应 DTO。
 */
@Data
public class WorkflowStatusResponse {

    /**
     * 执行 ID。
     */
    private String executionId;

    /**
     * 状态：pending、running、completed、failed。
     */
    private String status;

    /**
     * 当前节点。
     */
    private String currentNode;

    /**
     * 执行进度（百分比）。
     */
    private Integer progress;

    /**
     * 执行结果（完成后返回）。
     */
    private Object result;

    /**
     * 错误信息（如果失败）。
     */
    private String error;

    /**
     * 开始时间。
     */
    private Long startTime;

    /**
     * 结束时间。
     */
    private Long endTime;
}
