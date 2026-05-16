package com.theblood.statisticalreport.dto.report;

public record OrderSuccessRateDTO(
    Long totalOrders,
    Long completedOrders,
    Long failedOrders,
    Long cancelledOrders,
    Long returnedOrders,
    Double successRate
) {}
