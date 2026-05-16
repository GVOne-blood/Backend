package com.theblood.statisticalreport.repository;

import com.theblood.statisticalreport.dto.report.NewSignupReportDTO;
import com.theblood.statisticalreport.dto.report.OrderStatusBreakdownDTO;
import com.theblood.statisticalreport.dto.report.PlatformOverviewDTO;
import com.theblood.statisticalreport.dto.report.RevenueReportDTO;
import com.theblood.statisticalreport.dto.report.ShopRankingDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository cho các query "platform-wide" mà chỉ ADMIN cần.
 *
 * <p>Tất cả query đều cross-schema — pgvector của Postgres cho phép join giữa
 * {@code springfood_order}, {@code springfood_product}, {@code springfood_shop},
 * {@code springfood_authentication} miễn là cùng một database. Đây là lý do tại
 * sao service này dùng JdbcTemplate trực tiếp thay vì JPA — tránh phải khai báo
 * entity rời rạc cho mỗi schema.</p>
 *
 * <p>Các status string ('COMPLETED', 'PENDING'…) phải khớp với enum
 * {@code OrderStatus} ở order-service. Nếu BE đổi tên enum thì query này cũng
 * phải update — nhược điểm tất yếu của join cross-schema bằng SQL thay vì gRPC.</p>
 */
@Repository
public class AdminReportRepository {

    private final JdbcTemplate jdbc;

    public AdminReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ------------------------------------------------------------------
    // Platform overview — single round-trip với nhiều scalar subquery.
    // ------------------------------------------------------------------
    public PlatformOverviewDTO findPlatformOverview() {
        // Mỗi subquery độc lập về performance — chấp nhận đánh đổi để đỡ
        // build join phức tạp. Postgres planner cũng inline tốt subquery scalar.
        //
        // Status enums (xem com.theblood.shopservice.common.enums.ShopStatus):
        //   PENDING_APPROVAL, ACTIVE, INACTIVE, BANNED, CLOSED
        // Đối với "tổng shop" ta tính shops đã được duyệt (ACTIVE/INACTIVE/CLOSED/BANNED đã có
        // record hợp lệ); loại PENDING_APPROVAL.
        //
        // RegistrationRequest status (string column): DRAFT, PENDING, APPROVED, REJECTED
        String sql = """
            SELECT
              (SELECT COUNT(*) FROM springfood_shop.shops
                 WHERE shop_status IN ('ACTIVE','INACTIVE','CLOSED','BANNED')) AS total_shops,
              (SELECT COUNT(*) FROM springfood_shop.shops
                 WHERE shop_status = 'ACTIVE') AS active_shops,
              (SELECT COUNT(*) FROM springfood_shop.shops
                 WHERE shop_status = 'PENDING_APPROVAL') AS pending_shops,
              (SELECT COUNT(*) FROM springfood_shop.shops
                 WHERE shop_status IN ('INACTIVE','BANNED','CLOSED')) AS suspended_shops,
              (SELECT COUNT(*) FROM springfood_authentication."user"
                 WHERE COALESCE(is_deleted, false) = false
                   AND status <> 'BANNED') AS total_users,
              (SELECT COUNT(DISTINCT uhr.user_id)
                 FROM springfood_authentication.user_has_role uhr
                 JOIN springfood_authentication.role r ON r.role_id = uhr.role_id
                 WHERE r.name = 'CUSTOMER') AS total_customers,
              (SELECT COUNT(DISTINCT uhr.user_id)
                 FROM springfood_authentication.user_has_role uhr
                 JOIN springfood_authentication.role r ON r.role_id = uhr.role_id
                 WHERE r.name = 'SHOP_OWNER') AS total_shop_owners,
              (SELECT COUNT(*) FROM springfood_product.products) AS total_products,
              (SELECT COUNT(*) FROM springfood_order.orders
                 WHERE order_status = 'COMPLETED') AS total_orders,
              (SELECT COALESCE(SUM(final_price), 0) FROM springfood_order.orders
                 WHERE order_status = 'COMPLETED') AS total_revenue,
              (SELECT COUNT(*) FROM springfood_authentication.shop_registration_request
                 WHERE status IN ('PENDING','DRAFT')) AS pending_regs
            """;

        return jdbc.queryForObject(sql, (rs, rn) -> new PlatformOverviewDTO(
            rs.getLong("total_shops"),
            rs.getLong("active_shops"),
            rs.getLong("pending_shops"),
            rs.getLong("suspended_shops"),
            rs.getLong("total_users"),
            rs.getLong("total_customers"),
            rs.getLong("total_shop_owners"),
            rs.getLong("total_products"),
            rs.getLong("total_orders"),
            rs.getBigDecimal("total_revenue"),
            rs.getLong("pending_regs")
        ));
    }

