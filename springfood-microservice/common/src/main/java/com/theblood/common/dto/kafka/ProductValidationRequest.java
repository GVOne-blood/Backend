package com.theblood.common.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductValidationRequest {
    private UUID productId;
    private String sku;
    private UUID shopId;
    private UUID userId;
    private String username; // current user
    private String categoryNames;
}