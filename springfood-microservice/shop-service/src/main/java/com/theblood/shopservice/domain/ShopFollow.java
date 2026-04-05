package com.theblood.shopservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing user following shop relationship.
 * Maps to springfood_shop.shop_follow table.
 */
@Entity
@Table(name = "shop_follow", schema = "springfood_shop",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_shop", columnNames = {"user_id", "shop_id"})
       })
public class ShopFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "follow_id")
    private UUID followId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @NotNull
    @CreationTimestamp
    @Column(name = "followed_at", nullable = false)
    private Instant followedAt;

    // Constructors
    public ShopFollow() {}

    public ShopFollow(UUID userId, UUID shopId) {
        this.userId = userId;
        this.shopId = shopId;
    }

    // Getters and Setters
    public UUID getFollowId() {
        return followId;
    }

    public void setFollowId(UUID followId) {
        this.followId = followId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public void setShopId(UUID shopId) {
        this.shopId = shopId;
    }

    public Instant getFollowedAt() {
        return followedAt;
    }

    public void setFollowedAt(Instant followedAt) {
        this.followedAt = followedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShopFollow)) return false;
        ShopFollow that = (ShopFollow) o;
        return followId != null && followId.equals(that.followId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ShopFollow{" +
                "followId=" + followId +
                ", userId=" + userId +
                ", shopId=" + shopId +
                ", followedAt=" + followedAt +
                '}';
    }
}