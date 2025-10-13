package com.theblood.productservice.background.process;

import com.theblood.common.exception.custom.InvalidDataException;
import com.theblood.productservice.dto.response.ProductDetail;
import com.theblood.productservice.mapper.ProductMapper;
import com.theblood.productservice.repository.FeedbackRepository;
import com.theblood.productservice.repository.ProductRepository;
import com.theblood.productservice.repository.projection.ProductProjection;
import com.theblood.productservice.service.impl.RedisServiceWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProductDetailProcess implements Runnable {

    private final UUID productId;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final FeedbackRepository feedbackRepository;
    private final RedisServiceWrapper redisServiceWrapper;
    private final String REDIS_PRODUCT_DETAIL_PREFIX = "product_detail:";

    public ProductDetailProcess(UUID productId, ProductRepository productRepository, ProductMapper productMapper, FeedbackRepository feedbackRepository, RedisServiceWrapper redisServiceWrapper) {
        this.productId = productId;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.feedbackRepository = feedbackRepository;
        this.redisServiceWrapper = redisServiceWrapper;
    }

    @Override
    public void run() {
        try {
            ProductProjection projection = productRepository.findProductProjectionById(productId)
                    .orElseThrow(() -> new InvalidDataException("Product not found"));

            ProductDetail productDetail = productMapper.toProductDetail(projection);

            String key = REDIS_PRODUCT_DETAIL_PREFIX + productId;
            redisServiceWrapper.setValueWithTimeout(key, productDetail, 5, TimeUnit.MINUTES);

            log.info("Fetched and cached product detail for product: {}", productId);
        } catch (Exception e) {
            log.error("Failed to fetch product detail for {}: {}", productId, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
