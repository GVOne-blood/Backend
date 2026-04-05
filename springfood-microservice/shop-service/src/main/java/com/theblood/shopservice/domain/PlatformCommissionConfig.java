package com.theblood.shopservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing platform-wide commission configuration.
 * Maps to springfood_shop.platform_commission_config table.
 */
@Entity
@Table(name = "platform_commission_config", schema = "springfood_shop")
public class PlatformCommissionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "config_id")
    private UUID configId;

    @NotNull
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Size(max = 20)
    @Column(name = "commission_type", nullable = false, length = 20)
    private String commissionType; // PERCENTAGE, FLAT, HYBRID

    @Column(name = "percent_rate", precision = 5, scale = 2)
    private BigDecimal percentRate; // e.g., 5.00 (%)

    @Column(name = "flat_amount", precision = 15, scale = 2)
    private BigDecimal flatAmount; // e.g., 3000 (VND/order)

    @Column(name = "min_commission", precision = 15, scale = 2)
    private BigDecimal minCommission; // minimum commission per order

    @Column(name = "max_commission", precision = 15, scale = 2)
    private BigDecimal maxCommission; // maximum commission cap

    @Column(name = "is_active")
    private Boolean isActive = false;

    @NotNull
    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Size(max = 50)
    @Column(name = "created_by", length = 50)
    private String createdBy; // admin user_id

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @Size(max = 50)
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Constructors
    public PlatformCommissionConfig() {}

    public PlatformCommissionConfig(String name, String commissionType, Instant effectiveFrom) {
        this.name = name;
        this.commissionType = commissionType;
        this.effectiveFrom = effectiveFrom;
    }

    // Getters and Setters
    public UUID getConfigId() {
        return configId;
    }

    public void setConfigId(UUID configId) {
        this.configId = configId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommissionType() {
        return commissionType;
    }

    public void setCommissionType(String commissionType) {
        this.commissionType = commissionType;
    }

    public BigDecimal getPercentRate() {
        return percentRate;
    }

    public void setPercentRate(BigDecimal percentRate) {
        this.percentRate = percentRate;
    }

    public BigDecimal getFlatAmount() {
        return flatAmount;
    }

    public void setFlatAmount(BigDecimal flatAmount) {
        this.flatAmount = flatAmount;
    }

    public BigDecimal getMinCommission() {
        return minCommission;
    }

    public void setMinCommission(BigDecimal minCommission) {
        this.minCommission = minCommission;
    }

    public BigDecimal getMaxCommission() {
        return maxCommission;
    }

    public void setMaxCommission(BigDecimal maxCommission) {
        this.maxCommission = maxCommission;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Instant effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Instant getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(Instant effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Check if config is currently effective
     */
    public boolean isCurrentlyEffective() {
        Instant now = Instant.now();
        return isActive && 
               effectiveFrom.isBefore(now) && 
               (effectiveTo == null || effectiveTo.isAfter(now));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlatformCommissionConfig)) return false;
        PlatformCommissionConfig that = (PlatformCommissionConfig) o;
        return configId != null && configId.equals(that.configId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "PlatformCommissionConfig{" +
                "configId=" + configId +
                ", name='" + name + '\'' +
                ", commissionType='" + commissionType + '\'' +
                ", isActive=" + isActive +
                ", effectiveFrom=" + effectiveFrom +
                ", effectiveTo=" + effectiveTo +
                '}';
    }

    /**
     * Enum for commission types
     */
    public enum CommissionType {
        PERCENTAGE,
        FLAT,
        HYBRID
    }
}