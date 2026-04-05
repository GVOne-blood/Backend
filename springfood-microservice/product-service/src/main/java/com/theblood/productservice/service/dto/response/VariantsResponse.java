package com.theblood.productservice.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class VariantsResponse {

    String productId;
    UUID variantId;
    String variantName;
    String[] attributes;
    BigDecimal variantPrice;
    Integer stock;  // Changed from long to Integer to match entity

}
