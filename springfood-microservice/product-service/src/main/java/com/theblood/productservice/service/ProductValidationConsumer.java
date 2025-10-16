package com.theblood.productservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.common.dto.kafka.ProductValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductValidationConsumer {

    private final ObjectMapper objectMapper;
    private final ProductService productService;

    // Lưu trữ các latch để chờ phản hồi từ identity-service
    private final ConcurrentHashMap<String, ValidationResult> pendingValidations = new ConcurrentHashMap<>();

    @KafkaListener(topics = "product-validation-response", groupId = "product-service-validation-group")
    public void handleValidationResponse(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received validation response: {}", message);

            ProductValidationResponse response = objectMapper.readValue(message, ProductValidationResponse.class);

            // Tìm correlationId từ response
            String correlationId = response.getProductId().toString(); // Sử dụng productId làm correlationId

            ValidationResult result = pendingValidations.get(correlationId);
            if (result != null) {
                if (response.isValid()) {
                    result.latch.countDown();
                    result.success = true;
                    result.errorMessage = null;
                    log.info("Product validation successful for product: {}", response.getProductId());
                } else {
                    result.latch.countDown();
                    result.success = false;
                    result.errorMessage = response.getErrorMessage();
                    log.warn("Product validation failed for product: {} - {}", response.getProductId(), response.getErrorMessage());
                }
            } else {
                log.warn("Received validation response for unknown correlationId: {}", correlationId);
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing validation response: {}", e.getMessage(), e);
            // Không acknowledge để message được reprocess
        }
    }

    /**
     * Đăng ký một validation request và chờ kết quả
     */
    public ValidationResult waitForValidation(String correlationId) throws InterruptedException {
        ValidationResult result = new ValidationResult();
        pendingValidations.put(correlationId, result);

        // Đợi tối đa 30 giây
        boolean completed = result.latch.await(30, TimeUnit.SECONDS);

        if (!completed) {
            log.warn("Validation timeout for correlationId: {}", correlationId);
            result.success = false;
            result.errorMessage = "Validation timeout";
        }

        // Xóa khỏi pending sau khi xử lý xong
        pendingValidations.remove(correlationId);

        return result;
    }

    public static class ValidationResult {
        public CountDownLatch latch = new CountDownLatch(1);
        public boolean success = false;
        public String errorMessage;
    }
}