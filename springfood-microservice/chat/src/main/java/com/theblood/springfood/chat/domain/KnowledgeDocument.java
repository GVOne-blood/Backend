package com.theblood.springfood.chat.domain;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Entity for tracking documents ingested into Spring AI VectorStore.
 * Maintains mapping between source documents and their vector store IDs.
 * 
 * This is needed because Spring AI VectorStore only supports delete by ID,
 * not by metadata. We track IDs here to enable deletion by source.
 */
@Entity
@Table(name = "knowledge_documents", indexes = {
    @Index(name = "idx_documents_source", columnList = "source_type, source_id"),
    @Index(name = "idx_documents_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    /**
     * Original document content (before chunking)
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Source type: PRODUCT, ORDER, POLICY, FAQ, MANUAL
     */
    @Column(name = "source_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private DocumentSourceType sourceType;

    /**
     * Reference to original entity (product_id, order_id, etc.)
     */
    @Column(name = "source_id", length = 255)
    private String sourceId;

    /**
     * Additional metadata (category, tags, author, etc.)
     */
    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * List of vector store document IDs created from this document.
     * Used for deletion when document becomes outdated.
     */
    @ElementCollection
    @CollectionTable(
        name = "knowledge_document_vector_ids",
        joinColumns = @JoinColumn(name = "document_id")
    )
    @Column(name = "vector_id")
    @Builder.Default
    private List<String> vectorStoreIds = new ArrayList<>();

    @Column(name = "chunk_count")
    private Integer chunkCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Document source types
     */
    public enum DocumentSourceType {
        PRODUCT,
        ORDER,
        POLICY,
        FAQ,
        MANUAL,
        GENERAL
    }
}
