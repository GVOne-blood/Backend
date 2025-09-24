package com.spring_food.springfood.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequest {
    @NotBlank(message = "Product ID is required")
    private String productId;

    private String variantId; // Có thể null

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
