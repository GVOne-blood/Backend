package com.theblood.statisticalreport.dto.report;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Một dòng trong bảng "Top shops" — gộp doanh thu, đơn, rating cho 1 shop.
 *
 * @param shopId        UUID shop.
 * @param shopName      Tên shop (snapshot tại thời điểm query).
 * @param logoUrl       Logo shop nếu có.
 * @param totalRevenue  Tổng final_price của các đơn COMPLETED trong window.
 * @param totalOrders   Tổng đơn COMPLETED trong window.
 * @param avgRating     Avg rating từ feedbacks toàn shop (lifetime, không bound theo range).
 */
public record ShopRankingDTO(
    UUID shopId,
    String shopName,
    String logoUrl,
    BigDecimal totalRevenue,
    long totalOrders,
    Double avgRating
) {}
