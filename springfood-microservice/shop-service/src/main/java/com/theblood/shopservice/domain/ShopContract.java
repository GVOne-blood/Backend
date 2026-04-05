package com.theblood.shopservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity representing shop contract history.
 * Maps to springfood_shop.shop_contract table.
 */
@Entity
@Table(name = "shop_contract", schema = "springfood_shop",
       indexes = {
           @Index(name = "idx_shop_contract_shop", columnList = "shop_id, status"),
           @Index(name = "idx_shop_contract_code", columnList = "contract_code"),
           @Index(name = "idx_shop_contract_dates", columnList = "start_date, end_date"),
           @Index(name = "idx_shop_contract_status", columnList = "status")
       })
public class ShopContract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "contract_id")
    private UUID contractId;

    @NotNull
    @Column(name = "shop_id", nullable = false)
    private UUID shopId; // references springfood_shop.shops.shop_id

    @NotNull
    @Size(max = 100)
    @Column(name = "contract_code", nullable = false, length = 100)
    private String contractCode;

    @NotNull
    @Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Size(max = 50)
    @Column(name = "contract_type", length = 50)
    private String contractType; // STANDARD, MALL, VIP, PARTNER

    @Size(max = 255)
    @Column(name = "document_id", length = 255)
    private String documentId; // references springfood_media.media_file.id

    // Commission terms specific to this contract (overrides commission_rule)
    @Size(max = 20)
    @Column(name = "commission_type", length = 20)
    private String commissionType; // PERCENTAGE, FLAT, HYBRID

    @Column(name = "percent_rate", precision = 5, scale = 2)
    private BigDecimal percentRate;

    @Column(name = "flat_amount", precision = 15, scale = 2)
    private BigDecimal flatAmount;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @NotNull
    @Size(max = 50)
    @Column(name = "status", nullable = false, length = 50)
    private String status; // DRAFT, ACTIVE, EXPIRED, TERMINATED

    @Column(name = "signed_at")
    private Instant signedAt;

    @Size(max = 100)
    @Column(name = "signed_by_shop", length = 100)
    private String signedByShop;

    @Size(max = 50)
    @Column(name = "signed_by_admin", length = 50)
    private String signedByAdmin; // references springfood_authentication.user.user_id

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
    public ShopContract() {}

    public ShopContract(UUID shopId, String contractCode, String title, LocalDate startDate, String status) {
        this.shopId = shopId;
        this.contractCode = contractCode;
        this.title = title;
        this.startDate = startDate;
        this.status = status;
    }

    // Getters and Setters
    public UUID getContractId() {
        return contractId;
    }

    public void setContractId(UUID contractId) {
        this.contractId = contractId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public void setShopId(UUID shopId) {
        this.shopId = shopId;
    }

    public String getContractCode() {
        return contractCode;
    }

    public void setContractCode(String contractCode) {
        this.contractCode = contractCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(Instant signedAt) {
        this.signedAt = signedAt;
    }

    public String getSignedByShop() {
        return signedByShop;
    }

    public void setSignedByShop(String signedByShop) {
        this.signedByShop = signedByShop;
    }

    public String getSignedByAdmin() {
        return signedByAdmin;
    }

    public void setSignedByAdmin(String signedByAdmin) {
        this.signedByAdmin = signedByAdmin;
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
     * Check if contract is currently active
     */
    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return ContractStatus.ACTIVE.name().equals(status) &&
               startDate.isBefore(today.plusDays(1)) &&
               (endDate == null || endDate.isAfter(today.minusDays(1)));
    }

    /**
     * Check if contract is expired
     */
    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    /**
     * Sign the contract
     */
    public void signContract(String shopSigner, String adminSigner) {
        this.signedAt = Instant.now();
        this.signedByShop = shopSigner;
        this.signedByAdmin = adminSigner;
        this.status = ContractStatus.ACTIVE.name();
    }

    /**
     * Terminate the contract
     */
    public void terminateContract() {
        this.status = ContractStatus.TERMINATED.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShopContract)) return false;
        ShopContract that = (ShopContract) o;
        return contractId != null && contractId.equals(that.contractId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ShopContract{" +
                "contractId=" + contractId +
                ", shopId=" + shopId +
                ", contractCode='" + contractCode + '\'' +
                ", title='" + title + '\'' +
                ", contractType='" + contractType + '\'' +
                ", status='" + status + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }

    /**
     * Enum for contract status
     */
    public enum ContractStatus {
        DRAFT,
        ACTIVE,
        EXPIRED,
        TERMINATED
    }

    /**
     * Enum for contract types
     */
    public enum ContractType {
        STANDARD,
        MALL,
        VIP,
        PARTNER
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