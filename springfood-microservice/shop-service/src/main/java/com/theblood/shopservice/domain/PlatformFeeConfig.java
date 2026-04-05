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
 * Entity representing platform fee configuration for various fee types.
 * Maps to springfood_shop.platform_fee_config table.
 */
@Entity
@Table(name = "platform_fee_config", schema = "springfood_shop",
       indexes = {
           @Index(name = "idx_platform_fee_config_code", columnList = "fee_code"),
           @Index(name = "idx_platform_fee_config_active", columnList = "is_active, effective_from"),
           @Index(name = "idx_platform_fee_config_scope", columnList = "apply_scope")
       })
public class PlatformFeeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "fee_id")
    private UUID feeId;

    @NotNull
    @Size(max = 100)
    @Column(name = "fee_code", nullable = false, unique = true, length = 100)
    private String feeCode; // PAYMENT_GATEWAY_FEE, WITHDRAWAL_FEE

    @NotNull
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Size(max = 20)
    @Column(name = "fee_type", nullable = false, length = 20)
    private String feeType; // PERCENTAGE, FLAT

    @Column(name = "percent_rate", precision = 5, scale = 2)
    private BigDecimal percentRate;

    @Column(name = "flat_amount", precision = 15, scale = 2)
    private BigDecimal flatAmount;

    @Size(max = 50)
    @Column(name = "apply_scope", length = 50)
    private String applyScope; // ORDER, WITHDRAWAL

    @Column(name = "is_active")
    private Boolean isActive = true;

    @NotNull
    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @Size(max = 50)
    @Column(name = "created_by", length = 50)
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Size(max = 50)
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    // Constructors
    public PlatformFeeConfig() {}

    public PlatformFeeConfig(String feeCode, String name, String feeType, Instant effectiveFrom) {
        this.feeCode = feeCode;
        this.name = name;
        this.feeType = feeType;
        this.effectiveFrom = effectiveFrom;
    }

    // Getters and Setters
    public UUID getFeeId() {
        return feeId;
    }

    public void setFeeId(UUID feeId) {
        this.feeId = feeId;
    }

    public String getFeeCode() {
        return feeCode;
    }

    public void setFeeCode(String feeCode) {
        this.feeCode = feeCode;
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

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
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

    public String getApplyScope() {
        return applyScope;
    }

    public void setApplyScope(String applyScope) {
        this.applyScope = applyScope;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Check if fee config is currently effective
     */
    public boolean isCurrentlyEffective() {
        Instant now = Instant.now();
        return isActive && 
               effectiveFrom.isBefore(now) && 
               (effectiveTo == null || effectiveTo.isAfter(now));
    }

    /**
     * Calculate fee amount based on base amount
     */
    public BigDecimal calculateFee(BigDecimal baseAmount) {
        if (!isCurrentlyEffective()) {
            return BigDecimal.ZERO;
        }

        switch (FeeType.valueOf(feeType)) {
            case PERCENTAGE:
                return baseAmount.multiply(percentRate).divide(BigDecimal.valueOf(100));
            case FLAT:
                return flatAmount != null ? flatAmount : BigDecimal.ZERO;
            default:
                return BigDecimal.ZERO;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlatformFeeConfig)) return false;
        PlatformFeeConfig that = (PlatformFeeConfig) o;
        return feeId != null && feeId.equals(that.feeId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "PlatformFeeConfig{" +
                "feeId=" + feeId +
                ", feeCode='" + feeCode + '\'' +
                ", name='" + name + '\'' +
                ", feeType='" + feeType + '\'' +
                ", applyScope='" + applyScope + '\'' +
                ", isActive=" + isActive +
                '}';
    }

    /**
     * Enum for fee types
     */
    public enum FeeType {
        PERCENTAGE,
        FLAT
    }

    /**
     * Enum for apply scopes
     */
    public enum ApplyScope {
        ORDER,
        WITHDRAWAL
    }

    /**
     * Common fee codes
     */
    public static class FeeCode {
        public static final String PAYMENT_GATEWAY_FEE = "PAYMENT_GATEWAY_FEE";
        public static final String WITHDRAWAL_FEE = "WITHDRAWAL_FEE";
        public static final String SERVICE_FEE = "SERVICE_FEE";
        public static final String PROCESSING_FEE = "PROCESSING_FEE";
    }
}