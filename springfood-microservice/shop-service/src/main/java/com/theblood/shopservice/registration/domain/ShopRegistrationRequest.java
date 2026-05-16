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
@Table(name = "shop_registration_request", schema = "springfood_authentication")
public class ShopRegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "shop_name", length = 255)
    private String shopName;

    @Column(name = "logo_media_id", length = 255)
    private String logoMediaId;

    @Column(name = "introduction")
    private String introduction;

    @Column(name = "shop_type", length = 50)
    private String shopType;

    @Column(name = "business_type", length = 50)
    private String businessType;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(name = "shop_address", length = 255)
    private String shopAddress;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "province", length = 50)
    private String province;

    @Column(name = "postal_code", length = 50)
    private String postalCode;

    @Column(name = "nation_id", length = 50)
    private String nationId;

    @Column(name = "active_hours", length = 1000)
    private String activeHours;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "reviewed_by", length = 50)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "shop_id")
    private UUID shopId;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
