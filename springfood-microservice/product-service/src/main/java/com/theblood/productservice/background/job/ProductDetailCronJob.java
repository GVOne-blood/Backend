package com.theblood.productservice.background.job;


import com.theblood.productservice.dto.response.ProductDetail;
import com.theblood.productservice.mapper.ProductMapper;
import com.theblood.productservice.repository.ProductRepository;
import com.theblood.productservice.service.ProductService;
import com.theblood.productservice.service.impl.RedisServiceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductDetailCronJob {

    private final RedisServiceWrapper redisServiceWrapper;
    private final String REDIS_PRODUCT_DETAIL_PREFIX = "product_detail:";
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private LocalDateTime lastRunCompleted = LocalDateTime.now();

    @Scheduled(cron = "0 0 */6 * * *")
    public void cacheProductDetails() {
        log.info("Starting ProductDetailCronJob cacheProductDetails at {}", java.time.LocalDateTime.now());
        try {
            // get all keys with prefix REDIS_PRODUCT_DETAIL_PREFIX
            List<ProductDetail> productDetails = productService.getAllProductDetails();

            if (productDetails == null || productDetails.isEmpty()) {
                log.info("No product details found in database, skipping cache update");
                return;
            }
            log.info("Fetched {} product details from database", productDetails.size());

            for (ProductDetail pd : productDetails) {
                String key = REDIS_PRODUCT_DETAIL_PREFIX + pd.getId().toString();
                redisServiceWrapper.setValueWithTimeout(key, pd, 5, TimeUnit.HOURS);
            }
            log.info("Cached {} product details in Redis", productDetails.size());
            lastRunCompleted = LocalDateTime.now();
        } catch (Exception e) {
            log.error("Error occurred during ProductDetailCronJob: {}", e.getMessage());
        }
        log.info("Finished ProductDetailCronJob updateProductDetails at {}", java.time.LocalDateTime.now());
    }

    @Scheduled(cron = "0 */2 * * * *")
    public void updateProductDetails() {
        log.info("Starting ProductDetailCronJob updateProductDetails at {}", java.time.LocalDateTime.now());

        try {
            List<ProductDetail> productDetails = productService.getAllLastUpdatedProducts(lastRunCompleted);

            if (productDetails == null || productDetails.isEmpty()) {
                log.info("No updated product details found since {}, skipping cache update", lastRunCompleted);
                return;
            }
            for (ProductDetail pd : productDetails) {
                String key = REDIS_PRODUCT_DETAIL_PREFIX + pd.getId().toString();
                redisServiceWrapper.setValueWithTimeout(key, pd, 5, TimeUnit.HOURS);
            }
            // check deleted products

        } catch (Exception e) {
            log.error("Error occurred during ProductDetailCronJob: {}", e.getMessage());
        }

    }
}
