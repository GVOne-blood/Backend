package com.theblood.authentication.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "shop_registration_request",
        indexes = {
                @Index(name = "idx_shop_reg_user", columnList = "user_id, status"),
                @Index(name = "idx_shop_reg_status", columnList = "status, created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopRegistrationRequest {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "request_id", updatable = false, nullable = false)
    private UUID requestId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "shop_name", nullable = false, length = 255)
    private String shopName;

    @Column(name = "logo_media_id", length = 255)
    private String logoMediaId;

    @Column(name = "introduction", columnDefinition = "text")
    private String introduction;

    @Column(name = "shop_type", nullable = false, length = 50)
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
    
    @Column(name = "status", nullable = false, length = 50)
    private String status = "DRAFT";

    @Column(name = "reject_reason", columnDefinition = "text")
    private String rejectReason;

    @Column(name = "reviewed_by", length = 50)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private ZonedDateTime reviewedAt;

    @Column(name = "shop_id")
    private UUID shopId;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
        if (status == null) status = "DRAFT";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
