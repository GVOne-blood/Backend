package com.theblood.statisticalreport.dto.report;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregate payload cho Admin Dashboard — gộp tất cả slice mà UI cần
 * trong một response để FE chỉ phải gọi 1 endpoint.
 *
 * @param overview              Snapshot tổng nền tảng (lifetime + pending counts).
 * @param revenueSeries         Doanh thu / số đơn theo bucket thời gian (cho line/area chart).
 *                              Reuse {@link RevenueReportDTO} cho consistent với shop owner.
 * @param signupSeries          Đăng ký user/shop mới theo bucket (cho stacked bar/line chart).
 * @param orderStatusBreakdown  Phân bố đơn trong window theo status (cho donut chart).
 * @param topShops              Top N shop theo doanh thu trong window.
 * @param windowRevenue         Tổng revenue trong window (để show riêng).
 * @param windowOrders          Tổng đơn COMPLETED trong window.
 * @param previousRevenue       Revenue cùng kỳ liền trước (cho trend %).
 * @param previousOrders        Đơn cùng kỳ liền trước (cho trend %).
 * @param range                 Range token đã resolve ("week"|"month"|"quarter"|"year").
 */
public record AdminDashboardSnapshotDTO(
    PlatformOverviewDTO overview,
    List<RevenueReportDTO> revenueSeries,
    List<NewSignupReportDTO> signupSeries,
    List<OrderStatusBreakdownDTO> orderStatusBreakdown,
    List<ShopRankingDTO> topShops,
    BigDecimal windowRevenue,
    long windowOrders,
    BigDecimal previousRevenue,
    long previousOrders,
    String range
) {}
