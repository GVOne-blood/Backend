package com.theblood.statisticalreport.repository;

import com.theblood.statisticalreport.dto.report.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class StatisticalReportRepository {

    private final JdbcTemplate jdbc;

    public StatisticalReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<RevenueReportDTO> findRevenueByDay(UUID shopId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT DATE_TRUNC('day', o.created_at) AS period,
                   COALESCE(SUM(o.final_price), 0) AS revenue,
                   COUNT(DISTINCT o.order_id) AS order_count,
                   COALESCE(AVG(o.final_price), 0) AS avg_value
            FROM springfood_order.orders o
            WHERE o.shop_id = CAST(? AS UUID)
              AND o.created_at >= ?
              AND o.created_at < ?
              AND o.order_status = 'COMPLETED'
            GROUP BY DATE_TRUNC('day', o.created_at)
            ORDER BY period
            """;
        return jdbc.query(sql, revenueMapper, shopId.toString(), start, end);
    }

    public List<RevenueReportDTO> findRevenueByBuckets(UUID shopId, LocalDateTime start, LocalDateTime end, int buckets) {
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
                AND o.shop_id = CAST(? AS UUID)
                AND o.order_status = 'COMPLETED'
            GROUP BY g.idx, g.bucket_start
            ORDER BY g.idx
            """;
        return jdbc.query(sql, revenueMapper, start, end, buckets, buckets, buckets, shopId.toString());
    }

    public OrderSuccessRateDTO findOrderSuccessRate(UUID shopId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT COUNT(*) AS total,
                   COUNT(*) FILTER (WHERE o.order_status = 'COMPLETED') AS completed,
                   COUNT(*) FILTER (WHERE o.order_status IN ('FAILED','DELETED')) AS failed,
                   COUNT(*) FILTER (WHERE o.order_status IN ('CANCELLED','PENDING','PENDING_PAYMENT')) AS cancelled,
                   COUNT(*) FILTER (WHERE o.order_status = 'ORDER_RETURN') AS returned,
                   CASE WHEN COUNT(*) > 0
                        THEN ROUND(100.0 * COUNT(*) FILTER (WHERE o.order_status = 'COMPLETED') / COUNT(*), 2)
                        ELSE 0 END AS rate
            FROM springfood_order.orders o
            WHERE o.shop_id = CAST(? AS UUID)
              AND o.created_at >= ?
              AND o.created_at < ?
            """;
        return jdbc.queryForObject(sql, successRateMapper, shopId.toString(), start, end);
    }

    public List<ProfitReportDTO> findProfitByDay(UUID shopId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT DATE_TRUNC('day', o.created_at) AS period,
                   COALESCE(SUM(o.final_price), 0) AS gross,
                   COALESCE(SUM(o.shipping_fee), 0) AS shipping,
                   COALESCE(SUM(o.discount_amount), 0) AS discount,
                   COALESCE(SUM(o.final_price - o.shipping_fee - o.discount_amount), 0) AS profit
            FROM springfood_order.orders o
            WHERE o.shop_id = CAST(? AS UUID)
              AND o.created_at >= ?
              AND o.created_at < ?
              AND o.order_status = 'COMPLETED'
            GROUP BY DATE_TRUNC('day', o.created_at)
            ORDER BY period
            """;
        return jdbc.query(sql, profitMapper, shopId.toString(), start, end);
    }

    public List<ProfitReportDTO> findProfitByBuckets(UUID shopId, LocalDateTime start, LocalDateTime end, int buckets) {
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
                   COALESCE(SUM(o.final_price), 0) AS gross,
                   COALESCE(SUM(o.shipping_fee), 0) AS shipping,
                   COALESCE(SUM(o.discount_amount), 0) AS discount,
                   COALESCE(SUM(o.final_price - o.shipping_fee - o.discount_amount), 0) AS profit
            FROM grid g
            LEFT JOIN springfood_order.orders o
                ON o.created_at >= g.bucket_start
                AND o.created_at < g.bucket_end
                AND o.shop_id = CAST(? AS UUID)
                AND o.order_status = 'COMPLETED'
            GROUP BY g.idx, g.bucket_start
            ORDER BY g.idx
            """;
        return jdbc.query(sql, profitMapper, start, end, buckets, buckets, buckets, shopId.toString());
    }

    public RatingReportDTO findRatingByShop(UUID shopId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT COALESCE(AVG(f.rating), 0) AS avg_rating,
                   COUNT(*) AS total,
                   COUNT(*) FILTER (WHERE f.rating = 1) AS s1,
                   COUNT(*) FILTER (WHERE f.rating = 2) AS s2,
                   COUNT(*) FILTER (WHERE f.rating = 3) AS s3,
                   COUNT(*) FILTER (WHERE f.rating = 4) AS s4,
                   COUNT(*) FILTER (WHERE f.rating = 5) AS s5
            FROM springfood_product.feedbacks f
            WHERE f.shop_id = CAST(? AS UUID)
              AND f.created_at >= ?
              AND f.created_at < ?
              AND f.is_active = true
            """;
        return jdbc.queryForObject(sql, ratingMapper, shopId.toString(), start, end);
    }

    public ShopOverviewDTO findShopOverview(UUID shopId) {
        String sql = """
            SELECT (SELECT COUNT(*) FROM springfood_order.orders
                    WHERE shop_id = CAST(? AS UUID) AND order_status = 'COMPLETED') AS total_orders,
                   (SELECT COALESCE(SUM(final_price), 0) FROM springfood_order.orders
                    WHERE shop_id = CAST(? AS UUID) AND order_status = 'COMPLETED') AS total_revenue,
                   (SELECT COUNT(*) FROM springfood_product.products
                    WHERE shop_id = CAST(? AS UUID)) AS total_products,
                   (SELECT COALESCE(AVG(rating), 0) FROM springfood_product.feedbacks
                    WHERE shop_id = CAST(? AS UUID) AND is_active = true) AS avg_rating,
                   (SELECT COUNT(*) FROM springfood_product.feedbacks
                    WHERE shop_id = CAST(? AS UUID) AND is_active = true) AS total_feedbacks,
                   (SELECT COUNT(DISTINCT user_id) FROM springfood_order.orders
                    WHERE shop_id = CAST(? AS UUID)) AS total_customers
            """;
        return jdbc.queryForObject(sql, overviewMapper,
            shopId.toString(), shopId.toString(), shopId.toString(),
            shopId.toString(), shopId.toString(), shopId.toString());
    }

    public List<TopProductDTO> findTopProducts(UUID shopId, LocalDateTime start, LocalDateTime end, int limit) {
        // Extract first element from `products.images` jsonb. Supports the schemes
        // we have in the wild:
        //   - array of strings: ["https://..."]
        //   - array of objects with `url` field: [{"url": "https://..."}]
        // Falls back to NULL when neither is present.
        String sql = """
            SELECT p.product_id,
                   p.name AS product_name,
                   p.sku,
                   COALESCE(
                       p.images->0->>'url',
                       p.images->>0
                   ) AS image_url,
                   p.quantity AS stock_quantity,
                   COALESCE(SUM(oi.quantity), 0) AS qty,
                   COALESCE(SUM(oi.price_at_booking * oi.quantity), 0) AS revenue
            FROM springfood_order.order_items oi
            JOIN springfood_order.orders o ON oi.order_id = o.order_id
            JOIN springfood_product.products p ON oi.product_id = p.product_id
            WHERE o.shop_id = CAST(? AS UUID)
              AND o.order_status = 'COMPLETED'
              AND o.created_at >= ?
              AND o.created_at < ?
            GROUP BY p.product_id, p.name, p.sku, p.images, p.quantity
            ORDER BY qty DESC
            LIMIT ?
            """;
        return jdbc.query(sql, topProductMapper, shopId.toString(), start, end, limit);
    }

    public List<PlatformCommissionDTO> findCommissionByDay(UUID shopId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            WITH rate AS (
                SELECT COALESCE(pc.percent_rate, 5.00) AS pct,
                       COALESCE(pc.commission_type, 'PERCENTAGE') AS type,
                       COALESCE(pc.flat_amount, 0) AS flat,
                       COALESCE(pc.min_commission, 0) AS min_c,
                       COALESCE(pc.max_commission, 999999999) AS max_c
                FROM springfood_shop.platform_commission_config pc
                WHERE pc.is_active = true
                  AND pc.effective_from <= NOW()
                  AND (pc.effective_to IS NULL OR pc.effective_to > NOW())
                ORDER BY pc.effective_from DESC LIMIT 1
            )
            SELECT DATE_TRUNC('day', o.created_at) AS period,
                   COALESCE(SUM(
                       CASE r.type
                           WHEN 'FLAT' THEN r.flat
                           WHEN 'HYBRID' THEN LEAST(GREATEST(o.final_price * r.pct / 100, r.flat), r.max_c)
                           ELSE LEAST(GREATEST(o.final_price * r.pct / 100, r.min_c), r.max_c)
                       END
                   ), 0) AS commission,
                   COUNT(DISTINCT o.order_id) AS order_count
            FROM springfood_order.orders o
            CROSS JOIN rate r
            WHERE o.order_status = 'COMPLETED'
              AND (? IS NULL OR o.shop_id = CAST(? AS UUID))
              AND o.created_at >= ?
              AND o.created_at < ?
            GROUP BY DATE_TRUNC('day', o.created_at)
            ORDER BY period
            """;
        String sid = shopId != null ? shopId.toString() : null;
        return jdbc.query(sql, commissionMapper, sid, sid, start, end);
    }

    public List<PlatformCommissionDTO> findCommissionByBuckets(UUID shopId, LocalDateTime start, LocalDateTime end, int buckets) {
        String sql = """
            WITH rate AS (
                SELECT COALESCE(pc.percent_rate, 5.00) AS pct,
                       COALESCE(pc.commission_type, 'PERCENTAGE') AS type,
                       COALESCE(pc.flat_amount, 0) AS flat,
                       COALESCE(pc.min_commission, 0) AS min_c,
                       COALESCE(pc.max_commission, 999999999) AS max_c
                FROM springfood_shop.platform_commission_config pc
                WHERE pc.is_active = true
                  AND pc.effective_from <= NOW()
                  AND (pc.effective_to IS NULL OR pc.effective_to > NOW())
                ORDER BY pc.effective_from DESC LIMIT 1
            ),
            range AS (
                SELECT ?::timestamp AS s, ?::timestamp AS e
            ),
            grid AS (
                SELECT n AS idx,
                       s + (e - s) * (n - 1) / ? AS bucket_start,
                       s + (e - s) * n / ? AS bucket_end
                FROM range, LATERAL (SELECT generate_series(1, ?) AS n) ser
            )
            SELECT g.bucket_start AS period,
                   COALESCE(SUM(
                       CASE r.type
                           WHEN 'FLAT' THEN r.flat
                           WHEN 'HYBRID' THEN LEAST(GREATEST(o.final_price * r.pct / 100, r.flat), r.max_c)
                           ELSE LEAST(GREATEST(o.final_price * r.pct / 100, r.min_c), r.max_c)
                       END
                   ), 0) AS commission,
                   COUNT(DISTINCT o.order_id) AS order_count
            FROM grid g
            CROSS JOIN rate r
            LEFT JOIN springfood_order.orders o
                ON o.created_at >= g.bucket_start
                AND o.created_at < g.bucket_end
                AND o.order_status = 'COMPLETED'
                AND (? IS NULL OR o.shop_id = CAST(? AS UUID))
            GROUP BY g.idx, g.bucket_start
            ORDER BY g.idx
            """;
        String sid = shopId != null ? shopId.toString() : null;
        return jdbc.query(sql, commissionMapper, start, end, buckets, buckets, buckets, sid, sid);
    }

    public List<ShopCommissionDTO> findCommissionByShop(LocalDateTime start, LocalDateTime end) {
        String sql = """
            WITH rate AS (
                SELECT COALESCE(pc.percent_rate, 5.00) AS pct,
                       COALESCE(pc.commission_type, 'PERCENTAGE') AS type,
                       COALESCE(pc.flat_amount, 0) AS flat,
                       COALESCE(pc.min_commission, 0) AS min_c,
                       COALESCE(pc.max_commission, 999999999) AS max_c
                FROM springfood_shop.platform_commission_config pc
                WHERE pc.is_active = true
                  AND pc.effective_from <= NOW()
                  AND (pc.effective_to IS NULL OR pc.effective_to > NOW())
                ORDER BY pc.effective_from DESC LIMIT 1
            )
            SELECT o.shop_id,
                   COALESCE(s.shop_name, 'Unknown') AS shop_name,
                   COALESCE(SUM(
                       CASE r.type
                           WHEN 'FLAT' THEN r.flat
                           WHEN 'HYBRID' THEN LEAST(GREATEST(o.final_price * r.pct / 100, r.flat), r.max_c)
                           ELSE LEAST(GREATEST(o.final_price * r.pct / 100, r.min_c), r.max_c)
                       END
                   ), 0) AS commission,
                   COUNT(DISTINCT o.order_id) AS order_count
            FROM springfood_order.orders o
            CROSS JOIN rate r
            LEFT JOIN springfood_shop.shops s ON s.shop_id = o.shop_id
            WHERE o.order_status = 'COMPLETED'
              AND o.created_at >= ?
              AND o.created_at < ?
            GROUP BY o.shop_id, s.shop_name
            ORDER BY commission DESC
            """;
        return jdbc.query(sql, shopCommissionMapper, start, end);
    }

    private final RowMapper<RevenueReportDTO> revenueMapper = (rs, rowNum) ->
        new RevenueReportDTO(
            rs.getTimestamp("period").toLocalDateTime(),
            rs.getBigDecimal("revenue"),
            rs.getLong("order_count"),
            rs.getBigDecimal("avg_value")
        );

    private final RowMapper<OrderSuccessRateDTO> successRateMapper = (rs, rowNum) ->
        new OrderSuccessRateDTO(
            rs.getLong("total"),
            rs.getLong("completed"),
            rs.getLong("failed"),
            rs.getLong("cancelled"),
            rs.getLong("returned"),
            rs.getDouble("rate")
        );

    private final RowMapper<ProfitReportDTO> profitMapper = (rs, rowNum) ->
        new ProfitReportDTO(
            rs.getTimestamp("period").toLocalDateTime(),
            rs.getBigDecimal("gross"),
            rs.getBigDecimal("shipping"),
            rs.getBigDecimal("discount"),
            rs.getBigDecimal("profit")
        );

    private final RowMapper<RatingReportDTO> ratingMapper = (rs, rowNum) ->
        new RatingReportDTO(
            rs.getDouble("avg_rating"),
            rs.getLong("total"),
            rs.getLong("s1"),
            rs.getLong("s2"),
            rs.getLong("s3"),
            rs.getLong("s4"),
            rs.getLong("s5")
        );

    private final RowMapper<ShopOverviewDTO> overviewMapper = (rs, rowNum) ->
        new ShopOverviewDTO(
            rs.getLong("total_orders"),
            rs.getBigDecimal("total_revenue"),
            rs.getLong("total_products"),
            rs.getDouble("avg_rating"),
            rs.getLong("total_feedbacks"),
            rs.getLong("total_customers")
        );

    private final RowMapper<TopProductDTO> topProductMapper = (rs, rowNum) ->
        new TopProductDTO(
            UUID.fromString(rs.getString("product_id")),
            rs.getString("product_name"),
            rs.getString("sku"),
            rs.getString("image_url"),
            rs.getObject("stock_quantity") != null ? rs.getLong("stock_quantity") : null,
            rs.getLong("qty"),
            rs.getBigDecimal("revenue")
        );

    private final RowMapper<PlatformCommissionDTO> commissionMapper = (rs, rowNum) ->
        new PlatformCommissionDTO(
            rs.getTimestamp("period").toLocalDateTime(),
            rs.getBigDecimal("commission"),
            rs.getLong("order_count")
        );

    private final RowMapper<ShopCommissionDTO> shopCommissionMapper = (rs, rowNum) ->
        new ShopCommissionDTO(
            UUID.fromString(rs.getString("shop_id")),
            rs.getString("shop_name"),
            rs.getBigDecimal("commission"),
            rs.getLong("order_count")
        );
}