    // ------------------------------------------------------------------
    // Revenue across the entire platform (no shop filter).
    // ------------------------------------------------------------------
    public List<RevenueReportDTO> findPlatformRevenueByDay(LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT DATE_TRUNC('day', o.created_at) AS period,
                   COALESCE(SUM(o.final_price), 0) AS revenue,
                   COUNT(DISTINCT o.order_id) AS order_count,
                   COALESCE(AVG(o.final_price), 0) AS avg_value
            FROM springfood_order.orders o
            WHERE o.created_at >= ?
              AND o.created_at < ?
              AND o.order_status = 'COMPLETED'
            GROUP BY DATE_TRUNC('day', o.created_at)
            ORDER BY period
            """;
        return jdbc.query(sql, revenueMapper, start, end);
    }

    public List<RevenueReportDTO> findPlatformRevenueByBuckets(LocalDateTime start, LocalDateTime end, int buckets) {
        String sql = """
            WITH range AS (
                SELECT ?::timestamp AS s, ?::timestamp AS e
            ),
            grid AS (
                SELECT n AS idx,
                       s + (e - s) * (n - 1) / ? AS bucket_start,
                       s + (e - s) * n / ? AS bucket_end
                FROM range, LATERAL (SELECT generate_series(1, ?) AS n) ser
            )
            SELECT g.bucket_start AS period,
                   COALESCE(SUM(o.final_price), 0) AS revenue,
                   COUNT(DISTINCT o.order_id) AS order_count,
                   COALESCE(AVG(o.final_price), 0) AS avg_value
            FROM grid g
            LEFT JOIN springfood_order.orders o
                ON o.created_at >= g.bucket_start
                AND o.created_at < g.bucket_end
                AND o.order_status = 'COMPLETED'
            GROUP BY g.idx, g.bucket_start
            ORDER BY g.idx
            """;
        return jdbc.query(sql, revenueMapper, start, end, buckets, buckets, buckets);
    }

    // ------------------------------------------------------------------
    // New signups (users + shops) — cùng grid.
    // ------------------------------------------------------------------
    public List<NewSignupReportDTO> findSignupsByDay(LocalDateTime start, LocalDateTime end) {
        String sql = """
            WITH days AS (
                SELECT generate_series(
                    DATE_TRUNC('day', ?::timestamp),
                    DATE_TRUNC('day', ?::timestamp - INTERVAL '1 day'),
                    INTERVAL '1 day'
                ) AS day
            )
            SELECT d.day AS period,
                   (SELECT COUNT(*) FROM springfood_authentication."user" u
                       WHERE u.created_at >= d.day AND u.created_at < d.day + INTERVAL '1 day') AS new_users,
                   (SELECT COUNT(*) FROM springfood_shop.shops s
                       WHERE s.created_at >= d.day AND s.created_at < d.day + INTERVAL '1 day') AS new_shops
            FROM days d
            ORDER BY d.day
            """;
        return jdbc.query(sql, signupMapper, start, end);
    }

    public List<NewSignupReportDTO> findSignupsByBuckets(LocalDateTime start, LocalDateTime end, int buckets) {
        String sql = """
            WITH range AS (
                SELECT ?::timestamp AS s, ?::timestamp AS e
            ),
            grid AS (
                SELECT n AS idx,
                       s + (e - s) * (n - 1) / ? AS bucket_start,
                       s + (e - s) * n / ? AS bucket_end
                FROM range, LATERAL (SELECT generate_series(1, ?) AS n) ser
            )
            SELECT g.bucket_start AS period,
                   (SELECT COUNT(*) FROM springfood_authentication."user" u
                       WHERE u.created_at >= g.bucket_start AND u.created_at < g.bucket_end) AS new_users,
                   (SELECT COUNT(*) FROM springfood_shop.shops s
                       WHERE s.created_at >= g.bucket_start AND s.created_at < g.bucket_end) AS new_shops
            FROM grid g
            ORDER BY g.idx
            """;
        return jdbc.query(sql, signupMapper, start, end, buckets, buckets, buckets);
    }

    // ------------------------------------------------------------------
    // Order status breakdown trong window (không lọc shop).
    // ------------------------------------------------------------------
    public List<OrderStatusBreakdownDTO> findOrderStatusBreakdown(LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT o.order_status AS status,
                   COUNT(*) AS cnt
            FROM springfood_order.orders o
            WHERE o.created_at >= ?
              AND o.created_at < ?
            GROUP BY o.order_status
            ORDER BY cnt DESC
            """;
        return jdbc.query(sql, (rs, rn) -> new OrderStatusBreakdownDTO(
            rs.getString("status"),
            rs.getLong("cnt")
        ), start, end);
    }

