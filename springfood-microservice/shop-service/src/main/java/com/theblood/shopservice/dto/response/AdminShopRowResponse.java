package com.theblood.shopservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Một dòng trong bảng "Quản lý Shop" của admin.
 *
 * <p>Hợp nhất thông tin shop ({@code springfood_shop.shops}) với stats
 * cross-schema (số đơn COMPLETED + GMV từ {@code springfood_order.orders},
 * số sản phẩm đang bán từ {@code springfood_product.products}). Service
 * fetch tất cả qua một query để admin list page chỉ cần 1 round-trip.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminShopRowResponse {

    private UUID shopId;
    private String shopName;
    private String logoUrl;

    /** ACTIVE, INACTIVE, BANNED, CLOSED, PENDING_APPROVAL. */
    private String shopStatus;

    private String email;
    private String phoneNumber;
    private String city;
    private String province;
    private String shopType;
    private String businessType;
    private String ownerId;

    private BigDecimal avgStar;
    private Integer totalFeedback;
    private Integer shopLevel;

    /** Số đơn COMPLETED toàn thời gian. */
    private long totalOrders;

    /** Tổng GMV (sum final_price) từ đơn COMPLETED. */
    private BigDecimal totalRevenue;

    /** Số sản phẩm đang có trên store. */
    private long totalProducts;

    /** Lý do ban gần nhất (null nếu chưa từng bị ban). */
    private String bannedReason;
    private Instant bannedAt;
    private String bannedBy;

    private Instant createdAt;
    private Instant updatedAt;
}
