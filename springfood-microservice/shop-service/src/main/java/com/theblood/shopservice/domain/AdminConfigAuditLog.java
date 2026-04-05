package com.theblood.shopservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing audit log for admin configuration changes.
 * Tracks who changed what configuration/rule/contract.
 * Maps to springfood_shop.admin_config_audit_log table.
 */
@Entity
@Table(name = "admin_config_audit_log", schema = "springfood_shop",
       indexes = {
           @Index(name = "idx_admin_audit_entity", columnList = "entity_type, entity_id"),
           @Index(name = "idx_admin_audit_changed_by", columnList = "changed_by, changed_at"),
           @Index(name = "idx_admin_audit_changed_at", columnList = "changed_at DESC"),
           @Index(name = "idx_admin_audit_action", columnList = "action, entity_type")
       })
public class AdminConfigAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id")
    private UUID logId;

    @NotNull
    @Size(max = 50)
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // PLATFORM_COMMISSION, COMMISSION_RULE, PLATFORM_FEE, SHOP_CONTRACT

    @NotNull
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @NotNull
    @Size(max = 20)
    @Column(name = "action", nullable = false, length = 20)
    private String action; // CREATE, UPDATE, DEACTIVATE, DELETE

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private Map<String, Object> oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private Map<String, Object> newValue;

    @NotNull
    @Size(max = 50)
    @Column(name = "changed_by", nullable = false, length = 50)
    private String changedBy; // references springfood_authentication.user.user_id

    @NotNull
    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Constructors
    public AdminConfigAuditLog() {}

    public AdminConfigAuditLog(String entityType, UUID entityId, String action, String changedBy) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.changedBy = changedBy;
        this.changedAt = Instant.now();
    }

    public AdminConfigAuditLog(String entityType, UUID entityId, String action, 
                              Map<String, Object> oldValue, Map<String, Object> newValue, 
                              String changedBy, String note) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.changedAt = Instant.now();
        this.note = note;
    }

    // Getters and Setters
    public UUID getLogId() {
        return logId;
    }

    public void setLogId(UUID logId) {
        this.logId = logId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Map<String, Object> getOldValue() {
        return oldValue;
    }

    public void setOldValue(Map<String, Object> oldValue) {
        this.oldValue = oldValue;
    }

    public Map<String, Object> getNewValue() {
        return newValue;
    }

    public void setNewValue(Map<String, Object> newValue) {
        this.newValue = newValue;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdminConfigAuditLog)) return false;
        AdminConfigAuditLog that = (AdminConfigAuditLog) o;
        return logId != null && logId.equals(that.logId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "AdminConfigAuditLog{" +
                "logId=" + logId +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", action='" + action + '\'' +
                ", changedBy='" + changedBy + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }

    /**
     * Enum for entity types
     */
    public enum EntityType {
        PLATFORM_COMMISSION,
        COMMISSION_RULE,
        PLATFORM_FEE,
        SHOP_CONTRACT
    }

    /**
     * Enum for audit actions
     */
    public enum AuditAction {
        CREATE,
        UPDATE,
        DEACTIVATE,
        DELETE
    }

    /**
     * Helper method to create audit log for CREATE action
     */
    public static AdminConfigAuditLog createLog(String entityType, UUID entityId, 
                                               Map<String, Object> newValue, 
                                               String changedBy, String note) {
        return new AdminConfigAuditLog(entityType, entityId, AuditAction.CREATE.name(), 
                                     null, newValue, changedBy, note);
    }

    /**
     * Helper method to create audit log for UPDATE action
     */
    public static AdminConfigAuditLog updateLog(String entityType, UUID entityId, 
                                               Map<String, Object> oldValue, 
                                               Map<String, Object> newValue, 
                                               String changedBy, String note) {
        return new AdminConfigAuditLog(entityType, entityId, AuditAction.UPDATE.name(), 
                                     oldValue, newValue, changedBy, note);
    }

    /**
     * Helper method to create audit log for DELETE action
     */
    public static AdminConfigAuditLog deleteLog(String entityType, UUID entityId, 
                                               Map<String, Object> oldValue, 
                                               String changedBy, String note) {
        return new AdminConfigAuditLog(entityType, entityId, AuditAction.DELETE.name(), 
                                     oldValue, null, changedBy, note);
    }

    /**
     * Helper method to create audit log for DEACTIVATE action
     */
    public static AdminConfigAuditLog deactivateLog(String entityType, UUID entityId, 
                                                   Map<String, Object> oldValue, 
                                                   Map<String, Object> newValue, 
                                                   String changedBy, String note) {
        return new AdminConfigAuditLog(entityType, entityId, AuditAction.DEACTIVATE.name(), 
                                     oldValue, newValue, changedBy, note);
    }
}