package com.theblood.springfood.chat.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response after uploading document to RAG system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponse {

    private String message;
    private String sourceType;
    private String sourceId;
    private Integer chunkCount;
    private Long documentId;
    private Boolean success;
}
