package com.theblood.statisticalreport.dto.report;

import java.util.List;

/**
 * Aggregate dashboard payload tailored for the shop owner analytics page.
 *
 * <p>Combines the most useful slices of statistical data so the front-end can
 * render the entire dashboard with a single round-trip.</p>
 *
 * @param overview         All-time totals (orders, revenue, products, ratings…)
 * @param successRate      Order success/failure breakdown for the period
 * @param ratings          Rating histogram for the period
 * @param revenueSeries    Revenue per period bucket (chart series)
 * @param profitSeries     Profit per period bucket (chart series)
 * @param topProducts      Top selling products in the period
 * @param previousRevenue  Total revenue of the previous comparable window
 *                         (used by the FE to compute trend percentages)
 * @param previousOrders   Total orders of the previous comparable window
 * @param range            Echo of the requested range token (week/month/quarter/year)
 */
public record DashboardSnapshotDTO(
    ShopOverviewDTO overview,
    OrderSuccessRateDTO successRate,
    RatingReportDTO ratings,
    List<RevenueReportDTO> revenueSeries,
    List<ProfitReportDTO> profitSeries,
    List<TopProductDTO> topProducts,
    java.math.BigDecimal previousRevenue,
    Long previousOrders,
    String range
) {}
