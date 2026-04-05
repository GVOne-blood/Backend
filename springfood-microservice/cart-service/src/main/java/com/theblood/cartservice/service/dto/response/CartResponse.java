package com.theblood.cartservice.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO cho Frontend
 * Bao gồm cart info, shop grouping, và validation warnings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    
    private String userId;
    
    // ========== Totals ==========
    private BigDecimal totalPrice;           // Tổng tiền tất cả items
    private BigDecimal selectedTotal;        // Tổng tiền items được chọn
    private Integer totalItems;              // Tổng số items
    private Integer selectedItems;           // Số items được chọn
    
    // ========== Shop Groups ==========
    @Builder.Default
    private List<ShopCartGroup> shopGroups = new ArrayList<>();
    
    // ========== Validation ==========
    @Builder.Default
    private List<String> warnings = new ArrayList<>();  // Warnings cho user
    private Boolean canCheckout;             // Có thể checkout không
    private Boolean hasUnavailableItems;     // Có item không available
    private Boolean hasInsufficientStock;    // Có item vượt quá stock
    
    // ========== Metadata ==========
    private LocalDateTime updatedAt;
    private LocalDateTime lastValidated;     // Lần cuối validate với product service
}
