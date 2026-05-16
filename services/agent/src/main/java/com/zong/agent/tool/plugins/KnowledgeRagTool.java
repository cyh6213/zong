package com.zong.agent.tool.plugins;

import com.zong.agent.dto.KnowledgeRetrieveRequest;
import com.zong.agent.dto.KnowledgeRetrieveResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 知识库 RAG 检索工具。
 * <p>
 * 通过 HTTP API 调用 services/knowledge 的 RAG 检索接口。
 */
@Slf4j
@Component
public class KnowledgeRagTool {

    private static final String TOOL_NAME = "knowledge_retrieve";
    private static final String TOOL_DESCRIPTION = """
            从知识库中检索相关内容。
            输入：检索查询文本
            输出：检索到的知识条目列表（包含标题、内容摘要、相似度等）
            """;

    private final WebClient knowledgeWebClient;

    public KnowledgeRagTool(
            @Qualifier("knowledgeWebClient") WebClient knowledgeWebClient) {
        this.knowledgeWebClient = knowledgeWebClient;
    }

    /**
     * 获取工具名称。
     */
    public String getName() {
        return TOOL_NAME;
    }

    /**
     * 获取工具描述。
     */
    public String getDescription() {
        return TOOL_DESCRIPTION;
    }

    /**
     * 获取工具参数描述（用于 LLM 理解如何调用）。
     */
    public String getParameters() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "query": {
                            "type": "string",
                            "description": "检索查询文本"
                        },
                        "topK": {
                            "type": "integer",
                            "description": "返回结果数量，默认 5",
                            "default": 5
                        },
                        "orgTag": {
                            "type": "string",
                            "description": "组织标签，用于过滤知识库"
                        }
                    },
                    "required": ["query"]
                }
                """;
    }

    /**
     * 同步检索知识库。
     *
     * @param query  检索查询文本
     * @param topK  返回结果数量
     * @param orgTag 组织标签（可选）
     * @return 检索结果列表
     */
    public List<KnowledgeRetrieveResponse.KnowledgeResult> retrieve(
            String query, Integer topK, String orgTag) {

        log.info("检索知识库: query={}, topK={}, orgTag={}", query, topK, orgTag);

        try {
            KnowledgeRetrieveRequest request = new KnowledgeRetrieveRequest(query, topK, orgTag);

            KnowledgeRetrieveResponse response = knowledgeWebClient
                    .post()
                    .uri("/api/knowledge/retrieve")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(KnowledgeRetrieveResponse.class)
                    .block();

            if (response != null && response.isSuccess()) {
                List<KnowledgeRetrieveResponse.KnowledgeResult> results =
                        response.getData() != null ? response.getData().getResults() : null;
                log.info("检索成功，返回 {} 条结果", results != null ? results.size() : 0);
                return results;
            } else {
                log.warn("检索失败: code={}, message={}",
                        response != null ? response.getCode() : null,
                        response != null ? response.getMessage() : null);
                return List.of();
            }
        } catch (WebClientResponseException e) {
            log.error("HTTP 错误: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            log.error("检索异常: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 同步检索知识库（使用默认 topK=5）。
     */
    public List<KnowledgeRetrieveResponse.KnowledgeResult> retrieve(String query) {
        return retrieve(query, 5, null);
    }

    /**
     * 异步检索知识库。
     *
     * @param query  检索查询文本
     * @param topK  返回结果数量
     * @param orgTag 组织标签（可选）
     * @return 异步结果
     */
    public CompletableFuture<List<KnowledgeRetrieveResponse.KnowledgeResult>> retrieveAsync(
            String query, Integer topK, String orgTag) {

        log.info("异步检索知识库: query={}, topK={}, orgTag={}", query, topK, orgTag);

        try {
            KnowledgeRetrieveRequest request = new KnowledgeRetrieveRequest(query, topK, orgTag);

            return knowledgeWebClient
                    .post()
                    .uri("/api/knowledge/retrieve")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(KnowledgeRetrieveResponse.class)
                    .toFuture()
                    .thenApply(response -> {
                        if (response != null && response.isSuccess()) {
                            return response.getData() != null ? response.getData().getResults() : List.<KnowledgeRetrieveResponse.KnowledgeResult>of();
                        }
                        return List.<KnowledgeRetrieveResponse.KnowledgeResult>of();
                    })
                    .exceptionally(ex -> {
                        log.error("异步检索异常: {}", ex.getMessage());
                        return List.<KnowledgeRetrieveResponse.KnowledgeResult>of();
                    });
        } catch (Exception e) {
            log.error("异步检索启动失败: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(List.of());
        }
    }

    /**
     * 格式化检索结果为可读文本。
     *
     * @param results 检索结果列表
     * @return 格式化后的文本
     */
    public String formatResults(List<KnowledgeRetrieveResponse.KnowledgeResult> results) {
        if (results == null || results.isEmpty()) {
            return "未找到相关知识";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("检索到 ").append(results.size()).append(" 条相关知识：\n\n");

        for (int i = 0; i < results.size(); i++) {
            KnowledgeRetrieveResponse.KnowledgeResult result = results.get(i);
            sb.append("【").append(i + 1).append("】");
            if (result.getTitle() != null) {
                sb.append(result.getTitle());
            }
            sb.append("\n");
            if (result.getContent() != null) {
                sb.append(result.getContent());
            }
            sb.append("\n");
            if (result.getSource() != null) {
                sb.append("来源：").append(result.getSource()).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
