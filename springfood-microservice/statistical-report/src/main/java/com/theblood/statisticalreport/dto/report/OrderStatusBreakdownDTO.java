package com.theblood.statisticalreport.dto.report;

/**
 * Phân bố đơn theo status trong một window — dùng để vẽ donut chart.
 *
 * @param status  Tên status (e.g. COMPLETED, CANCELLED, PENDING…).
 * @param count   Số đơn ở status đó.
 */
public record OrderStatusBreakdownDTO(
    String status,
    long count
) {}
