package com.theblood.common.dto.kafka;

import com.theblood.common.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreationEvent {
    private UUID productId;
    private String sku;
    private UUID shopId;
    private String shopName;
    private String username; // Người tạo
    private List<String> categoryNames;
    private ProductStatus status;
    private String productName;
    private String description;
    private String price;
    private Integer quantity;
    private LocalDateTime createdAt;
    private String sourceService = "product-service";
    private String correlationId; // Để track events liên quan
}
