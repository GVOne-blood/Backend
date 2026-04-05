package com.theblood.productservice.repository;

import com.theblood.productservice.domain.UserWishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserWishlist entity.
 */
@Repository
public interface UserWishlistRepository extends JpaRepository<UserWishlist, UUID> {

    /**
     * Find user's wishlist items
     */
    @Query("SELECT uw FROM UserWishlist uw WHERE uw.userId = :userId ORDER BY uw.createdAt DESC")
    Page<UserWishlist> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find wishlist item by user and product
     */
    Optional<UserWishlist> findByUserIdAndProductId(UUID userId, UUID productId);

    /**
     * Find wishlist item by user, product and variant
     */
    Optional<UserWishlist> findByUserIdAndProductIdAndVariantId(UUID userId, UUID productId, UUID variantId);

    /**
     * Check if product is in user's wishlist
     */
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    /**
     * Check if specific variant is in user's wishlist
     */
    boolean existsByUserIdAndProductIdAndVariantId(UUID userId, UUID productId, UUID variantId);

    /**
     * Count wishlist items for user
     */
    long countByUserId(UUID userId);

    /**
     * Get most wishlisted products (for analytics)
     */
    @Query("SELECT uw.productId, COUNT(uw) as wishlistCount FROM UserWishlist uw " +
           "GROUP BY uw.productId ORDER BY wishlistCount DESC")
    Page<Object[]> findMostWishlistedProducts(Pageable pageable);

    /**
     * Get wishlist count for a product
     */
    @Query("SELECT COUNT(uw) FROM UserWishlist uw WHERE uw.productId = :productId")
    long countByProductId(@Param("productId") UUID productId);

    /**
     * Find all users who wishlisted a product (for notifications)
     */
    @Query("SELECT uw.userId FROM UserWishlist uw WHERE uw.productId = :productId")
    List<UUID> findUserIdsByProductId(@Param("productId") UUID productId);

    /**
     * Find wishlist items by product IDs (for batch operations)
     */
    @Query("SELECT uw FROM UserWishlist uw WHERE uw.userId = :userId AND uw.productId IN :productIds")
    List<UserWishlist> findByUserIdAndProductIdIn(@Param("userId") UUID userId, @Param("productIds") List<UUID> productIds);

    /**
     * Delete wishlist item by user and product
     */
    void deleteByUserIdAndProductId(UUID userId, UUID productId);

    /**
     * Delete all wishlist items for a user
     */
    void deleteByUserId(UUID userId);
}