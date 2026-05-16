package com.zong.agent.core.workflow;

/**
 * 节点执行结果
 */
public class NodeExecutionResult {

    private String nodeId;
    private boolean success;
    private Object result;
    private String errorMessage;
    private long executionTimeMs;

    public NodeExecutionResult() {
    }

    public static NodeExecutionResult success(String nodeId, Object result) {
        NodeExecutionResult r = new NodeExecutionResult();
        r.setNodeId(nodeId);
        r.setSuccess(true);
        r.setResult(result);
        return r;
    }

    public static NodeExecutionResult failure(String nodeId, String errorMessage) {
        NodeExecutionResult r = new NodeExecutionResult();
        r.setNodeId(nodeId);
        r.setSuccess(false);
        r.setErrorMessage(errorMessage);
        return r;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
}
