package com.theblood.orderservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing order status change history for tracking.
 * Maps to springfood_order.order_status_history table.
 */
@Entity
@Table(name = "order_status_history", schema = "springfood_order",
       indexes = {
           @Index(name = "idx_order_created_at", columnList = "order_id, created_at")
       })
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id")
    private UUID historyId;

    @NotNull
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Size(max = 100)
    @Column(name = "from_status", length = 100)
    private String fromStatus;

    @NotNull
    @Size(max = 100)
    @Column(name = "to_status", nullable = false, length = 100)
    private String toStatus;

    @Size(max = 50)
    @Column(name = "changed_by", length = 50)
    private String changedBy; // user_id or "system"

    @Size(max = 50)
    @Column(name = "changed_role", length = 50)
    private String changedRole; // CUSTOMER, SHOP, SHIPPER, SYSTEM

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @NotNull
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Constructors
    public OrderStatusHistory() {}

    public OrderStatusHistory(UUID orderId, String toStatus, String changedBy, String changedRole) {
        this.orderId = orderId;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.changedRole = changedRole;
    }

    public OrderStatusHistory(UUID orderId, String fromStatus, String toStatus, String changedBy, String changedRole, String note) {
        this.orderId = orderId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.changedRole = changedRole;
        this.note = note;
    }

    // Getters and Setters
    public UUID getHistoryId() {
        return historyId;
    }

    public void setHistoryId(UUID historyId) {
        this.historyId = historyId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangedRole() {
        return changedRole;
    }

    public void setChangedRole(String changedRole) {
        this.changedRole = changedRole;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderStatusHistory)) return false;
        OrderStatusHistory that = (OrderStatusHistory) o;
        return historyId != null && historyId.equals(that.historyId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "OrderStatusHistory{" +
                "historyId=" + historyId +
                ", orderId=" + orderId +
                ", fromStatus='" + fromStatus + '\'' +
                ", toStatus='" + toStatus + '\'' +
                ", changedBy='" + changedBy + '\'' +
                ", changedRole='" + changedRole + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * Enum for user roles that can change order status
     */
    public enum ChangeRole {
        CUSTOMER,
        SHOP,
        SHIPPER,
        SYSTEM
    }
}