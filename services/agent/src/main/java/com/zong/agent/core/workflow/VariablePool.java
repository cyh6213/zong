package com.zong.agent.core.workflow;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 变量池 - 存储节点间共享的变量
 */
public class VariablePool {

    private final Map<String, Object> variables = new HashMap<>();

    /**
     * 存储变量
     */
    public void put(String key, Object value) {
        variables.put(key, value);
    }

    /**
     * 获取变量
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) variables.get(key);
    }

    /**
     * 获取变量（带默认值）
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        Object value = variables.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * 检查变量是否存在
     */
    public boolean contains(String key) {
        return variables.containsKey(key);
    }

    /**
     * 获取所有变量
     */
    public Map<String, Object> getAll() {
        return new HashMap<>(variables);
    }

    /**
     * 清空变量池
     */
    public void clear() {
        variables.clear();
    }

    /**
     * 解析变量引用 {{nodeId.field}} 或 {{nodeId}}
     */
    public String resolve(String template) {
        if (template == null) {
            return null;
        }

        String result = template;
        // 简单替换 {{variableName}} 格式
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    /**
     * 获取所有变量名
     */
    public Set<String> getVariableNames() {
        return variables.keySet();
    }

    @Override
    public String toString() {
        return "VariablePool{" +
                "variables=" + variables +
                '}';
    }
}
