package com.zong.agent.dto;

import lombok.Data;

import java.util.List;

/**
 * 知识库检索响应 DTO。
 */
@Data
public class KnowledgeRetrieveResponse {

    /**
     * 响应码，0 表示成功。
     */
    private Integer code;

    /**
     * 响应消息。
     */
    private String message;

    /**
     * 检索结果数据。
     */
    private KnowledgeData data;

    @Data
    public static class KnowledgeData {
        /**
         * 检索结果列表。
         */
        private List<KnowledgeResult> results;
    }

    @Data
    public static class KnowledgeResult {
        /**
         * 知识条目 ID。
         */
        private String id;

        /**
         * 文档标题。
         */
        private String title;

        /**
         * 文档内容摘要。
         */
        private String content;

        /**
         * 相似度分数。
         */
        private Double score;

        /**
         * 来源路径。
         */
        private String source;

        /**
         * 所属知识库。
         */
        private String knowledgeBase;

        /**
         * 元数据（可选）。
         */
        private Object metadata;
    }

    /**
     * 判断请求是否成功。
     */
    public boolean isSuccess() {
        return code != null && code == 0;
    }
}
