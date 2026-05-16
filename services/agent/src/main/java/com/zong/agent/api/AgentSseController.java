package com.zong.agent.api;

import com.zong.agent.dto.AgentChatRequest;
import com.zong.agent.llm.SseStreamListener;
import com.zong.agent.llm.StreamListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Agent SSE 流式输出控制器。
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
public class AgentSseController {

    /**
     * SSE 默认超时时间（30分钟）。
     */
    private static final long DEFAULT_TIMEOUT = 30 * 60 * 1000L;

    @Autowired(required = false)
    private StreamListener streamListener;

    /**
     * 流式对话（SSE）。
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AgentChatRequest request) {
        String sessionId = request.getSessionId() != null
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        log.info("收到流式对话请求: sessionId={}, mode={}", sessionId, request.getMode());

        // 创建 SSE 发射器
        SseEmitter emitter = SseStreamListener.createEmitter(DEFAULT_TIMEOUT);
        StreamListener listener = new SseStreamListener(emitter);

        // 异步执行 Agent
        CompletableFuture.runAsync(() -> {
            try {
                // TODO: 调用 Agent 执行，传入 listener
                // 目前先发送占位数据
                executeAgent(sessionId, request, listener);
            } catch (Exception e) {
                log.error("Agent 执行失败: {}", e.getMessage(), e);
                listener.onError("Agent 执行失败: " + e.getMessage());
            }
        });

        return emitter;
    }

    /**
     * 执行 Agent 逻辑（临时实现，后续替换）。
     */
    private void executeAgent(String sessionId, AgentChatRequest request, StreamListener listener) {
        try {
            // 发送思考内容
            listener.onThinking("正在分析您的问题...");

            // 模拟处理
            Thread.sleep(500);

            // TODO: 调用实际 Agent
            // 这里等待 Agent 核心实现后替换

            // 发送内容
            listener.onContent("Agent 核心逻辑待实现。请稍后...");

            // 模拟工作流 JSON
            // String workflowJson = generateWorkflowJson(request);
            // listener.onWorkflowJson(workflowJson);

            listener.onDone();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            listener.onError("执行被中断");
        }
    }

    /**
     * 健康检查（SSE 格式）。
     */
    @GetMapping(value = "/health/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter healthStream() {
        SseEmitter emitter = new SseEmitter();

        CompletableFuture.runAsync(() -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("health")
                        .data("{\"status\":\"UP\",\"service\":\"agent\"}"));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
