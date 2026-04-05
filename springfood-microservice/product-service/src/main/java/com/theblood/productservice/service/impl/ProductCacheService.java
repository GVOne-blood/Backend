package com.theblood.productservice.service.impl;

import com.theblood.productservice.background.process.ProductDetailProcess;
import com.theblood.productservice.background.process.ProductRelateProcess;
import com.theblood.productservice.repository.FeedbackRepository;
import com.theblood.productservice.repository.ProductCategoryRepository;
import com.theblood.productservice.repository.ProductRepository;
import com.theblood.productservice.service.dto.request.RelateProductRequest;
import com.theblood.productservice.service.mapper.ProductMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCacheService {

    private final RedisServiceWrapper redisServiceWrapper;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final FeedbackRepository feedbackRepository;
    private final ProductCategoryRepository productCategoryRepository;

    private final ExecutorService processExecutor = Executors.newFixedThreadPool(20);

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down process executor...");
        processExecutor.shutdown();
        try {
            if (!processExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                processExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            processExecutor.shutdownNow();
        }
    }

    public Future<?> submitProductDetailTask(UUID productId) {
        ProductDetailProcess process = new ProductDetailProcess(
                productId,
                productRepository,
                productMapper,
                feedbackRepository,
                redisServiceWrapper
        );
        return processExecutor.submit(process);
    }

    public Future<?> submitProductRelateTask(RelateProductRequest request) {
        ProductRelateProcess process = new ProductRelateProcess(
                request,
                productCategoryRepository,
                redisServiceWrapper
        );
        return processExecutor.submit(process);
    }
}
