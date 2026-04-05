package com.theblood.productservice.repository;

import com.theblood.productservice.domain.ProductViewHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for ProductViewHistory entity.
 */
@Repository
public interface ProductViewHistoryRepository extends JpaRepository<ProductViewHistory, UUID> {

    /**
     * Find recent viewed products by user (for "Recently Viewed" feature)
     */
    @Query("SELECT pvh FROM ProductViewHistory pvh WHERE pvh.userId = :userId ORDER BY pvh.viewedAt DESC")
    Page<ProductViewHistory> findRecentViewsByUser(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find view history by user and product
     */
    @Query("SELECT pvh FROM ProductViewHistory pvh WHERE pvh.userId = :userId AND pvh.productId = :productId ORDER BY pvh.viewedAt DESC")
    List<ProductViewHistory> findByUserAndProduct(@Param("userId") UUID userId, @Param("productId") UUID productId);

    /**
     * Get most viewed products (for analytics)
     */
    @Query("SELECT pvh.productId, COUNT(pvh) as viewCount FROM ProductViewHistory pvh " +
           "WHERE pvh.viewedAt >= :since GROUP BY pvh.productId ORDER BY viewCount DESC")
    Page<Object[]> findMostViewedProducts(@Param("since") Instant since, Pageable pageable);

    /**
     * Get view count for a product
     */
    @Query("SELECT COUNT(pvh) FROM ProductViewHistory pvh WHERE pvh.productId = :productId")
    long countViewsByProduct(@Param("productId") UUID productId);

    /**
     * Get view count for a product within time range
     */
    @Query("SELECT COUNT(pvh) FROM ProductViewHistory pvh WHERE pvh.productId = :productId AND pvh.viewedAt >= :since")
    long countViewsByProductSince(@Param("productId") UUID productId, @Param("since") Instant since);

    /**
     * Delete old view history (for cleanup)
     */
    @Modifying
    @Query("DELETE FROM ProductViewHistory pvh WHERE pvh.viewedAt < :before")
    int deleteOldViews(@Param("before") Instant before);

    /**
     * Find views by source type
     */
    @Query("SELECT pvh FROM ProductViewHistory pvh WHERE pvh.userId = :userId AND pvh.source = :source ORDER BY pvh.viewedAt DESC")
    List<ProductViewHistory> findByUserAndSource(@Param("userId") UUID userId, @Param("source") String source);

    /**
     * Check if user has viewed product recently (within last hour)
     */
    @Query("SELECT COUNT(pvh) > 0 FROM ProductViewHistory pvh WHERE pvh.userId = :userId AND pvh.productId = :productId AND pvh.viewedAt >= :since")
    boolean hasRecentView(@Param("userId") UUID userId, @Param("productId") UUID productId, @Param("since") Instant since);
}