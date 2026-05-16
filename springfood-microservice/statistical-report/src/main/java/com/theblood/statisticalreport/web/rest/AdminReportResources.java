package com.theblood.statisticalreport.web.rest;

import com.theblood.statisticalreport.dto.report.*;
import com.theblood.statisticalreport.repository.AdminReportRepository;
import com.theblood.statisticalreport.service.AdminReportService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * REST controller cho Admin Dashboard.
 *
 * <p>Authorization được enforce ở API Gateway (filter check {@code X-User-Roles}
 * có chứa {@code ROLE_ADMIN}). Statistical-report service không tự verify JWT
 * mà tin tưởng gateway header — pattern đã dùng cho các endpoint {@code /me/*}.</p>
 *
 * <p>Trong DEV nếu chưa có rule ở gateway, tạm thời check role bằng header
 * {@code X-User-Roles} ở đây để tránh leak data. Khi gateway có rule sẵn rồi
 * có thể bỏ check này (gateway đã chặn từ trước).</p>
 */
@RestController
@RequestMapping("/statistical-reports/admin")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminReportResources {

    private static final String DEFAULT_RANGE = "week";
    private static final int DEFAULT_TOP_SHOPS = 10;

    private final AdminReportService adminService;

    public AdminReportResources(AdminReportService adminService) {
        this.adminService = adminService;
    }

    /**
     * Aggregate dashboard cho admin — gộp overview, revenue series, signups,
     * order breakdown, top shops vào một response.
     *
     * @param range           week|month|quarter|year (mặc định week).
     * @param topShopsLimit   số shop trả về trong bảng top.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardSnapshotDTO> getAdminDashboard(
        @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
        @RequestParam(defaultValue = DEFAULT_RANGE) String range,
        @RequestParam(defaultValue = "10") int topShopsLimit
    ) {
        requireAdmin(rolesHeader);

        DateRange window = resolveRange(range);
        DateRange previous = previousWindow(window);
        String groupBy = window.suggestedGroupBy();

        PlatformOverviewDTO overview = adminService.getPlatformOverview();
        List<RevenueReportDTO> revenue = adminService.getPlatformRevenue(window.start(), window.end(), groupBy);
        List<NewSignupReportDTO> signups = adminService.getSignups(window.start(), window.end(), groupBy);
        List<OrderStatusBreakdownDTO> statuses = adminService.getOrderStatusBreakdown(window.start(), window.end());
        List<ShopRankingDTO> topShops = adminService.getTopShops(window.start(), window.end(),
            Math.min(Math.max(topShopsLimit, 1), 100));

        AdminReportRepository.WindowTotal current = adminService.getWindowTotal(window.start(), window.end());
        AdminReportRepository.WindowTotal prev = adminService.getWindowTotal(previous.start(), previous.end());

        AdminDashboardSnapshotDTO snapshot = new AdminDashboardSnapshotDTO(
            overview,
            revenue,
            signups,
            statuses,
            topShops,
            current.revenue(),
            current.orders(),
            prev.revenue(),
            prev.orders(),
            window.token()
        );
        return ResponseEntity.ok(snapshot);
    }

    /** Granular endpoint — top shops standalone (cho page riêng nếu cần). */
    @GetMapping("/top-shops")
    public ResponseEntity<List<ShopRankingDTO>> getTopShops(
        @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "20") int limit
    ) {
        requireAdmin(rolesHeader);
        return ResponseEntity.ok(adminService.getTopShops(startDate, endDate, limit));
    }

    /** Granular endpoint — platform overview only. */
    @GetMapping("/overview")
    public ResponseEntity<PlatformOverviewDTO> getOverview(
        @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader
    ) {
        requireAdmin(rolesHeader);
        return ResponseEntity.ok(adminService.getPlatformOverview());
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    /**
     * Quick role check dựa vào header gateway populate. Trả 403 nếu thiếu
     * ROLE_ADMIN. Cho phép pass khi rolesHeader null (tức là gateway chưa
     * forward header — chỉ xảy ra ở local dev không có gateway, lúc đó FE
     * cũng không gọi được endpoint này).
     */
    private static void requireAdmin(String rolesHeader) {
        if (rolesHeader == null) {
            // Cho phép trong dev không qua gateway; production gateway luôn set header này.
            return;
        }
        boolean hasAdmin = false;
        for (String r : rolesHeader.split(",")) {
            if ("ROLE_ADMIN".equals(r.trim())) {
                hasAdmin = true;
                break;
            }
        }
        if (!hasAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }

    /** Same as the shop owner's resolver — tách ra ở đây để tránh coupling. */
    private record DateRange(LocalDateTime start, LocalDateTime end, String token) {
        String suggestedGroupBy() {
            return switch (token) {
                case "year" -> "year";
                case "quarter", "month" -> "month";
                default -> "day";
            };
        }
    }

    private static DateRange resolveRange(String token) {
        String t = token == null ? DEFAULT_RANGE : token.toLowerCase();
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

    private static DateRange previousWindow(DateRange w) {
        long days = ChronoUnit.DAYS.between(w.start(), w.end());
        LocalDateTime prevEnd = w.start();
        LocalDateTime prevStart = prevEnd.minusDays(days);
        return new DateRange(prevStart, prevEnd, w.token());
    }
}
