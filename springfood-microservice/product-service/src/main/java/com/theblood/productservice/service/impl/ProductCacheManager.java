package com.theblood.productservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCacheManager {

    private final RedisServiceWrapper redisServiceWrapper;

    // Cache key constants
    private static final String PRODUCT_DETAIL_PREFIX = "product_detail:";
    private static final String RELATED_PRODUCTS_PREFIX = "related_products:";

    /**
     * Invalidate cache for a specific product
     */
    public void invalidateProductCache(UUID productId) {
        try {
            // Invalidate product detail
            String productDetailKey = PRODUCT_DETAIL_PREFIX + productId;
            redisServiceWrapper.deleteKey(productDetailKey);

            // Invalidate related products
            String relatedProductsKey = RELATED_PRODUCTS_PREFIX + productId;
            redisServiceWrapper.deleteKey(relatedProductsKey);

            log.info("Invalidated cache for product: {}", productId);

        } catch (Exception e) {
            log.error("Error invalidating cache for product {}: {}", productId, e.getMessage());
        }
    }

    /**
     * Invalidate all product-related caches
     */
    public void invalidateAllProductCache() {
        try {
            // Delete all product detail keys
            Set<String> productDetailKeys = redisServiceWrapper.getKeys(PRODUCT_DETAIL_PREFIX + "*");
            if (productDetailKeys != null && !productDetailKeys.isEmpty()) {
                productDetailKeys.forEach(redisServiceWrapper::deleteKey);
            }

            // Delete all related products keys
            Set<String> relatedProductsKeys = redisServiceWrapper.getKeys(RELATED_PRODUCTS_PREFIX + "*");
            if (relatedProductsKeys != null && !relatedProductsKeys.isEmpty()) {
                relatedProductsKeys.forEach(redisServiceWrapper::deleteKey);
            }

            log.info("Invalidated all product caches");

        } catch (Exception e) {
            log.error("Error invalidating all product cache: {}", e.getMessage());
        }
    }

    /**
     * Get all cached product IDs
     */
    public Set<UUID> getAllCachedProductIds() {
        try {
            Set<String> keys = redisServiceWrapper.getKeys(PRODUCT_DETAIL_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return Set.of();
            }

            return keys.stream()
                .map(key -> key.replace(PRODUCT_DETAIL_PREFIX, ""))
                .map(UUID::fromString)
                .collect(java.util.stream.Collectors.toSet());

        } catch (Exception e) {
            log.error("Error getting cached product IDs: {}", e.getMessage());
            return Set.of();
        }
    }

    /**
     * Check if product is cached
     */
    public boolean isProductCached(UUID productId) {
        String key = PRODUCT_DETAIL_PREFIX + productId;
        return redisServiceWrapper.checkExistsKey(key);
    }

    /**
     * Check if related products are cached
     */
    public boolean isRelatedProductsCached(UUID productId) {
        String key = RELATED_PRODUCTS_PREFIX + productId;
        return redisServiceWrapper.checkExistsKey(key);
    }
}