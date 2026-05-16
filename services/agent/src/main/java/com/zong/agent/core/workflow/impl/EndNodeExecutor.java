package com.zong.agent.core.workflow.impl;

import com.zong.agent.core.workflow.Node;
import com.zong.agent.core.workflow.NodeExecutor;
import com.zong.agent.core.workflow.NodeTypeEnum;
import com.zong.agent.core.workflow.VariablePool;
import com.zong.agent.llm.StreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 结束节点执行器
 */
public class EndNodeExecutor implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(EndNodeExecutor.class);

    @Override
    public Object execute(Node node, VariablePool pool, StreamListener listener) {
        log.info("执行结束节点: {}", node.getId());

        // 发送结束事件
        if (listener != null) {
            listener.onContent("■ 工作流结束\n");
            listener.onDone();
        }

        return "completed";
    }

    @Override
    public NodeTypeEnum getSupportedType() {
        return NodeTypeEnum.END;
    }
}
