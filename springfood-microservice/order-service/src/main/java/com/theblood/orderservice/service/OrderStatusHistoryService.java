package com.theblood.orderservice.service;

import com.theblood.orderservice.domain.OrderStatusHistory;
import com.theblood.orderservice.repository.OrderStatusHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing OrderStatusHistory entities.
 */
@Service
@Transactional
public class OrderStatusHistoryService {

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    public OrderStatusHistoryService(OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    /**
     * Record order status change
     */
    public OrderStatusHistory recordStatusChange(UUID orderId, String fromStatus, String toStatus, 
                                               String changedBy, String changedRole, String note) {
        OrderStatusHistory statusHistory = new OrderStatusHistory(orderId, fromStatus, toStatus, changedBy, changedRole, note);
        return orderStatusHistoryRepository.save(statusHistory);
    }

    /**
     * Record order status change (simple version)
     */
    public OrderStatusHistory recordStatusChange(UUID orderId, String toStatus, String changedBy, String changedRole) {
        // Get current status from latest history entry
        OrderStatusHistory latestHistory = orderStatusHistoryRepository.findLatestByOrderId(orderId);
        String fromStatus = latestHistory != null ? latestHistory.getToStatus() : null;
        
        return recordStatusChange(orderId, fromStatus, toStatus, changedBy, changedRole, null);
    }

    /**
     * Get complete status history for an order
     */
    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getOrderStatusHistory(UUID orderId) {
        return orderStatusHistoryRepository.findByOrderIdOrderByCreatedAt(orderId);
    }

    /**
     * Get latest status for an order
     */
    @Transactional(readOnly = true)
    public OrderStatusHistory getLatestOrderStatus(UUID orderId) {
        return orderStatusHistoryRepository.findLatestByOrderId(orderId);
    }

    /**
     * Get status changes by role (for analytics)
     */
    @Transactional(readOnly = true)
    public Page<OrderStatusHistory> getStatusChangesByRole(String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderStatusHistoryRepository.findByChangedRole(role, pageable);
    }

    /**
     * Get status changes by specific user
     */
    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getStatusChangesByUser(String changedBy) {
        return orderStatusHistoryRepository.findByChangedBy(changedBy);
    }

    /**
     * Get status changes within time range
     */
    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getStatusChangesInTimeRange(Instant startTime, Instant endTime) {
        return orderStatusHistoryRepository.findByCreatedAtBetween(startTime, endTime);
    }

    /**
     * Get orders that changed to specific status
     */
    @Transactional(readOnly = true)
    public Page<OrderStatusHistory> getOrdersByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderStatusHistoryRepository.findByToStatus(status, pageable);
    }

    /**
     * Get orders that changed from specific status
     */
    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getOrdersFromStatus(String status) {
        return orderStatusHistoryRepository.findByFromStatus(status);
    }

    /**
     * Get status change count for an order
     */
    @Transactional(readOnly = true)
    public long getStatusChangeCount(UUID orderId) {
        return orderStatusHistoryRepository.countByOrderId(orderId);
    }

    /**
     * Get status change statistics for dashboard
     */
    @Transactional(readOnly = true)
    public List<Object[]> getStatusChangeStatistics(int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        return orderStatusHistoryRepository.getStatusChangeStats(since);
    }

    /**
     * Get status history for multiple orders
     */
    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getStatusHistoryForOrders(List<UUID> orderIds) {
        return orderStatusHistoryRepository.findByOrderIdIn(orderIds);
    }

    /**
     * Check if order has been in specific status
     */
    @Transactional(readOnly = true)
    public boolean hasOrderBeenInStatus(UUID orderId, String status) {
        return orderStatusHistoryRepository.hasStatusInHistory(orderId, status);
    }

    /**
     * Get recent status changes (for monitoring)
     */
    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getRecentStatusChanges(int hours) {
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        Instant now = Instant.now();
        return orderStatusHistoryRepository.findByCreatedAtBetween(since, now);
    }

    /**
     * Record system status change (automated)
     */
    public OrderStatusHistory recordSystemStatusChange(UUID orderId, String toStatus, String note) {
        return recordStatusChange(orderId, toStatus, "system", "SYSTEM", note);
    }

    /**
     * Record customer status change
     */
    public OrderStatusHistory recordCustomerStatusChange(UUID orderId, String toStatus, UUID customerId, String note) {
        return recordStatusChange(orderId, toStatus, customerId.toString(), "CUSTOMER", note);
    }

    /**
     * Record shop status change
     */
    public OrderStatusHistory recordShopStatusChange(UUID orderId, String toStatus, UUID shopId, String note) {
        return recordStatusChange(orderId, toStatus, shopId.toString(), "SHOP", note);
    }

    /**
     * Record shipper status change
     */
    public OrderStatusHistory recordShipperStatusChange(UUID orderId, String toStatus, UUID shipperId, String note) {
        return recordStatusChange(orderId, toStatus, shipperId.toString(), "SHIPPER", note);
    }
}