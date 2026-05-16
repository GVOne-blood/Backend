package com.theblood.springfood.common.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * ProductDetailWithShop - Extended product detail with shop information
 * Used for APIs that need to display shop name along with product details
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetailWithShop {
    UUID id;
    String name;
    String description;
    BigDecimal price;
    String images;
    Integer quantity;
    LocalDate msg;
    LocalDate exp;
    Double averageRating;
    Long totalFeedbacks;
    String shopName;

    /**
     * Giá gốc (chưa giảm). Bằng price khi không có sale active.
     */
    BigDecimal originalPrice;

    /**
     * % giảm giá đang áp dụng (0-100). Null/0 nếu không có sale active.
     */
    BigDecimal discountPercentage;
}
