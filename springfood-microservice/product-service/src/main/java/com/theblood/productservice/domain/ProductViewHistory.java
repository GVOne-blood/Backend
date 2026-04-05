package com.theblood.productservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing product view history for "Recently Viewed" feature.
 * Maps to springfood_product.product_view_history table.
 */
@Entity
@Table(name = "product_view_history", schema = "springfood_product",
       indexes = {
           @Index(name = "idx_user_viewed_at", columnList = "user_id, viewed_at DESC")
       })
public class ProductViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @NotNull
    @Column(name = "viewed_at", nullable = false)
    private Instant viewedAt;

    @Size(max = 100)
    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Size(max = 50)
    @Column(name = "source", length = 50)
    private String source; // SEARCH, CATEGORY, RECOMMENDATION, DIRECT

    // Constructors
    public ProductViewHistory() {}

    public ProductViewHistory(UUID userId, UUID productId, String source) {
        this.userId = userId;
        this.productId = productId;
        this.source = source;
        this.viewedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Instant getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(Instant viewedAt) {
        this.viewedAt = viewedAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductViewHistory)) return false;
        ProductViewHistory that = (ProductViewHistory) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProductViewHistory{" +
                "id=" + id +
                ", userId=" + userId +
                ", productId=" + productId +
                ", viewedAt=" + viewedAt +
                ", source='" + source + '\'' +
                '}';
    }

    /**
     * Enum for view source types
     */
    public enum ViewSource {
        SEARCH,
        CATEGORY,
        RECOMMENDATION,
        DIRECT
    }
}