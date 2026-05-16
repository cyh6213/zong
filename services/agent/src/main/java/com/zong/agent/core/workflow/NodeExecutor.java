package com.zong.agent.core.workflow;

import com.zong.agent.llm.StreamListener;

/**
 * 节点执行器接口
 */
public interface NodeExecutor {

    /**
     * 执行节点
     *
     * @param node     节点
     * @param pool     变量池
     * @param listener 流式监听器
     * @return 节点执行结果
     */
    Object execute(Node node, VariablePool pool, StreamListener listener);

    /**
     * 获取支持的节点类型
     */
    NodeTypeEnum getSupportedType();

    /**
     * 执行前校验
     */
    default boolean validate(Node node, VariablePool pool) {
        return node != null && node.getType() != null;
    }
}
