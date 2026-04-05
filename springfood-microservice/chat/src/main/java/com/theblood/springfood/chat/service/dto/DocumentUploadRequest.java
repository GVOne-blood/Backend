package com.theblood.springfood.chat.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for uploading documents to RAG system.
 * Supports both text content and file uploads.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {

    /**
     * Document title (for display purposes)
     */
    @NotBlank(message = "Title is required")
    private String title;

    /**
     * Plain text content (for text-based documents)
     * Either content or file must be provided
     */
    private String content;

    /**
     * Source type: PRODUCT, ORDER, FAQ, POLICY, GENERAL
     */
    @NotNull(message = "Source type is required")
    private String sourceType;

    /**
     * Source identifier (e.g., product_id, order_id)
     * Used for updating/deleting specific documents
     */
    @NotBlank(message = "Source ID is required")
    private String sourceId;

    /**
     * Additional metadata (optional)
     * Examples:
     * - category: "electronics", "food"
     * - language: "vi", "en"
     * - author: "admin"
     * - tags: ["important", "featured"]
     */
    @Builder.Default
    private Map<String, Object> additionalMetadata = new HashMap<>();

    /**
     * Build complete metadata for vector store
     */
    public Map<String, Object> buildMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", title);
        metadata.put("source_type", sourceType);
        metadata.put("source_id", sourceId);
        
        // Add all additional metadata
        if (additionalMetadata != null) {
            metadata.putAll(additionalMetadata);
        }
        
        return metadata;
    }
}
