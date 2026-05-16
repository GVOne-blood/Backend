package com.theblood.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Đọc trực tiếp bảng <code>springfood_order.orders</code> qua JdbcTemplate
 * (không dùng JPA để tránh Hibernate ddl-auto={@code update} mò sang schema
 * khác và tạo bảng nhầm).
 *
 * <p>Service này chỉ READ-ONLY. Mọi mutation phải đi qua order-service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderQueryService {

    private final JdbcTemplate jdbcTemplate;

    public Optional<OrderSnapshot> findById(UUID orderId) {
        try {
            OrderSnapshot snapshot = jdbcTemplate.queryForObject(
                    """
                            SELECT
                              order_id,
                              user_id,
                              shop_id,
                              order_status,
                              payment_status,
                              final_price,
                              payment_method_name
                            FROM springfood_order.orders
                            WHERE order_id = ?
                            """,
                    (rs, rowNum) -> new OrderSnapshot(
                            rs.getObject("order_id", UUID.class),
                            rs.getObject("user_id", UUID.class),
                            rs.getObject("shop_id", UUID.class),
                            rs.getString("order_status"),
                            rs.getString("payment_status"),
                            rs.getBigDecimal("final_price"),
                            rs.getString("payment_method_name")
                    ),
                    orderId);
            return Optional.ofNullable(snapshot);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        } catch (Exception ex) {
            log.error("Error querying springfood_order.orders for id={}: {}",
                    orderId, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Snapshot read-only của 1 order, đủ để build VNPay URL.
     */
    public record OrderSnapshot(
            UUID orderId,
            UUID userId,
            UUID shopId,
            String orderStatus,
            String paymentStatus,
            BigDecimal finalPrice,
            String paymentMethodName
    ) {}
}
