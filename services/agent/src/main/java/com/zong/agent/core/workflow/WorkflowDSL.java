package com.zong.agent.core.workflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流定义（WorkflowDSL）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowDSL {

    private String id;
    private String name;
    private String description;
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    // 元数据
    private String version;
    private Long createTime;
    private Long updateTime;

    public WorkflowDSL() {
    }

    public WorkflowDSL(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges != null ? edges : new ArrayList<>();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 根据 ID 查找节点
     */
    public Node findNodeById(String nodeId) {
        return nodes.stream()
                .filter(n -> n.getId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取节点的出边
     */
    public List<Edge> getOutgoingEdges(String nodeId) {
        return edges.stream()
                .filter(e -> e.getSource().equals(nodeId))
                .toList();
    }

    /**
     * 获取节点的入边
     */
    public List<Edge> getIncomingEdges(String nodeId) {
        return edges.stream()
                .filter(e -> e.getTarget().equals(nodeId))
                .toList();
    }

    /**
     * 添加节点
     */
    public void addNode(Node node) {
        this.nodes.add(node);
    }

    /**
     * 添加边
     */
    public void addEdge(Edge edge) {
        this.edges.add(edge);
    }
}
