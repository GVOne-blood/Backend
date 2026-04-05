package com.theblood.shopservice.repository;

import com.theblood.shopservice.domain.ShopFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ShopFollow entity.
 */
@Repository
public interface ShopFollowRepository extends JpaRepository<ShopFollow, UUID> {

    /**
     * Find shops followed by user
     */
    @Query("SELECT sf FROM ShopFollow sf WHERE sf.userId = :userId ORDER BY sf.followedAt DESC")
    Page<ShopFollow> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find followers of a shop
     */
    @Query("SELECT sf FROM ShopFollow sf WHERE sf.shopId = :shopId ORDER BY sf.followedAt DESC")
    Page<ShopFollow> findByShopId(@Param("shopId") UUID shopId, Pageable pageable);

    /**
     * Find specific follow relationship
     */
    Optional<ShopFollow> findByUserIdAndShopId(UUID userId, UUID shopId);

    /**
     * Check if user follows shop
     */
    boolean existsByUserIdAndShopId(UUID userId, UUID shopId);

    /**
     * Count followers for a shop
     */
    long countByShopId(UUID shopId);

    /**
     * Count shops followed by user
     */
    long countByUserId(UUID userId);

    /**
     * Get follower count for multiple shops
     */
    @Query("SELECT sf.shopId, COUNT(sf) FROM ShopFollow sf WHERE sf.shopId IN :shopIds GROUP BY sf.shopId")
    List<Object[]> getFollowerCountsByShopIds(@Param("shopIds") List<UUID> shopIds);

    /**
     * Find most followed shops
     */
    @Query("SELECT sf.shopId, COUNT(sf) as followerCount FROM ShopFollow sf " +
           "GROUP BY sf.shopId ORDER BY followerCount DESC")
    Page<Object[]> findMostFollowedShops(Pageable pageable);

    /**
     * Find recent followers for a shop
     */
    @Query("SELECT sf FROM ShopFollow sf WHERE sf.shopId = :shopId AND sf.followedAt >= :since ORDER BY sf.followedAt DESC")
    List<ShopFollow> findRecentFollowersByShop(@Param("shopId") UUID shopId, @Param("since") Instant since);

    /**
     * Find users who follow multiple shops (for recommendations)
     */
    @Query("SELECT sf.userId FROM ShopFollow sf WHERE sf.shopId IN :shopIds GROUP BY sf.userId HAVING COUNT(sf) > 1")
    List<UUID> findUsersFollowingMultipleShops(@Param("shopIds") List<UUID> shopIds);

    /**
     * Get follow statistics for time period
     */
    @Query("SELECT DATE(sf.followedAt), COUNT(sf) FROM ShopFollow sf " +
           "WHERE sf.followedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(sf.followedAt) ORDER BY DATE(sf.followedAt)")
    List<Object[]> getFollowStatsByDateRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Delete follow relationship
     */
    void deleteByUserIdAndShopId(UUID userId, UUID shopId);

    /**
     * Delete all follows for a user
     */
    void deleteByUserId(UUID userId);

    /**
     * Delete all follows for a shop
     */
    void deleteByShopId(UUID shopId);
}