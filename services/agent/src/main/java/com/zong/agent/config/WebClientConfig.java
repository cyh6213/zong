package com.zong.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 配置类。
 * <p>
 * 用于 HTTP API 调用，特别是调用 services/knowledge 的 RAG 检索接口。
 */
@Configuration
public class WebClientConfig {

    /**
     * 创建 knowledge 服务的 WebClient 实例。
     * <p>
     * 基础 URL 从配置文件读取：spring.application.external.knowledge-service-url
     * 默认值：http://localhost:8081（knowledge 服务端口）
     */
    @Bean("knowledgeWebClient")
    public WebClient knowledgeWebClient(
            @org.springframework.beans.factory.annotation.Value(
                    "${spring.application.external.knowledge-service-url:http://localhost:8081}"
            ) String knowledgeServiceUrl) {
        return WebClient.builder()
                .baseUrl(knowledgeServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
