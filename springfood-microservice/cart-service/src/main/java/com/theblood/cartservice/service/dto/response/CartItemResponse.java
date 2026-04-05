package com.theblood.cartservice.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO cho CartItem
 * Chứa đầy đủ thông tin để frontend hiển thị
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    
    // ========== Product Identity ==========
    private String sku;
    private UUID productId;
    private String productName;
    private String productImage;
    
    // ========== Shop Info ==========
    private UUID shopId;
    private String shopName;
    
    // ========== Pricing ==========
    private BigDecimal originalPrice;
    private BigDecimal price;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;
    private String promotionName;
    private Integer discountPercent;     // % giảm giá để hiển thị
    
    // ========== Quantity & Stock ==========
    private Integer quantity;
    private Integer availableStock;
    private Boolean hasEnoughStock;      // quantity <= availableStock
    
    // ========== Availability ==========
    private Boolean isAvailable;
    private String unavailableReason;
    private String unavailableMessage;   // Message thân thiện cho user
    
    // ========== Variant ==========
    private String variantName;
    private Map<String, String> attributes;
    
    // ========== Selection ==========
    private Boolean selected;
    private Boolean canCheckout;         // Có thể checkout item này không
    
    // ========== Timestamps ==========
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}
