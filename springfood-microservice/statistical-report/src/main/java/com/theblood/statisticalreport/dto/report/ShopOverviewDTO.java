package com.theblood.statisticalreport.dto.report;

import java.math.BigDecimal;

public record ShopOverviewDTO(
    Long totalOrders,
    BigDecimal totalRevenue,
    Long totalProducts,
    Double averageRating,
    Long totalFeedbacks,
    Long totalCustomers
) {}
