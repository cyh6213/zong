package com.zong.agent.core.workflow.impl;

import com.zong.agent.core.workflow.Node;
import com.zong.agent.core.workflow.NodeData;
import com.zong.agent.core.workflow.NodeExecutor;
import com.zong.agent.core.workflow.NodeTypeEnum;
import com.zong.agent.core.workflow.VariablePool;
import com.zong.agent.llm.StreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 插件节点执行器
 */
public class PluginNodeExecutor implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(PluginNodeExecutor.class);

    // TODO: 注入 PluginManager
    // private final PluginManager pluginManager;

    public PluginNodeExecutor() {
    }

    @Override
    public Object execute(Node node, VariablePool pool, StreamListener listener) {
        log.info("执行插件节点: {}", node.getId());

        NodeData data = node.getData();
        if (data == null) {
            return handleError(node, "节点数据为空", listener);
        }

        String pluginName = data.getPluginName();
        if (pluginName == null || pluginName.isEmpty()) {
            return handleError(node, "插件名称为空", listener);
        }

        if (listener != null) {
            listener.onThinking("执行插件: " + pluginName);
            listener.onContent("🔧 执行插件: " + pluginName + "\n");
        }

        try {
            // 解析参数中的变量引用
            Map<String, Object> params = resolveParams(data.getParams(), pool);

            // TODO: 实际调用插件
            // Object result = pluginManager.execute(pluginName, params);

            // 模拟执行
            Object result = simulatePluginCall(node.getName(), pluginName, params);

            // 存储结果
            pool.put(node.getId(), result);

            if (listener != null) {
                listener.onContent("插件 [" + pluginName + "] 执行完成\n");
            }

            return result;
        } catch (Exception e) {
            log.error("插件节点执行失败: {}", e.getMessage(), e);
            return handleError(node, e.getMessage(), listener);
        }
    }

    /**
     * 解析参数中的变量引用
     */
    private Map<String, Object> resolveParams(Map<String, Object> params, VariablePool pool) {
        if (params == null) {
            return null;
        }

        // TODO: 实现变量解析
        // 这里简单返回原参数
        return params;
    }

    private Object simulatePluginCall(String nodeName, String pluginName, Map<String, Object> params) {
        // TODO: 替换为实际插件调用
        return "Plugin [" + pluginName + "] result";
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
        return NodeTypeEnum.PLUGIN;
    }

    @Override
    public boolean validate(Node node, VariablePool pool) {
        if (node == null || node.getType() == null) {
            return false;
        }
        NodeData data = node.getData();
        return data != null && data.getPluginName() != null;
    }
}
