package com.theblood.cartservice.service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddToCartRequest {

    @NotBlank(message = "Product ID is required")
    String productId;

    String sku;

    @NotBlank(message = "Product name is required")
    String productName;

    String productImage;

    String shopId;

    String shopName;

    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity = 1;

    BigDecimal price;

    BigDecimal originalPrice;

    BigDecimal discountAmount;

    // ========== Variants (decision #3) ==========
    /**
     * Tên hiển thị của biến thể, vd "Màu Đỏ - Size XL".
     */
    String variantName;

    /**
     * Map thuộc tính biến thể, vd {"Color":"Red","Size":"XL"}.
     */
    Map<String, String> attributes;
}