    // ------------------------------------------------------------------
    // Top shops theo revenue trong window.
    // ------------------------------------------------------------------
    public List<ShopRankingDTO> findTopShops(LocalDateTime start, LocalDateTime end, int limit) {
        // Avg rating join từ shops.avg_star (đã được denormalize) cho khoẻ;
        // nếu không có cột đó thì fallback về AVG(feedbacks.rating).
        String sql = """
            SELECT o.shop_id,
                   COALESCE(s.shop_name, 'Unknown') AS shop_name,
                   s.logo_url,
                   COALESCE(SUM(o.final_price), 0) AS revenue,
                   COUNT(DISTINCT o.order_id) AS order_count,
                   COALESCE(s.avg_star,
                       (SELECT AVG(f.rating) FROM springfood_product.feedbacks f
                          WHERE f.shop_id = o.shop_id AND f.is_active = true)
                   , 0) AS avg_rating
            FROM springfood_order.orders o
            LEFT JOIN springfood_shop.shops s ON s.shop_id = o.shop_id
            WHERE o.order_status = 'COMPLETED'
              AND o.created_at >= ?
              AND o.created_at < ?
            GROUP BY o.shop_id, s.shop_name, s.logo_url, s.avg_star
            ORDER BY revenue DESC
            LIMIT ?
            """;
        return jdbc.query(sql, shopRankingMapper, start, end, limit);
    }

    // ------------------------------------------------------------------
    // Tổng revenue + đơn trong window — dùng cho trend so với cùng kỳ.
    // ------------------------------------------------------------------
    public WindowTotal findWindowTotal(LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT COALESCE(SUM(o.final_price), 0) AS revenue,
                   COUNT(DISTINCT o.order_id) AS order_count
            FROM springfood_order.orders o
            WHERE o.created_at >= ?
              AND o.created_at < ?
              AND o.order_status = 'COMPLETED'
            """;
        return jdbc.queryForObject(sql, (rs, rn) -> new WindowTotal(
            rs.getBigDecimal("revenue"),
            rs.getLong("order_count")
        ), start, end);
    }

    public record WindowTotal(BigDecimal revenue, long orders) {}

    // ------------------------------------------------------------------
    // Mappers
    // ------------------------------------------------------------------
    private final RowMapper<RevenueReportDTO> revenueMapper = (rs, rn) -> new RevenueReportDTO(
        rs.getTimestamp("period").toLocalDateTime(),
        rs.getBigDecimal("revenue"),
        rs.getLong("order_count"),
        rs.getBigDecimal("avg_value")
    );

    private final RowMapper<NewSignupReportDTO> signupMapper = (rs, rn) -> new NewSignupReportDTO(
        rs.getTimestamp("period").toLocalDateTime(),
        rs.getLong("new_users"),
        rs.getLong("new_shops")
    );

    private final RowMapper<ShopRankingDTO> shopRankingMapper = (rs, rn) -> {
        // shop_id có thể là null trong join hợp lệ (nếu order trỏ shop bị xóa) → bỏ qua.
        String sid = rs.getString("shop_id");
        return new ShopRankingDTO(
            sid != null ? UUID.fromString(sid) : null,
            rs.getString("shop_name"),
            rs.getString("logo_url"),
            rs.getBigDecimal("revenue"),
            rs.getLong("order_count"),
            rs.getObject("avg_rating") != null ? rs.getDouble("avg_rating") : null
        );
    };
}
