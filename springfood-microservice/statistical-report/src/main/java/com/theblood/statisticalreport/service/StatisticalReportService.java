package com.theblood.statisticalreport.service;

import com.theblood.statisticalreport.dto.report.*;
import com.theblood.statisticalreport.repository.StatisticalReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StatisticalReportService {

    private final StatisticalReportRepository repository;

    public StatisticalReportService(StatisticalReportRepository repository) {
        this.repository = repository;
    }

    public List<RevenueReportDTO> getRevenue(UUID shopId, LocalDateTime start, LocalDateTime end, String groupBy) {
        return switch (normalizeGroupBy(groupBy)) {
            case "day" -> repository.findRevenueByDay(shopId, start, end);
            case "month" -> repository.findRevenueByBuckets(shopId, start, end, 4);
            case "year" -> repository.findRevenueByBuckets(shopId, start, end, 12);
            default -> repository.findRevenueByDay(shopId, start, end);
        };
    }

    public OrderSuccessRateDTO getOrderSuccessRate(UUID shopId, LocalDateTime start, LocalDateTime end) {
        return repository.findOrderSuccessRate(shopId, start, end);
    }

    public List<ProfitReportDTO> getProfit(UUID shopId, LocalDateTime start, LocalDateTime end, String groupBy) {
        return switch (normalizeGroupBy(groupBy)) {
            case "day" -> repository.findProfitByDay(shopId, start, end);
            case "month" -> repository.findProfitByBuckets(shopId, start, end, 4);
            case "year" -> repository.findProfitByBuckets(shopId, start, end, 12);
            default -> repository.findProfitByDay(shopId, start, end);
        };
    }

    public RatingReportDTO getRating(UUID shopId, LocalDateTime start, LocalDateTime end) {
        return repository.findRatingByShop(shopId, start, end);
    }

    public ShopOverviewDTO getOverview(UUID shopId) {
        return repository.findShopOverview(shopId);
    }

    public List<TopProductDTO> getTopProducts(UUID shopId, LocalDateTime start, LocalDateTime end, int limit) {
        return repository.findTopProducts(shopId, start, end, Math.min(limit, 100));
    }

    public List<PlatformCommissionDTO> getCommission(UUID shopId, LocalDateTime start, LocalDateTime end, String groupBy) {
        return switch (normalizeGroupBy(groupBy)) {
            case "day" -> repository.findCommissionByDay(shopId, start, end);
            case "month" -> repository.findCommissionByBuckets(shopId, start, end, 4);
            case "year" -> repository.findCommissionByBuckets(shopId, start, end, 12);
            default -> repository.findCommissionByDay(shopId, start, end);
        };
    }

    public List<ShopCommissionDTO> getCommissionByShop(LocalDateTime start, LocalDateTime end) {
        return repository.findCommissionByShop(start, end);
    }

    private String normalizeGroupBy(String groupBy) {
        if (groupBy == null) return "day";
        return switch (groupBy.toLowerCase()) {
            case "day", "month", "year" -> groupBy.toLowerCase();
            default -> "day";
        };
    }
}
