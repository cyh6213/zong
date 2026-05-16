package com.zong.agent.core.workflow.impl;

import com.zong.agent.core.workflow.Node;
import com.zong.agent.core.workflow.NodeExecutor;
import com.zong.agent.core.workflow.NodeTypeEnum;
import com.zong.agent.core.workflow.VariablePool;
import com.zong.agent.llm.StreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 开始节点执行器
 */
public class StartNodeExecutor implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(StartNodeExecutor.class);

    @Override
    public Object execute(Node node, VariablePool pool, StreamListener listener) {
        log.info("执行开始节点: {}", node.getId());

        // 发送开始事件
        if (listener != null) {
            listener.onThinking("开始执行工作流: " + node.getName());
            listener.onContent("▶ 工作流开始\n");
        }

        // 开始节点直接返回
        return "started";
    }

    @Override
    public NodeTypeEnum getSupportedType() {
        return NodeTypeEnum.START;
    }
}
