package com.theblood.statisticalreport.web.rest;

import com.theblood.statisticalreport.dto.report.*;
import com.theblood.statisticalreport.service.StatisticalReportService;
import com.theblood.statisticalreport.service.carbone.CarboneCloudService;
import com.theblood.statisticalreport.service.carbone.CarboneService;
import com.theblood.statisticalreport.service.carbone.dto.CarboneResponseData;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/statistical-reports")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticalReportResources {

    @Autowired(required = false)
    CarboneService carboneService;

    @Autowired(required = false)
    CarboneCloudService carboneCloudService;

    @Value("${carbone.cloud.enabled:false}")
    boolean carboneCloudEnabled;

    private final StatisticalReportService reportService;

    public StatisticalReportResources(StatisticalReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/shops/{shopId}/revenue")
    public ResponseEntity<List<RevenueReportDTO>> getRevenue(
        @PathVariable UUID shopId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "day") String groupBy
    ) {
        return ResponseEntity.ok(reportService.getRevenue(shopId, startDate, endDate, groupBy));
    }

    @GetMapping("/shops/{shopId}/order-success-rate")
    public ResponseEntity<OrderSuccessRateDTO> getOrderSuccessRate(
        @PathVariable UUID shopId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(reportService.getOrderSuccessRate(shopId, startDate, endDate));
    }

    @GetMapping("/shops/{shopId}/profit")
    public ResponseEntity<List<ProfitReportDTO>> getProfit(
        @PathVariable UUID shopId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "day") String groupBy
    ) {
        return ResponseEntity.ok(reportService.getProfit(shopId, startDate, endDate, groupBy));
    }

    @GetMapping("/shops/{shopId}/ratings")
    public ResponseEntity<RatingReportDTO> getRatings(
        @PathVariable UUID shopId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(reportService.getRating(shopId, startDate, endDate));
    }

    @GetMapping("/shops/{shopId}/overview")
    public ResponseEntity<ShopOverviewDTO> getOverview(@PathVariable UUID shopId) {
        return ResponseEntity.ok(reportService.getOverview(shopId));
    }

    @GetMapping("/shops/{shopId}/top-products")
    public ResponseEntity<List<TopProductDTO>> getTopProducts(
        @PathVariable UUID shopId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(reportService.getTopProducts(shopId, startDate, endDate, limit));
    }

    @GetMapping("/platform/commissions")
    public ResponseEntity<List<PlatformCommissionDTO>> getCommissions(
        @RequestParam(required = false) UUID shopId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "day") String groupBy
    ) {
        return ResponseEntity.ok(reportService.getCommission(shopId, startDate, endDate, groupBy));
    }

    @GetMapping("/platform/commissions/by-shop")
    public ResponseEntity<List<ShopCommissionDTO>> getCommissionsByShop(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(reportService.getCommissionByShop(startDate, endDate));
    }

    // ---------------------------------------------------------------------
    // "Me" endpoints — resolve shopId from the X-Shop-ID header populated by
    // the API gateway from the JWT `sid` claim. Use these from the front-end
    // so callers don't need to know their own shopId.
    // ---------------------------------------------------------------------

    /** Range token accepted by /me/dashboard. */
    private static final String DEFAULT_RANGE = "week";

    @GetMapping("/me/overview")
    public ResponseEntity<ShopOverviewDTO> getMyOverview(@RequestHeader("X-Shop-ID") String shopIdHeader) {
        return ResponseEntity.ok(reportService.getOverview(parseShopId(shopIdHeader)));
    }

    @GetMapping("/me/revenue")
    public ResponseEntity<List<RevenueReportDTO>> getMyRevenue(
        @RequestHeader("X-Shop-ID") String shopIdHeader,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "day") String groupBy
    ) {
        return ResponseEntity.ok(reportService.getRevenue(parseShopId(shopIdHeader), startDate, endDate, groupBy));
    }

    @GetMapping("/me/profit")
    public ResponseEntity<List<ProfitReportDTO>> getMyProfit(
        @RequestHeader("X-Shop-ID") String shopIdHeader,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "day") String groupBy
    ) {
        return ResponseEntity.ok(reportService.getProfit(parseShopId(shopIdHeader), startDate, endDate, groupBy));
    }

    @GetMapping("/me/order-success-rate")
    public ResponseEntity<OrderSuccessRateDTO> getMyOrderSuccessRate(
        @RequestHeader("X-Shop-ID") String shopIdHeader,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(reportService.getOrderSuccessRate(parseShopId(shopIdHeader), startDate, endDate));
    }

    @GetMapping("/me/ratings")
    public ResponseEntity<RatingReportDTO> getMyRatings(
        @RequestHeader("X-Shop-ID") String shopIdHeader,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(reportService.getRating(parseShopId(shopIdHeader), startDate, endDate));
    }

    @GetMapping("/me/top-products")
    public ResponseEntity<List<TopProductDTO>> getMyTopProducts(
        @RequestHeader("X-Shop-ID") String shopIdHeader,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(reportService.getTopProducts(parseShopId(shopIdHeader), startDate, endDate, limit));
    }

    /**
     * Aggregate dashboard endpoint — resolves a date window from {@code range},
     * fetches every slice the shop owner dashboard needs in parallel-friendly
     * code, and returns a single {@link DashboardSnapshotDTO}.
     *
     * @param range one of {@code week|month|quarter|year} (defaults to {@code week}).
     */
    @GetMapping("/me/dashboard")
    public ResponseEntity<DashboardSnapshotDTO> getMyDashboard(
        @RequestHeader("X-Shop-ID") String shopIdHeader,
        @RequestParam(defaultValue = DEFAULT_RANGE) String range,
        @RequestParam(defaultValue = "5") int topProductsLimit
    ) {
        UUID shopId = parseShopId(shopIdHeader);
        DateRange window = resolveRange(range);
        DateRange previous = previousWindow(window);
        String groupBy = window.suggestedGroupBy();

        ShopOverviewDTO overview = reportService.getOverview(shopId);
        OrderSuccessRateDTO success = reportService.getOrderSuccessRate(shopId, window.start(), window.end());
        RatingReportDTO ratings = reportService.getRating(shopId, window.start(), window.end());
        List<RevenueReportDTO> revenue = reportService.getRevenue(shopId, window.start(), window.end(), groupBy);
        List<ProfitReportDTO> profit = reportService.getProfit(shopId, window.start(), window.end(), groupBy);
        List<TopProductDTO> topProducts = reportService.getTopProducts(shopId, window.start(), window.end(), topProductsLimit);

        // Previous-period totals, used by the FE for trend percentages.
        java.math.BigDecimal previousRevenue = reportService
            .getRevenue(shopId, previous.start(), previous.end(), "day")
            .stream()
            .map(RevenueReportDTO::revenue)
            .filter(v -> v != null)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        Long previousOrders = reportService
            .getRevenue(shopId, previous.start(), previous.end(), "day")
            .stream()
            .map(RevenueReportDTO::orderCount)
            .filter(v -> v != null)
            .reduce(0L, Long::sum);

        DashboardSnapshotDTO snapshot = new DashboardSnapshotDTO(
            overview,
            success,
            ratings,
            revenue,
            profit,
            topProducts,
            previousRevenue,
            previousOrders,
            window.token()
        );
        return ResponseEntity.ok(snapshot);
    }

    private static UUID parseShopId(String shopIdHeader) {
        if (shopIdHeader == null || shopIdHeader.isBlank() || "null".equalsIgnoreCase(shopIdHeader)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Caller does not own a shop (missing X-Shop-ID header)."
            );
        }
        try {
            return UUID.fromString(shopIdHeader);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid X-Shop-ID header: " + shopIdHeader);
        }
    }

    /**
     * Window resolved from a friendly range token. Both ends are inclusive of
     * "now": {@code start} is exclusive and {@code end} is the current moment.
     */
    private record DateRange(LocalDateTime start, LocalDateTime end, String token) {
        String suggestedGroupBy() {
            return switch (token) {
                case "year" -> "year";   // 12 buckets across the year
                case "quarter", "month" -> "month"; // 4 buckets across the period
                default -> "day";        // week → daily
            };
        }
    }

    private static DateRange resolveRange(String token) {
        String t = token == null ? DEFAULT_RANGE : token.toLowerCase();
        // Normalize end-of-day so totals include the current day.
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime start = switch (t) {
            case "month" -> end.minusDays(30);
            case "quarter" -> end.minusDays(90);
            case "year" -> end.minusDays(365);
            case "week", "" -> end.minusDays(7);
            default -> end.minusDays(7);
        };
        String normalized = switch (t) {
            case "month", "quarter", "year" -> t;
            default -> "week";
        };
        return new DateRange(start, end, normalized);
    }

    /** Same length window immediately preceding the supplied range. */
    private static DateRange previousWindow(DateRange w) {
        long days = ChronoUnit.DAYS.between(w.start(), w.end());
        LocalDateTime prevEnd = w.start();
        LocalDateTime prevStart = prevEnd.minusDays(days);
        return new DateRange(prevStart, prevEnd, w.token());
    }

    @PostMapping("/render")
    public ResponseEntity<CarboneResponseData> renderReport(
        @RequestParam String templateFileName,
        @RequestParam(required = false) String reportName,
        @RequestParam(defaultValue = "pdf") String convertTo,
        @RequestBody Map<String, Object> data
    ) {
        CarboneResponseData result;
        if (carboneCloudEnabled) {
            if (carboneCloudService == null) {
                throw new IllegalStateException("Carbone Cloud is enabled but service is not available. Check your API token.");
            }
            if (reportName != null && !reportName.isBlank()) {
                result = carboneCloudService.renderReport(data, templateFileName, reportName, convertTo);
            } else {
                result = carboneCloudService.renderReport(data, templateFileName, convertTo);
            }
        } else {
            if (carboneService == null) {
                throw new IllegalStateException("Carbone self-hosted is not available. Check your configuration.");
            }
            if (reportName != null && !reportName.isBlank()) {
                result = carboneService.renderReport(data, templateFileName, reportName, convertTo);
            } else {
                result = carboneService.renderReport(data, templateFileName, convertTo);
            }
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/cache/template/{templateName}")
    public ResponseEntity<Void> clearTemplateCache(@PathVariable String templateName) {
        if (carboneCloudEnabled && carboneCloudService != null) {
            carboneCloudService.clearTemplateCache(templateName);
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cache/template")
    public ResponseEntity<Void> clearAllTemplateCache() {
        if (carboneCloudEnabled && carboneCloudService != null) {
            carboneCloudService.clearAllTemplateCache();
        }
        return ResponseEntity.noContent().build();
    }
}
