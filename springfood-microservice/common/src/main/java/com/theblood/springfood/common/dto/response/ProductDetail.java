package com.theblood.springfood.common.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetail {
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

    /**
     * Giá gốc (chưa giảm). Bằng price khi không có sale active.
     */
    BigDecimal originalPrice;

    /**
     * % giảm giá đang áp dụng (0-100). Null/0 nếu không có sale active.
     */
    BigDecimal discountPercentage;

    /**
     * Backwards-compatible 10-arg constructor used by JPQL constructor
     * expressions such as {@code ProductRepository#findProductDetailById}.
     * Sale-related fields default to {@code null}; callers (e.g.
     * {@link com.theblood.springfood.common.dto.response.ProductDetail}) that
     * need them are expected to set them explicitly via {@code SaleApplier}
     * after the projection is loaded.
     */
    public ProductDetail(UUID id,
                         String name,
                         String description,
                         BigDecimal price,
                         String images,
                         Integer quantity,
                         LocalDate msg,
                         LocalDate exp,
                         Double averageRating,
                         Long totalFeedbacks) {
        this(id, name, description, price, images, quantity, msg, exp, averageRating, totalFeedbacks, null, null);
    }
}
