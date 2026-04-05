package com.theblood.productservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing user wishlist for favorite products.
 * Maps to springfood_product.user_wishlist table.
 */
@Entity
@Table(name = "user_wishlist", schema = "springfood_product",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_product", columnNames = {"user_id", "product_id"})
       })
public class UserWishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "wishlist_id")
    private UUID wishlistId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_id")
    private UUID variantId; // null = wish any variant

    @Size(max = 500)
    @Column(name = "note", length = 500)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    // Constructors
    public UserWishlist() {}

    public UserWishlist(UUID userId, UUID productId) {
        this.userId = userId;
        this.productId = productId;
    }

    public UserWishlist(UUID userId, UUID productId, UUID variantId, String note) {
        this.userId = userId;
        this.productId = productId;
        this.variantId = variantId;
        this.note = note;
    }

    // Getters and Setters
    public UUID getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(UUID wishlistId) {
        this.wishlistId = wishlistId;
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

    public UUID getVariantId() {
        return variantId;
    }

    public void setVariantId(UUID variantId) {
        this.variantId = variantId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserWishlist)) return false;
        UserWishlist that = (UserWishlist) o;
        return wishlistId != null && wishlistId.equals(that.wishlistId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "UserWishlist{" +
                "wishlistId=" + wishlistId +
                ", userId=" + userId +
                ", productId=" + productId +
                ", variantId=" + variantId +
                ", createdAt=" + createdAt +
                '}';
    }
}