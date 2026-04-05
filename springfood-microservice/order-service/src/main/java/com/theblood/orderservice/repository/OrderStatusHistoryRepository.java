package com.theblood.orderservice.repository;

import com.theblood.orderservice.domain.OrderStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for OrderStatusHistory entity.
 */
@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {

    /**
     * Find status history for an order
     */
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.orderId = :orderId ORDER BY osh.createdAt ASC")
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAt(@Param("orderId") UUID orderId);

    /**
     * Find latest status change for an order
     */
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.orderId = :orderId ORDER BY osh.createdAt DESC LIMIT 1")
    OrderStatusHistory findLatestByOrderId(@Param("orderId") UUID orderId);

    /**
     * Find status changes by role (for analytics)
     */
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.changedRole = :role ORDER BY osh.createdAt DESC")
    Page<OrderStatusHistory> findByChangedRole(@Param("role") String role, Pageable pageable);

    /**
     * Find status changes by specific user
     */
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.changedBy = :changedBy ORDER BY osh.createdAt DESC")
    List<OrderStatusHistory> findByChangedBy(@Param("changedBy") String changedBy);

    /**
     * Find status changes within time range
     */
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.createdAt BETWEEN :startTime AND :endTime ORDER BY osh.createdAt DESC")
    List<OrderStatusHistory> findByCreatedAtBetween(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    /**
     * Find orders that changed to specific status
     */
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.toStatus = :status ORDER BY osh.createdAt DESC")
    Page<OrderStatusHistory> findByToStatus(@Param("status") String status, Pageable pageable);

    /**
     * Find orders that changed from specific status
     */
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.fromStatus = :status ORDER BY osh.createdAt DESC")
    List<OrderStatusHistory> findByFromStatus(@Param("status") String status);

    /**
     * Count status changes for an order
     */
    long countByOrderId(UUID orderId);

    /**
     * Get status change statistics (for dashboard)
     */
    @Query("SELECT osh.toStatus, COUNT(osh) FROM OrderStatusHistory osh " +
           "WHERE osh.createdAt >= :since GROUP BY osh.toStatus ORDER BY COUNT(osh) DESC")
    List<Object[]> getStatusChangeStats(@Param("since") Instant since);

    /**
     * Find orders with status changes by multiple orders
     */
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.orderId IN :orderIds ORDER BY osh.orderId, osh.createdAt ASC")
    List<OrderStatusHistory> findByOrderIdIn(@Param("orderIds") List<UUID> orderIds);

    /**
     * Check if order has specific status in history
     */
    @Query("SELECT COUNT(osh) > 0 FROM OrderStatusHistory osh WHERE osh.orderId = :orderId AND osh.toStatus = :status")
    boolean hasStatusInHistory(@Param("orderId") UUID orderId, @Param("status") String status);
}