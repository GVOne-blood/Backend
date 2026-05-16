package com.theblood.productservice.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * ProductProjection - Interface-based projection for product queries
 * Can be mapped to both ProductDetail and ProductDetailWithShop
 */
public interface ProductProjection {
    UUID getId();
    String getName();
    String getDescription();
    BigDecimal getPrice();
    String getImages();
    Integer getQuantity();
    LocalDate getMsg();
    LocalDate getExp();
    BigDecimal getAverageRating();
    Integer getTotalFeedbacks();
    String getShopName(); // Optional - may be null if not joined with shop table
}
