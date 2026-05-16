package com.theblood.shopservice.repository;

import com.theblood.shopservice.dto.response.AdminShopRowResponse;
import com.theblood.shopservice.dto.response.AdminShopStatsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * JdbcTemplate-based queries cho admin shop list. Cross-schema queries
 * (orders/products) được resolve trực tiếp qua Postgres để tránh round-trip
 * tới các service khác. Trade-off: tightly coupled với schema names — nếu
 * order-service hoặc product-service đổi schema, query này phải update.
 *
 * <p>Repository này tách khỏi {@link ShopRepository} (JPA) vì JPA không tiện
 * cho dynamic filter + cross-schema aggregation.</p>
 */
@Repository
public class AdminShopReportRepository {

    private final JdbcTemplate jdbc;

    public AdminShopReportRepository(@Qualifier("jdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * List shops với stats — server-side filter theo status + search, paginate.
     *
     * @param statusFilter  null/blank/"all" → không filter; còn lại match đúng enum string.
     * @param search        substring tìm trong shop_name / email / phone / shop_id.
     */
    public List<AdminShopRowResponse> findShops(
        String statusFilter, String search, int limit, int offset
    ) {
        // ILIKE để case-insensitive; pgTrgm có thể dùng để tăng tốc nhưng
        // trong scope admin (vài nghìn shops) thì sequential scan vẫn ok.
        String sql = """
            SELECT s.shop_id,
                   s.shop_name,
                   s.logo AS logo_url,
                   s.shop_status,
                   s.email,
                   s.phone_number,
                   s.city,
                   s.province,
                   s.shop_type,
                   s.business_type,
                   s."ownerId" AS owner_id,
                   s.avg_star,
                   s.total_feedback,
                   s.shop_level,
                   s.banned_reason,
                   s.banned_at,
                   s.banned_by,
                   s.created_at,
                   s.updated_at,
                   COALESCE(stat.total_orders, 0) AS total_orders,
                   COALESCE(stat.total_revenue, 0) AS total_revenue,
                   COALESCE(prod.total_products, 0) AS total_products
            FROM springfood_shop.shops s
            LEFT JOIN LATERAL (
                SELECT COUNT(*) AS total_orders,
                       COALESCE(SUM(o.final_price), 0) AS total_revenue
                FROM springfood_order.orders o
                WHERE o.shop_id = s.shop_id
                  AND o.order_status = 'COMPLETED'
            ) stat ON true
            LEFT JOIN LATERAL (
                SELECT COUNT(*) AS total_products
                FROM springfood_product.products p
                WHERE p.shop_id = s.shop_id
            ) prod ON true
            WHERE (CAST(? AS VARCHAR) IS NULL OR s.shop_status = CAST(? AS VARCHAR))
              AND (CAST(? AS VARCHAR) IS NULL OR (
                    LOWER(s.shop_name) LIKE LOWER(CAST(? AS VARCHAR))
                 OR LOWER(s.email)     LIKE LOWER(CAST(? AS VARCHAR))
                 OR LOWER(s.phone_number) LIKE LOWER(CAST(? AS VARCHAR))
                 OR CAST(s.shop_id AS VARCHAR) LIKE CAST(? AS VARCHAR)
              ))
            ORDER BY s.created_at DESC NULLS LAST, s.shop_id
            LIMIT ? OFFSET ?
            """;

        String status = (statusFilter == null || statusFilter.isBlank() || "all".equalsIgnoreCase(statusFilter))
            ? null : statusFilter.trim().toUpperCase();
        String like = buildSearchLike(search);

        return jdbc.query(sql, rowMapper,
            status, status,
            like, like, like, like, like,
            limit, offset);
    }

    public long countShops(String statusFilter, String search) {
        String sql = """
            SELECT COUNT(*)
            FROM springfood_shop.shops s
            WHERE (CAST(? AS VARCHAR) IS NULL OR s.shop_status = CAST(? AS VARCHAR))
              AND (CAST(? AS VARCHAR) IS NULL OR (
                    LOWER(s.shop_name) LIKE LOWER(CAST(? AS VARCHAR))
                 OR LOWER(s.email)     LIKE LOWER(CAST(? AS VARCHAR))
                 OR LOWER(s.phone_number) LIKE LOWER(CAST(? AS VARCHAR))
                 OR CAST(s.shop_id AS VARCHAR) LIKE CAST(? AS VARCHAR)
              ))
            """;
        String status = (statusFilter == null || statusFilter.isBlank() || "all".equalsIgnoreCase(statusFilter))
            ? null : statusFilter.trim().toUpperCase();
        String like = buildSearchLike(search);
        Long total = jdbc.queryForObject(sql, Long.class,
            status, status, like, like, like, like, like);
        return total != null ? total : 0L;
    }

    public AdminShopRowResponse findOneById(UUID shopId) {
        String sql = """
            SELECT s.shop_id,
                   s.shop_name,
                   s.logo AS logo_url,
                   s.shop_status,
                   s.email,
                   s.phone_number,
                   s.city,
                   s.province,
                   s.shop_type,
                   s.business_type,
                   s."ownerId" AS owner_id,
                   s.avg_star,
                   s.total_feedback,
                   s.shop_level,
                   s.banned_reason,
                   s.banned_at,
                   s.banned_by,
                   s.created_at,
                   s.updated_at,
                   COALESCE(stat.total_orders, 0) AS total_orders,
                   COALESCE(stat.total_revenue, 0) AS total_revenue,
                   COALESCE(prod.total_products, 0) AS total_products
            FROM springfood_shop.shops s
            LEFT JOIN LATERAL (
                SELECT COUNT(*) AS total_orders,
                       COALESCE(SUM(o.final_price), 0) AS total_revenue
                FROM springfood_order.orders o
                WHERE o.shop_id = s.shop_id
                  AND o.order_status = 'COMPLETED'
            ) stat ON true
            LEFT JOIN LATERAL (
                SELECT COUNT(*) AS total_products
                FROM springfood_product.products p
                WHERE p.shop_id = s.shop_id
            ) prod ON true
            WHERE s.shop_id = CAST(? AS UUID)
            """;
        var rows = jdbc.query(sql, rowMapper, shopId.toString());
        return rows.isEmpty() ? null : rows.get(0);
    }

    public AdminShopStatsResponse getStats() {
        String sql = """
            SELECT
              (SELECT COUNT(*) FROM springfood_shop.shops
                 WHERE shop_status IN ('ACTIVE','INACTIVE','BANNED','CLOSED')) AS total_shops,
              (SELECT COUNT(*) FROM springfood_shop.shops WHERE shop_status = 'ACTIVE') AS active_shops,
              (SELECT COUNT(*) FROM springfood_shop.shops WHERE shop_status = 'INACTIVE') AS inactive_shops,
              (SELECT COUNT(*) FROM springfood_shop.shops WHERE shop_status = 'BANNED') AS banned_shops,
              (SELECT COUNT(*) FROM springfood_shop.shops WHERE shop_status = 'CLOSED') AS closed_shops,
              (SELECT COUNT(*) FROM springfood_shop.shops WHERE shop_status = 'PENDING_APPROVAL') AS pending_shops,
              (SELECT COALESCE(SUM(final_price), 0) FROM springfood_order.orders
                 WHERE order_status = 'COMPLETED') AS total_gmv,
              (SELECT COUNT(*) FROM springfood_shop.shops
                 WHERE created_at >= NOW() - INTERVAL '30 days') AS new_shops_last30
            """;
        return jdbc.queryForObject(sql, (rs, rn) -> new AdminShopStatsResponse(
            rs.getLong("total_shops"),
            rs.getLong("active_shops"),
            rs.getLong("inactive_shops"),
            rs.getLong("banned_shops"),
            rs.getLong("closed_shops"),
            rs.getLong("pending_shops"),
            rs.getBigDecimal("total_gmv") != null ? rs.getBigDecimal("total_gmv") : BigDecimal.ZERO,
            rs.getLong("new_shops_last30")
        ));
    }

    private static String buildSearchLike(String search) {
        if (search == null || search.isBlank()) return null;
        return "%" + search.trim() + "%";
    }

    private final RowMapper<AdminShopRowResponse> rowMapper = (rs, rn) -> {
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        Timestamp bannedAt = rs.getTimestamp("banned_at");
        String shopIdStr = rs.getString("shop_id");
        return AdminShopRowResponse.builder()
            .shopId(shopIdStr != null ? UUID.fromString(shopIdStr) : null)
            .shopName(rs.getString("shop_name"))
            .logoUrl(rs.getString("logo_url"))
            .shopStatus(rs.getString("shop_status"))
            .email(rs.getString("email"))
            .phoneNumber(rs.getString("phone_number"))
            .city(rs.getString("city"))
            .province(rs.getString("province"))
            .shopType(rs.getString("shop_type"))
            .businessType(rs.getString("business_type"))
            .ownerId(rs.getString("owner_id"))
            .avgStar(rs.getBigDecimal("avg_star"))
            .totalFeedback(rs.getObject("total_feedback") != null ? rs.getInt("total_feedback") : null)
            .shopLevel(rs.getObject("shop_level") != null ? rs.getInt("shop_level") : null)
            .bannedReason(rs.getString("banned_reason"))
            .bannedAt(bannedAt != null ? bannedAt.toInstant() : null)
            .bannedBy(rs.getString("banned_by"))
            .totalOrders(rs.getLong("total_orders"))
            .totalRevenue(rs.getBigDecimal("total_revenue") != null
                ? rs.getBigDecimal("total_revenue") : BigDecimal.ZERO)
            .totalProducts(rs.getLong("total_products"))
            .createdAt(created != null ? created.toInstant() : null)
            .updatedAt(updated != null ? updated.toInstant() : null)
            .build();
    };
}
