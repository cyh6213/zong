package com.zong.agent.api;

import com.zong.agent.dto.AgentChatRequest;
import com.zong.agent.dto.AgentChatResponse;
import com.zong.agent.dto.WorkflowExecuteRequest;
import com.zong.agent.dto.WorkflowStatusResponse;
import com.zong.agent.runtime.api.RuntimeThreadStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Agent REST API 控制器。
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Autowired(required = false)
    private RuntimeThreadStore runtimeThreadStore;

    /**
     * 非流式对话。
     */
    @PostMapping("/chat")
    public ResponseEntity<AgentChatResponse> chat(@RequestBody AgentChatRequest request) {
        log.info("收到对话请求: sessionId={}, mode={}", request.getSessionId(), request.getMode());

        // TODO: 调用 Agent 执行
        // 这里先返回占位响应，等 Agent 核心实现后补充
        AgentChatResponse response = new AgentChatResponse();
        response.setSessionId(request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString());
        response.setContent("Agent 核心逻辑待实现");
        response.setStatus("pending");
        response.setMode(request.getMode());

        return ResponseEntity.ok(response);
    }

    /**
     * 执行工作流。
     */
    @PostMapping("/workflow/execute")
    public ResponseEntity<Map<String, Object>> executeWorkflow(@RequestBody WorkflowExecuteRequest request) {
        log.info("收到工作流执行请求: executionId={}", request.getExecutionId());

        String executionId = request.getExecutionId() != null
                ? request.getExecutionId()
                : UUID.randomUUID().toString();

        // TODO: 调用 DAG 执行引擎
        // 这里先返回占位响应
        Map<String, Object> result = new HashMap<>();
        result.put("executionId", executionId);
        result.put("status", "pending");
        result.put("message", "工作流执行待实现");

        return ResponseEntity.ok(result);
    }

    /**
     * 查询工作流执行状态。
     */
    @GetMapping("/workflow/status/{id}")
    public ResponseEntity<WorkflowStatusResponse> getWorkflowStatus(@PathVariable("id") String id) {
        log.info("查询工作流状态: executionId={}", id);

        WorkflowStatusResponse status = new WorkflowStatusResponse();
        status.setExecutionId(id);
        status.setStatus("pending");
        status.setProgress(0);

        // TODO: 从 RuntimeThreadStore 获取实际状态
        if (runtimeThreadStore != null) {
            // 实际实现时查询状态
        }

        return ResponseEntity.ok(status);
    }

    /**
     * 健康检查。
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "agent");
        return ResponseEntity.ok(result);
    }
}
