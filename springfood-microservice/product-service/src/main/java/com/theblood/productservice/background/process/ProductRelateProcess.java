package com.theblood.productservice.background.process;

import com.theblood.productservice.repository.ProductCategoryRepository;
import com.theblood.productservice.service.dto.request.RelateProductRequest;
import com.theblood.productservice.service.impl.RedisServiceWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProductRelateProcess implements Runnable {

    private final RelateProductRequest request;
    private final ProductCategoryRepository productCategoryRepository;
    private final RedisServiceWrapper redisServiceWrapper;
    private final String REDIS_CACHE_RELATE_RESULT = "related_products:";

    public ProductRelateProcess(RelateProductRequest request, ProductCategoryRepository productCategoryRepository, RedisServiceWrapper redisServiceWrapper) {
        this.request = request;
        this.productCategoryRepository = productCategoryRepository;
        this.redisServiceWrapper = redisServiceWrapper;
    }

    @Override
    public void run() {
        try {
            String cacheKey = REDIS_CACHE_RELATE_RESULT + request.getProductId();

            // Find related products
            List<UUID> relatedProducts = productCategoryRepository.findRelatedProductIds(
                    request.getProductId(),
                    request.getCategoryName()
            );

            if (relatedProducts.isEmpty()) {
                log.warn("No related products found for product ID: {}", request.getProductId());
            } else {
                redisServiceWrapper.setValueWithTimeout(cacheKey, relatedProducts, 1, TimeUnit.HOURS);
                log.info("Cached {} related products for product: {}", relatedProducts.size(), request.getProductId());
            }
        } catch (Exception e) {
            log.error("Failed to process relate request for product: {}", request.getProductId(), e);
            throw new RuntimeException(e);
        }
    }
}



