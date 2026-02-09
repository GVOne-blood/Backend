package com.theblood.cartservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    private String sku;           // Mã định danh biến thể (SKU-LAPTOP-001)
    private String productId;     // ID sản phẩm gốc (UUID dạng String)
    private String productName;   // Snapshot tên sản phẩm (để hiển thị nhanh)
    private String productImage;  // Snapshot ảnh thumbnail

    private Integer quantity;
    private BigDecimal price;     // Giá tại thời điểm add vào giỏ

    private LocalDateTime addedAt; // Thời gian thêm sản phẩm này
}