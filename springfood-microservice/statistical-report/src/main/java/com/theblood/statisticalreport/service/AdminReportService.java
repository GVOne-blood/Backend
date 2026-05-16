package com.theblood.statisticalreport.service;

import com.theblood.statisticalreport.dto.report.*;
import com.theblood.statisticalreport.repository.AdminReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service chứa logic cho admin/platform-wide reports — wraps
 * {@link AdminReportRepository} với fallback bucketing tuỳ {@code groupBy}.
 */
@Service
public class AdminReportService {

    private final AdminReportRepository repository;

    public AdminReportService(AdminReportRepository repository) {
        this.repository = repository;
    }

    public PlatformOverviewDTO getPlatformOverview() {
        return repository.findPlatformOverview();
    }

    public List<RevenueReportDTO> getPlatformRevenue(LocalDateTime start, LocalDateTime end, String groupBy) {
        return switch (normalizeGroupBy(groupBy)) {
            case "month" -> repository.findPlatformRevenueByBuckets(start, end, 4);
            case "year" -> repository.findPlatformRevenueByBuckets(start, end, 12);
            default -> repository.findPlatformRevenueByDay(start, end);
        };
    }

    public List<NewSignupReportDTO> getSignups(LocalDateTime start, LocalDateTime end, String groupBy) {
        return switch (normalizeGroupBy(groupBy)) {
            case "month" -> repository.findSignupsByBuckets(start, end, 4);
            case "year" -> repository.findSignupsByBuckets(start, end, 12);
            default -> repository.findSignupsByDay(start, end);
        };
    }

    public List<OrderStatusBreakdownDTO> getOrderStatusBreakdown(LocalDateTime start, LocalDateTime end) {
        return repository.findOrderStatusBreakdown(start, end);
    }

    public List<ShopRankingDTO> getTopShops(LocalDateTime start, LocalDateTime end, int limit) {
        return repository.findTopShops(start, end, Math.min(Math.max(limit, 1), 100));
    }

    public AdminReportRepository.WindowTotal getWindowTotal(LocalDateTime start, LocalDateTime end) {
        return repository.findWindowTotal(start, end);
    }

    private String normalizeGroupBy(String groupBy) {
        if (groupBy == null) return "day";
        return switch (groupBy.toLowerCase()) {
            case "day", "month", "year" -> groupBy.toLowerCase();
            default -> "day";
        };
    }
}
