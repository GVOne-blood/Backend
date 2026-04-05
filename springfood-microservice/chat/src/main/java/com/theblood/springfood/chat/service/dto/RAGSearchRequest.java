package com.theblood.springfood.chat.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Request for RAG semantic search
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAGSearchRequest {

    /**
     * User's question/query
     */
    @NotBlank(message = "Query is required")
    private String query;

    /**
     * Number of similar documents to retrieve
     */
    @Builder.Default
    @Min(value = 1, message = "Top K must be at least 1")
    @Max(value = 20, message = "Top K cannot exceed 20")
    private Integer topK = 5;

    /**
     * Minimum similarity threshold (0.0 to 1.0)
     * Only return documents with similarity >= this value
     */
    @Builder.Default
    @Min(value = 0, message = "Similarity threshold must be between 0 and 1")
    @Max(value = 1, message = "Similarity threshold must be between 0 and 1")
    private Double similarityThreshold = 0.7;

    /**
     * Filter by source type (optional)
     * Example: "PRODUCT", "FAQ"
     */
    private String sourceType;

    /**
     * Additional metadata filters (optional)
     * Example: {"category": "electronics", "language": "vi"}
     */
    @Builder.Default
    private Map<String, Object> metadataFilters = new HashMap<>();
}
