package com.zong.agent.core.workflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 边（连接）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Edge {

    private String id;
    private String source;
    private String target;
    private String sourceHandle;  // 用于区分同一节点的多个出口（如条件节点的真/假分支）
    private String targetHandle;
    private EdgeType type;

    public Edge() {
    }

    public Edge(String source, String target) {
        this.source = source;
        this.target = target;
        this.type = EdgeType.DEFAULT;
    }

    public Edge(String id, String source, String target) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.type = EdgeType.DEFAULT;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSourceHandle() {
        return sourceHandle;
    }

    public void setSourceHandle(String sourceHandle) {
        this.sourceHandle = sourceHandle;
    }

    public String getTargetHandle() {
        return targetHandle;
    }

    public void setTargetHandle(String targetHandle) {
        this.targetHandle = targetHandle;
    }

    public EdgeType getType() {
        return type;
    }

    public void setType(EdgeType type) {
        this.type = type;
    }

    /**
     * 边类型
     */
    public enum EdgeType {
        DEFAULT,
        TRUE_BRANCH,   // 条件为真
        FALSE_BRANCH,  // 条件为假
        LOOP_BODY,     // 循环体
        LOOP_EXIT      // 循环退出
    }
}
