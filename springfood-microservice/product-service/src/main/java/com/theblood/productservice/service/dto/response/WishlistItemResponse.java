package com.theblood.productservice.service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO cho wishlist item — gộp thông tin cơ bản của product để FE
 * không phải gọi thêm endpoint lấy product detail từng item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WishlistItemResponse {
    UUID wishlistId;
    UUID productId;
    UUID variantId;
    String note;
    Instant createdAt;

    // Snapshot product info (best-effort)
    String productName;
    String productImage;
    BigDecimal productPrice;
    BigDecimal productOriginalPrice;
    Boolean isAvailable;
}
