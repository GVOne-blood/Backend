package com.theblood.shopservice.registration.dto;

import com.theblood.shopservice.registration.domain.ShopRegistrationRequest;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-only DTO admin nhìn thấy 1 đơn đăng ký shop.
 *
 * <p>Tách khỏi entity để tránh leak metadata Hibernate. Tất cả field đều
 * nullable trừ id/userId/status/createdAt vì step-by-step registration có thể
 * mới điền step 1.</p>
 */
@Builder
public record AdminShopRegistrationView(
    UUID requestId,
    UUID userId,
    String shopName,
    String logoMediaId,
    String introduction,
    String shopType,
    String businessType,
    String email,
    String phoneNumber,
    String shopAddress,
    String city,
    String province,
    String postalCode,
    String nationId,
    String activeHours,
    String taxId,
    /** DRAFT | PENDING | APPROVED | REJECTED. */
    String status,
    String rejectReason,
    String reviewedBy,
    Instant reviewedAt,
    UUID shopId,
    Instant createdAt,
    Instant updatedAt
) {
    public static AdminShopRegistrationView from(ShopRegistrationRequest e) {
        return AdminShopRegistrationView.builder()
            .requestId(e.getRequestId())
            .userId(e.getUserId())
            .shopName(e.getShopName())
            .logoMediaId(e.getLogoMediaId())
            .introduction(e.getIntroduction())
            .shopType(e.getShopType())
            .businessType(e.getBusinessType())
            .email(e.getEmail())
            .phoneNumber(e.getPhoneNumber())
            .shopAddress(e.getShopAddress())
            .city(e.getCity())
            .province(e.getProvince())
            .postalCode(e.getPostalCode())
            .nationId(e.getNationId())
            .activeHours(e.getActiveHours())
            .taxId(e.getTaxId())
            .status(e.getStatus())
            .rejectReason(e.getRejectReason())
            .reviewedBy(e.getReviewedBy())
            .reviewedAt(e.getReviewedAt())
            .shopId(e.getShopId())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .build();
    }
}
