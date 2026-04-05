package com.theblood.cartservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Updated CartItem với đầy đủ thông tin cần thiết cho e-commerce
 * So với version cũ, thêm: shop info, stock, pricing, variants, selection
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemUpdated {

    // ========== Product Identity ==========
    private String sku;              // Mã SKU của variant
    @Field(targetType = FieldType.STRING)
    private UUID productId;          // ID sản phẩm gốc
    private String productName;      // Snapshot tên sản phẩm
    private String productImage;     // Snapshot ảnh thumbnail

    // ========== Shop Information (CRITICAL) ==========
    @Field(targetType = FieldType.STRING)
    private UUID shopId;             // ID shop bán sản phẩm
    private String shopName;         // Snapshot tên shop

    // ========== Pricing & Promotions ==========
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal originalPrice;    // Giá gốc
    
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal price;            // Giá hiện tại (có thể đã giảm)
    
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal discountAmount;   // Số tiền giảm cho 1 item
    
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal finalPrice;       // Giá cuối = (price * quantity) - (discountAmount * quantity)
    
    private String promotionId;          // ID chương trình khuyến mãi
    private String promotionName;        // Tên khuyến mãi để hiển thị

    // ========== Quantity & Stock ==========
    private Integer quantity;            // Số lượng user muốn mua
    private Integer availableStock;      // Số lượng còn trong kho (snapshot)

    // ========== Availability ==========
    private Boolean isAvailable;         // Sản phẩm còn bán không
    private String unavailableReason;    // Lý do không khả dụng: "OUT_OF_STOCK", "DISCONTINUED", "SHOP_CLOSED"

    // ========== Product Variants ==========
    private String variantName;          // VD: "Màu Đỏ - Size XL"
    private Map<String, String> attributes; // VD: {"Color": "Red", "Size": "XL"}

    // ========== Selection ==========
    @Builder.Default
    private Boolean selected = true;     // User có chọn item này để checkout không

    // ========== Timestamps ==========
    private LocalDateTime addedAt;       // Thời gian thêm vào giỏ
    private LocalDateTime updatedAt;     // Lần cuối cập nhật

    // ========== Helper Methods ==========
    
    /**
     * Check xem có đủ stock để mua không
     */
    public Boolean hasEnoughStock() {
        return availableStock != null && availableStock >= quantity;
    }

    /**
     * Check xem item có thể checkout không
     */
    public Boolean canCheckout() {
        return isAvailable && hasEnoughStock();
    }

    /**
     * Tính lại finalPrice dựa trên quantity
     */
    public void recalculateFinalPrice() {
        if (price != null && quantity != null) {
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
            if (discountAmount != null) {
                BigDecimal totalDiscount = discountAmount.multiply(BigDecimal.valueOf(quantity));
                this.finalPrice = subtotal.subtract(totalDiscount);
            } else {
                this.finalPrice = subtotal;
            }
        }
    }
}
