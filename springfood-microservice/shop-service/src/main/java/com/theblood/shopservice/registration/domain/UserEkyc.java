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
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_ekyc", schema = "springfood_authentication")
public class UserEkyc {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "kyc_id", nullable = false)
    private UUID kycId;

    @Column(name = "request_id")
    private UUID requestId;

    @Column(name = "shop_id")
    private UUID shopId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "id_number", length = 20, nullable = false)
    private String idNumber;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "permanent_address")
    private String permanentAddress;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "issued_place", length = 255)
    private String issuedPlace;

    @Column(name = "front_image_media_id", length = 255)
    private String frontImageMediaId;

    @Column(name = "back_image_media_id", length = 255)
    private String backImageMediaId;

    @Column(name = "selfie_media_id", length = 255)
    private String selfieMediaId;

    @Column(name = "nfc_verified")
    private Boolean nfcVerified = false;

    @Column(name = "nfc_verified_at")
    private Instant nfcVerifiedAt;

    @Column(name = "nfc_raw_data")
    private String nfcRawData;

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
