package com.zong.agent.core.workflow.impl;

import com.zong.agent.core.workflow.Node;
import com.zong.agent.core.workflow.NodeData;
import com.zong.agent.core.workflow.NodeExecutor;
import com.zong.agent.core.workflow.NodeTypeEnum;
import com.zong.agent.core.workflow.VariablePool;
import com.zong.agent.llm.StreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 条件节点执行器
 */
public class ConditionNodeExecutor implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(ConditionNodeExecutor.class);

    @Override
    public Object execute(Node node, VariablePool pool, StreamListener listener) {
        log.info("执行条件节点: {}", node.getId());

        NodeData data = node.getData();
        if (data == null) {
            return handleError(node, "节点数据为空", listener);
        }

        String conditionExpr = data.getConditionExpression();
        if (conditionExpr == null || conditionExpr.isEmpty()) {
            return handleError(node, "条件表达式为空", listener);
        }

        if (listener != null) {
            listener.onThinking("评估条件: " + conditionExpr);
        }

        try {
            // 解析条件表达式
            String resolvedExpr = pool.resolve(conditionExpr);
            boolean conditionResult = evaluateCondition(resolvedExpr, pool);

            String result = conditionResult ? "TRUE" : "FALSE";
            pool.put(node.getId(), result);

            if (listener != null) {
                listener.onContent("❓ 条件 [" + conditionExpr + "] = " + result + "\n");
            }

            return result;
        } catch (Exception e) {
            log.error("条件节点执行失败: {}", e.getMessage(), e);
            return handleError(node, e.getMessage(), listener);
        }
    }

    /**
     * 评估条件表达式
     * 
     * 支持格式：
     * - {{nodeId}} == true/false
     * - {{nodeId}} > < >= <= value
     * - {{nodeId}} contains "string"
     */
    private boolean evaluateCondition(String expression, VariablePool pool) {
        // 简单的条件评估
        // TODO: 使用 SpEL 或其他表达式引擎

        expression = expression.trim();

        // 处理 == 比较
        if (expression.contains("==")) {
            String[] parts = expression.split("==");
            if (parts.length == 2) {
                String left = parts[0].trim();
                String right = parts[1].trim();
                // 解析变量
                Object leftVal = resolveValue(left, pool);
                // 比较
                return String.valueOf(leftVal).equals(right.replace("\"", "").trim());
            }
        }

        // 处理 > < >= <= 比较
        for (String op : new String[]{" > ", " < ", " >= ", " <= "}) {
            if (expression.contains(op)) {
                String[] parts = expression.split(op);
                if (parts.length == 2) {
                    String left = parts[0].trim();
                    String right = parts[1].trim();
                    Object leftVal = resolveValue(left, pool);
                    try {
                        double numVal = Double.parseDouble(String.valueOf(leftVal));
                        double compareVal = Double.parseDouble(right.trim());
                        if (op.contains(">=")) return numVal >= compareVal;
                        if (op.contains("<=")) return numVal <= compareVal;
                        if (op.contains(">")) return numVal > compareVal;
                        if (op.contains("<")) return numVal < compareVal;
                    } catch (NumberFormatException e) {
                        // 不是数字，比较字符串
                    }
                }
            }
        }

        // 处理 contains
        if (expression.contains("contains")) {
            String[] parts = expression.split("contains");
            if (parts.length == 2) {
                String left = parts[0].trim();
                String right = parts[1].trim().replace("\"", "");
                Object leftVal = resolveValue(left, pool);
                return String.valueOf(leftVal).contains(right);
            }
        }

        // 默认返回 true
        return true;
    }

    private Object resolveValue(String variableName, VariablePool pool) {
        // 移除 {{ }}
        String name = variableName.replace("{{", "").replace("}}", "").trim();
        return pool.get(name);
    }

    private String handleError(Node node, String error, StreamListener listener) {
        String errorResult = "ERROR: " + error;
        if (listener != null) {
            listener.onError(error);
        }
        return errorResult;
    }

    @Override
    public NodeTypeEnum getSupportedType() {
        return NodeTypeEnum.CONDITION;
    }
}
