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
 * Entity representing commission rules that override default platform commission.
 * Can be applied to specific shops, categories, or products.
 * Maps to springfood_shop.commission_rule table.
 */
@Entity
@Table(name = "commission_rule", schema = "springfood_shop",
       indexes = {
           @Index(name = "idx_commission_rule_shop", columnList = "shop_id, is_active"),
           @Index(name = "idx_commission_rule_category", columnList = "category_name, is_active"),
           @Index(name = "idx_commission_rule_product", columnList = "product_id, is_active"),
           @Index(name = "idx_commission_rule_priority", columnList = "priority, is_active")
       })
public class CommissionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rule_id")
    private UUID ruleId;

    @NotNull
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Scope fields (null = applies to all)
    @Column(name = "shop_id")
    private UUID shopId; // references springfood_shop.shops.shop_id

    @Size(max = 255)
    @Column(name = "category_name", length = 255)
    private String categoryName; // references springfood_product.categories.category_name

    @Column(name = "product_id")
    private UUID productId; // references springfood_product.products.product_id

    @NotNull
    @Size(max = 20)
    @Column(name = "commission_type", nullable = false, length = 20)
    private String commissionType; // PERCENTAGE, FLAT, HYBRID

    @Column(name = "percent_rate", precision = 5, scale = 2)
    private BigDecimal percentRate;

    @Column(name = "flat_amount", precision = 15, scale = 2)
    private BigDecimal flatAmount;

    @Column(name = "min_commission", precision = 15, scale = 2)
    private BigDecimal minCommission;

    @NotNull
    @Column(name = "priority", nullable = false)
    private Integer priority = 100; // Lower number = higher priority

    @Column(name = "is_active")
    private Boolean isActive = true;

    @NotNull
    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Size(max = 50)
    @Column(name = "created_by", length = 50)
    private String createdBy;

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
    public CommissionRule() {}

    public CommissionRule(String name, String commissionType, Integer priority, Instant effectiveFrom) {
        this.name = name;
        this.commissionType = commissionType;
        this.priority = priority;
        this.effectiveFrom = effectiveFrom;
    }

    // Getters and Setters
    public UUID getRuleId() {
        return ruleId;
    }

    public void setRuleId(UUID ruleId) {
        this.ruleId = ruleId;
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

    public UUID getShopId() {
        return shopId;
    }

    public void setShopId(UUID shopId) {
        this.shopId = shopId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
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
     * Check if rule is currently effective
     */
    public boolean isCurrentlyEffective() {
        Instant now = Instant.now();
        return isActive && 
               effectiveFrom.isBefore(now) && 
               (effectiveTo == null || effectiveTo.isAfter(now));
    }

    /**
     * Get rule scope type
     */
    public RuleScope getRuleScope() {
        if (productId != null) return RuleScope.PRODUCT;
        if (categoryName != null) return RuleScope.CATEGORY;
        if (shopId != null) return RuleScope.SHOP;
        return RuleScope.GLOBAL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommissionRule)) return false;
        CommissionRule that = (CommissionRule) o;
        return ruleId != null && ruleId.equals(that.ruleId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CommissionRule{" +
                "ruleId=" + ruleId +
                ", name='" + name + '\'' +
                ", commissionType='" + commissionType + '\'' +
                ", priority=" + priority +
                ", scope=" + getRuleScope() +
                ", isActive=" + isActive +
                '}';
    }

    /**
     * Enum for rule scope types
     */
    public enum RuleScope {
        GLOBAL,    // Applies to all
        SHOP,      // Applies to specific shop
        CATEGORY,  // Applies to specific category
        PRODUCT    // Applies to specific product
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