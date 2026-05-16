package com.zong.agent.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * SSE 流式输出监听器实现。
 * <p>
 * 将 LLM 输出转换为 SSE 事件，推送给前端。
 */
@Slf4j
public class SseStreamListener implements StreamListener {

    public static final String EVENT_THINKING = "thinking_delta";
    public static final String EVENT_CONTENT = "content_delta";
    public static final String EVENT_WORKFLOW = "workflow_json";
    public static final String EVENT_DONE = "done";
    public static final String EVENT_ERROR = "error";

    private final SseEmitter emitter;
    private final CompletableFuture<Void> completionFuture;

    public SseStreamListener(SseEmitter emitter) {
        this.emitter = emitter;
        this.completionFuture = new CompletableFuture<>();

        // 设置超时
        emitter.onTimeout(() -> {
            log.info("SSE 连接超时");
            completionFuture.complete(null);
        });

        emitter.onCompletion(() -> {
            log.info("SSE 连接完成");
            completionFuture.complete(null);
        });

        emitter.onError(e -> {
            log.error("SSE 连接错误: {}", e.getMessage());
            completionFuture.completeExceptionally(e);
        });
    }

    @Override
    public void onThinking(String delta) {
        sendEvent(EVENT_THINKING, delta);
    }

    @Override
    public void onContent(String delta) {
        sendEvent(EVENT_CONTENT, delta);
    }

    @Override
    public void onWorkflowJson(String workflowJson) {
        sendEvent(EVENT_WORKFLOW, workflowJson);
    }

    @Override
    public void onDone() {
        try {
            sendEvent(EVENT_DONE, "completed");
            emitter.complete();
        } catch (IOException e) {
            log.error("完成 SSE 失败: {}", e.getMessage());
        }
        completionFuture.complete(null);
    }

    @Override
    public void onError(String error) {
        try {
            sendEvent(EVENT_ERROR, error);
            emitter.completeWithError(new RuntimeException(error));
        } catch (IOException e) {
            log.error("发送错误失败: {}", e.getMessage());
        }
        completionFuture.completeExceptionally(new RuntimeException(error));
    }

    /**
     * 发送 SSE 事件。
     */
    private void sendEvent(String eventName, String data) {
        try {
            emitter.send(ServerSentEvent.builder()
                    .event(eventName)
                    .data(data)
                    .build());
        } catch (IOException e) {
            log.error("发送 SSE 事件失败: event={}, error={}", eventName, e.getMessage());
            emitter.completeWithError(e);
        }
    }

    /**
     * 获取完成 Future。
     */
    public CompletableFuture<Void> getCompletionFuture() {
        return completionFuture;
    }

    /**
     * 创建 SSE 通用的发送器工厂。
     */
    public static SseEmitter createEmitter(long timeout) {
        return new SseEmitter(timeout);
    }
}
