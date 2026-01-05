package com.theblood.orderservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequest {
    @NotBlank(message = "Product ID is required")
    private String productId;

    private String variantId; // Có thể null

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @Min(value = 0, message = "Price a product must be at least 0")
    private BigDecimal priceAtBooking;


}
