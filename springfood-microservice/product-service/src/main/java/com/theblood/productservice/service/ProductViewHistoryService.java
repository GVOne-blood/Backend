package com.theblood.productservice.service;

import com.theblood.productservice.domain.ProductViewHistory;
import com.theblood.productservice.repository.ProductViewHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing ProductViewHistory entities.
 */
@Service
@Transactional
public class ProductViewHistoryService {

    private final ProductViewHistoryRepository productViewHistoryRepository;

    @Autowired
    public ProductViewHistoryService(ProductViewHistoryRepository productViewHistoryRepository) {
        this.productViewHistoryRepository = productViewHistoryRepository;
    }

    /**
     * Record a product view
     */
    public ProductViewHistory recordProductView(UUID userId, UUID productId, String source, String sessionId) {
        // Check if user has viewed this product recently (within last hour) to avoid spam
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        if (productViewHistoryRepository.hasRecentView(userId, productId, oneHourAgo)) {
            // Don't record duplicate views within the same hour
            return null;
        }

        ProductViewHistory viewHistory = new ProductViewHistory(userId, productId, source);
        viewHistory.setSessionId(sessionId);
        return productViewHistoryRepository.save(viewHistory);
    }

    /**
     * Get recently viewed products for user
     */
    @Transactional(readOnly = true)
    public Page<ProductViewHistory> getRecentlyViewedProducts(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productViewHistoryRepository.findRecentViewsByUser(userId, pageable);
    }

    /**
     * Get view history for specific user and product
     */
    @Transactional(readOnly = true)
    public List<ProductViewHistory> getViewHistoryByUserAndProduct(UUID userId, UUID productId) {
        return productViewHistoryRepository.findByUserAndProduct(userId, productId);
    }

    /**
     * Get most viewed products (for analytics/recommendations)
     */
    @Transactional(readOnly = true)
    public Page<Object[]> getMostViewedProducts(int days, int page, int size) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        Pageable pageable = PageRequest.of(page, size);
        return productViewHistoryRepository.findMostViewedProducts(since, pageable);
    }

    /**
     * Get view count for a product
     */
    @Transactional(readOnly = true)
    public long getProductViewCount(UUID productId) {
        return productViewHistoryRepository.countViewsByProduct(productId);
    }

    /**
     * Get view count for a product within specific time period
     */
    @Transactional(readOnly = true)
    public long getProductViewCountSince(UUID productId, int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        return productViewHistoryRepository.countViewsByProductSince(productId, since);
    }

    /**
     * Get views by source type for user
     */
    @Transactional(readOnly = true)
    public List<ProductViewHistory> getViewsByUserAndSource(UUID userId, String source) {
        return productViewHistoryRepository.findByUserAndSource(userId, source);
    }

    /**
     * Clean up old view history (older than specified days)
     */
    public int cleanupOldViewHistory(int daysToKeep) {
        Instant cutoffDate = Instant.now().minus(daysToKeep, ChronoUnit.DAYS);
        return productViewHistoryRepository.deleteOldViews(cutoffDate);
    }

    /**
     * Check if user has viewed product recently
     */
    @Transactional(readOnly = true)
    public boolean hasUserViewedProductRecently(UUID userId, UUID productId, int hours) {
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        return productViewHistoryRepository.hasRecentView(userId, productId, since);
    }

    /**
     * Get user's recently viewed products (simple list for quick access)
     */
    @Transactional(readOnly = true)
    public List<ProductViewHistory> getRecentlyViewedProductsList(UUID userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productViewHistoryRepository.findRecentViewsByUser(userId, pageable).getContent();
    }
}