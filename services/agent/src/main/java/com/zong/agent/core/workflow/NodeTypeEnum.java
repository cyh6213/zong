package com.zong.agent.core.workflow;

/**
 * 节点类型枚举
 */
public enum NodeTypeEnum {
    START("start", "开始节点"),
    LLM("llm", "LLM 节点"),
    PLUGIN("plugin", "插件节点"),
    CONDITION("condition", "条件节点"),
    LOOP("loop", "循环节点"),
    END("end", "结束节点");

    private final String type;
    private final String description;

    NodeTypeEnum(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public static NodeTypeEnum fromType(String type) {
        for (NodeTypeEnum enumType : values()) {
            if (enumType.type.equalsIgnoreCase(type)) {
                return enumType;
            }
        }
        throw new IllegalArgumentException("Unknown node type: " + type);
    }
}
