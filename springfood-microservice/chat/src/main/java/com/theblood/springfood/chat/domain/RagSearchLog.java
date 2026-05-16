package com.theblood.springfood.chat.domain;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import io.hypersistence.utils.hibernate.type.array.UUIDArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Entity for tracking RAG search queries and analytics.
 * Used for monitoring, debugging, and improving search quality.
 */
@Entity
@Table(
    name = "rag_search_logs",
    indexes = {
        @Index(name = "idx_search_logs_user", columnList = "user_id"),
        @Index(name = "idx_search_logs_created", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RagSearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User who performed the search
     */
    @Column(name = "user_id")
    private UUID userId;

    /**
     * Original search query
     */
    @Column(name = "query", nullable = false, columnDefinition = "TEXT")
    private String query;

    /**
     * Query embedding vector (768 dimensions)
     * Optional: Can be null to save storage if not needed for analytics
     * Note: Using double[] instead of float[] because Hypersistence Utils ListArrayType doesn't support Float
     */
    @Type(ListArrayType.class)
    @Column(name = "query_embedding", columnDefinition = "real[]")
    private List<Double> queryEmbedding;

    /**
     * IDs of documents retrieved for this query
     */
    @Type(UUIDArrayType.class)
    @Column(name = "retrieved_document_ids", columnDefinition = "uuid[]")
    private UUID[] retrievedDocumentIds;

    /**
     * Number of results returned
     */
    @Column(name = "results_count")
    private Integer resultsCount;

    /**
     * Top K parameter used in search
     */
    @Column(name = "top_k")
    private Integer topK;

    /**
     * Whether a response was successfully generated
     */
    @Column(name = "response_generated")
    @Builder.Default
    private Boolean responseGenerated = false;

    /**
     * Total response time in milliseconds
     */
    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    /**
     * Embedding generation time in milliseconds
     */
    @Column(name = "embedding_time_ms")
    private Integer embeddingTimeMs;

    /**
     * Vector search time in milliseconds
     */
    @Column(name = "search_time_ms")
    private Integer searchTimeMs;

    /**
     * LLM generation time in milliseconds
     */
    @Column(name = "llm_time_ms")
    private Integer llmTimeMs;

    /**
     * User feedback score (1-5 rating)
     */
    @Column(name = "feedback_score")
    private Integer feedbackScore;

    /**
     * User feedback comment
     */
    @Column(name = "feedback_comment", columnDefinition = "TEXT")
    private String feedbackComment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
