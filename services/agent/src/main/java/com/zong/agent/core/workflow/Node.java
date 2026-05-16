package com.zong.agent.core.workflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 节点
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Node {

    private String id;
    private String type;
    private String name;

    // 位置信息（用于前端展示）
    private Position position;

    // 节点数据
    private NodeData data;

    public Node() {
    }

    public Node(String id, String type, String name) {
        this.id = id;
        this.type = type;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public NodeData getData() {
        return data;
    }

    public void setData(NodeData data) {
        this.data = data;
    }

    /**
     * 获取节点类型枚举
     */
    public NodeTypeEnum getNodeType() {
        return NodeTypeEnum.fromType(type);
    }

    /**
     * 位置信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Position {
        private double x;
        private double y;

        public Position() {
        }

        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }
}
