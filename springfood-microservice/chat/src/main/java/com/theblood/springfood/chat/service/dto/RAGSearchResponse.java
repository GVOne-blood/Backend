package com.theblood.springfood.chat.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response from RAG semantic search
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAGSearchResponse {

    private String query;
    private List<SearchResult> results;
    private Integer totalResults;
    private String context; // Combined context for AI

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResult {
        private String content;
        private Double similarity;
        private Map<String, Object> metadata;
        private String sourceType;
        private String sourceId;
        private String title;
    }
}
