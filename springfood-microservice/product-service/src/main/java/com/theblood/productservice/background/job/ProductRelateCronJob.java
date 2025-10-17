package com.theblood.productservice.background.job;

import com.theblood.productservice.repository.ProductCategoryRepository;
import com.theblood.productservice.repository.ProductRepository;
import com.theblood.productservice.service.impl.RedisServiceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRelateCronJob {

    private final RedisServiceWrapper redisServiceWrapper;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final String REDIS_CACHE_RELATE_RESULT = "related_products:";
    private final String REDIS_PRODUCT_DETAIL_PREFIX = "product_detail:";
    private Pageable pageable;


    @Scheduled(cron = "0 0 */6 * * *")
    public void cacheProductRelated() {
        List<UUID> productIds = redisServiceWrapper.getKeys(REDIS_PRODUCT_DETAIL_PREFIX + "*").stream()
                .map(key -> key.replace(REDIS_PRODUCT_DETAIL_PREFIX, ""))
                .map(UUID::fromString)
                .toList();

        if (productIds.isEmpty()) {
            log.warn("No product IDs found in cache to process related products.");
            // try to get from DB ...
            return;
        }
        log.info("Starting ProductRelateCronJob cacheProductRelated for {} products at {}", productIds.size(), java.time.LocalDateTime.now());

    }

    @Scheduled(cron = "0 */30 * * * *")
    public void updateProductRelate() {
        log.info("Starting ProductRelateCronJob updateProductRelate at {}", java.time.LocalDateTime.now());
        try {
            // Get all cached related product keys
            Set<String> relatedKeys = redisServiceWrapper.getKeys(REDIS_CACHE_RELATE_RESULT + "*");
            log.info("Found {} cached related product keys", relatedKeys != null ? relatedKeys.size() : 0);

            // Chưa có key relate nào trong redis
            if (relatedKeys == null || relatedKeys.isEmpty()) {
                log.warn("No cached related products found, get id from cache then get list relate product from DB...");
                // try to call cron job cacheProductRelated
            }


            int processedCount = 0;
            int successCount = 0;
            for (String key : relatedKeys) {
                try {
                    String productIdStr = key.replace(REDIS_CACHE_RELATE_RESULT, "");
                    log.debug("Processing related key: {}, extracted product ID: {}", key, productIdStr);
                    UUID productId = UUID.fromString(productIdStr);

                    // Recompute related products
                    boolean success = refreshRelatedProducts(productId, key);
                    if (success) successCount++;
                    processedCount++;

                } catch (Exception e) {
                    log.error("Error processing related key {}: {}", key, e.getMessage());
                }
            }

            log.info("Processed {} related product caches, {} successful refreshes", processedCount, successCount);

        } catch (Exception e) {
            log.error("Error updating product relations: {}", e.getMessage(), e);
        }
        log.info("Completed ProductRelateCronJob updateProductRelate");
    }

    private boolean refreshRelatedProducts(UUID productId, String cacheKey) {
        try {
            log.debug("Refreshing related products for product ID: {}", productId);
            // Get product categories (this is a simplified approach)
            // In real implementation, you might need to get categories from product
            List<String> categoryNames = getProductCategories(productId);
            log.debug("Found {} categories for product: {}", categoryNames.size(), productId);

            if (categoryNames.isEmpty()) {
                log.warn("No categories found for product: {}", productId);
                return false;
            }

            // Find related products for each category
            List<UUID> allRelatedIds = categoryNames.stream()
                    .flatMap(category -> {
                        try {
                            List<UUID> ids = productCategoryRepository.findRelatedProductIds(productId, List.of(category));
                            log.debug("Found {} related products for category {} of product {}", ids.size(), category, productId);
                            return ids.stream();
                        } catch (Exception e) {
                            log.warn("Error finding related products for category {}: {}", category, e.getMessage());
                            return Stream.empty();
                        }
                    })
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());

            if (allRelatedIds.isEmpty()) {
                log.warn("No related products found for product: {}", productId);
                return false;
            } else {
                // Update cache
                redisServiceWrapper.setValueWithTimeout(cacheKey, allRelatedIds, 60, TimeUnit.MINUTES);
                log.debug("Refreshed {} related products for product: {}", allRelatedIds.size(), productId);
                return true;
            }

        } catch (Exception e) {
            log.error("Failed to refresh related products for {}: {}", productId, e.getMessage());
            return false;
        }
    }

    private List<String> getProductCategories(UUID productId) {
        try {
            return productRepository.findById(productId)
                    .map(product -> product.getProductCategories().stream()
                            .map(pc -> pc.getCategories().getName())
                            .collect(Collectors.toList()))
                    .orElse(List.of());
        } catch (Exception e) {
            log.error("Error getting categories for product {}: {}", productId, e.getMessage());
            return List.of();
        }
    }
}

