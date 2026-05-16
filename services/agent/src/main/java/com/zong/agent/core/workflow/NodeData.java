package com.zong.agent.core.workflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * 节点数据（用于 JSON 反序列化）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeData {

    // LLM 节点配置
    private String model;
    private String prompt;
    private List<String> tools;
    private Integer maxTokens;
    private Double temperature;

    // 插件节点配置
    private String pluginName;
    private Map<String, Object> params;

    // 条件节点配置
    private String conditionExpression;
    private String trueBranch;
    private String falseBranch;

    // 循环节点配置
    private Integer maxLoopCount;
    private String loopCondition;
    private String loopBody;

    // 通用配置
    private Map<String, Object> config;
    private String description;

    public NodeData() {
    }

    // LLM 节点
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public List<String> getTools() {
        return tools;
    }

    public void setTools(List<String> tools) {
        this.tools = tools;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    // 插件节点
    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    // 条件节点
    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public String getTrueBranch() {
        return trueBranch;
    }

    public void setTrueBranch(String trueBranch) {
        this.trueBranch = trueBranch;
    }

    public String getFalseBranch() {
        return falseBranch;
    }

    public void setFalseBranch(String falseBranch) {
        this.falseBranch = falseBranch;
    }

    // 循环节点
    public Integer getMaxLoopCount() {
        return maxLoopCount;
    }

    public void setMaxLoopCount(Integer maxLoopCount) {
        this.maxLoopCount = maxLoopCount;
    }

    public String getLoopCondition() {
        return loopCondition;
    }

    public void setLoopCondition(String loopCondition) {
        this.loopCondition = loopCondition;
    }

    public String getLoopBody() {
        return loopBody;
    }

    public void setLoopBody(String loopBody) {
        this.loopBody = loopBody;
    }

    // 通用
    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
