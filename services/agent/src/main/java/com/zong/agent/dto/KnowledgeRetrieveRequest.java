package com.zong.agent.dto;

import lombok.Data;

/**
 * 知识库检索请求 DTO。
 */
@Data
public class KnowledgeRetrieveRequest {

    /**
     * 检索查询文本。
     */
    private String query;

    /**
     * 返回结果数量，默认 5。
     */
    private Integer topK = 5;

    /**
     * 组织标签，用于过滤知识库。
     */
    private String orgTag;

    /**
     * 知识库 ID（可选），用于指定特定知识库。
     */
    private String knowledgeBaseId;

    public KnowledgeRetrieveRequest() {
    }

    public KnowledgeRetrieveRequest(String query) {
        this.query = query;
    }

    public KnowledgeRetrieveRequest(String query, Integer topK) {
        this.query = query;
        this.topK = topK;
    }

    public KnowledgeRetrieveRequest(String query, Integer topK, String orgTag) {
        this.query = query;
        this.topK = topK;
        this.orgTag = orgTag;
    }
}
