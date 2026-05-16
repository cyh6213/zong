package com.zong.agent.core.workflow;

import java.util.Map;

/**
 * 工作流执行结果
 */
public class WorkflowResult {

    private boolean success;
    private VariablePool variablePool;
    private Map<String, NodeExecutionResult> nodeResults;
    private long executionTimeMs;
    private String errorMessage;

    public WorkflowResult() {
    }

    public WorkflowResult(boolean success, VariablePool variablePool, 
                          Map<String, NodeExecutionResult> nodeResults, 
                          long executionTimeMs, String errorMessage) {
        this.success = success;
        this.variablePool = variablePool;
        this.nodeResults = nodeResults;
        this.executionTimeMs = executionTimeMs;
        this.errorMessage = errorMessage;
    }

    public static WorkflowResult success(VariablePool pool, 
                                          Map<String, NodeExecutionResult> results,
                                          long executionTimeMs) {
        return new WorkflowResult(true, pool, results, executionTimeMs, null);
    }

    public static WorkflowResult failure(String errorMessage, long executionTimeMs) {
        return new WorkflowResult(false, null, null, executionTimeMs, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public VariablePool getVariablePool() {
        return variablePool;
    }

    public void setVariablePool(VariablePool variablePool) {
        this.variablePool = variablePool;
    }

    public Map<String, NodeExecutionResult> getNodeResults() {
        return nodeResults;
    }

    public void setNodeResults(Map<String, NodeExecutionResult> nodeResults) {
        this.nodeResults = nodeResults;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 获取最终结果（最后一个 LLM 节点的结果）
     */
    public Object getFinalResult() {
        if (variablePool == null || nodeResults == null) {
            return null;
        }

        // 返回最后一个成功的 LLM 节点结果
        NodeExecutionResult lastLlmResult = null;
        for (Map.Entry<String, NodeExecutionResult> entry : nodeResults.entrySet()) {
            NodeExecutionResult result = entry.getValue();
            if (result.isSuccess() && result.getResult() != null) {
                lastLlmResult = result;
            }
        }

        return lastLlmResult != null ? lastLlmResult.getResult() : null;
    }
}
