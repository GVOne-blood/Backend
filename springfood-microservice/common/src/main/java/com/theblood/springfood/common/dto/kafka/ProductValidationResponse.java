package com.theblood.springfood.common.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductValidationResponse {
    private UUID productId;
    private String sku;
    private UUID shopId;
    private String username;
    private boolean isValid;
    private String errorMessage; // Nếu không hợp lệ
    private String validationType; // "USER_AUTHORIZATION", "SHOP_STATUS", "SKU_EXISTS"
}