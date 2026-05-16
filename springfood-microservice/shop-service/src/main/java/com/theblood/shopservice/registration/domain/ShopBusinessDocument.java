package com.theblood.shopservice.registration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "shop_business_document", schema = "springfood_shop")
public class ShopBusinessDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "doc_id", nullable = false)
    private UUID docId;

    @Column(name = "request_id")
    private UUID requestId;

    @Column(name = "shop_id")
    private UUID shopId;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "business_reg_number", length = 100)
    private String businessRegNumber;

    @Column(name = "license_media_id", length = 255)
    private String licenseMediaId;

    @Column(name = "company_address", length = 500)
    private String companyAddress;

    @Column(name = "verification_status", length = 50)
    private String verificationStatus;

    @Column(name = "verified_by", length = 50)
    private String verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
