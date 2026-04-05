package com.theblood.cartservice.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Group items theo shop
 * Frontend sẽ hiển thị từng shop riêng biệt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopCartGroup {
    
    private UUID shopId;
    private String shopName;
    private String shopAvatar;
    
    @Builder.Default
    private List<CartItemResponse> items = new ArrayList<>();
    
    private BigDecimal shopTotal;        // Tổng tiền items của shop này
    private BigDecimal selectedTotal;    // Tổng tiền items được chọn
    private Integer itemCount;           // Số lượng items
    private Integer selectedCount;       // Số items được chọn
    
    private Boolean allSelected;         // Tất cả items đều được chọn
    private Boolean hasUnavailableItems; // Shop có items không available
}
