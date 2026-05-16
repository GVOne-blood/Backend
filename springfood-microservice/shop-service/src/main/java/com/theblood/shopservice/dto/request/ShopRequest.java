package com.theblood.shopservice.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopRequest {

    // ===== BƯỚC 1: Thông tin cơ bản =====
    @NotBlank
    @Size(max = 255)
    private String shopName;

    private String logoMediaId;         // references springfood_media.media_file.id

    @Size(max = 2000)
    private String introduction;

    @NotBlank
    private String shopType;            // INDIVIDUAL | HOUSEHOLD | COMPANY
    // maps to shops.shop_type

    @NotBlank
    private String businessType;        // maps to shops.business_type (ngành hàng chính)

    // ===== BƯỚC 2: Liên hệ & Địa chỉ kho/lấy hàng =====
    @Email
    @NotBlank
    @Size(max = 100)
    private String email;               // maps to shops.email

    @NotBlank
    @Size(max = 50)
    private String phoneNumber;         // maps to shops.phone_number

    @NotBlank
    @Size(max = 255)
    private String shopAddress;         // maps to shops.shop_address

    @NotBlank
    @Size(max = 50)
    private String city;                // maps to shops.city

    @NotBlank
    @Size(max = 50)
    private String province;            // maps to shops.province

    @Size(max = 50)
    private String postalCode;          // maps to shops.postal_code

    @Size(max = 50)
    private String nationId;            // maps to shops.nation_id (default "VN")

    @Size(max = 1000)
    private String activeHours;         // maps to shops.active_hours (JSON)

    // ===== BƯỚC 3: Thông tin thuế =====
    @Size(max = 50)
    private String taxId;               // maps to shops.tax_id
    // 10 số (cá nhân kinh doanh) hoặc 13 số (DN)

    // ===== BƯỚC 4: KYC danh tính cá nhân =====
    // Required nếu shopType = INDIVIDUAL, HOUSEHOLD, COMPANY (người đại diện)
    @NotNull
    @Valid
    private IndividualKycDTO kyc;

    // ===== BƯỚC 5: Hồ sơ doanh nghiệp =====
    // Required nếu shopType = HOUSEHOLD hoặc COMPANY
    @Valid
    private BusinessDocDTO businessDoc;

    // ===== BƯỚC 6: Tài khoản ngân hàng nhận tiền =====
    // maps to springfood_payment.bank_accounts
    // Optional tại bước đăng ký, có thể bổ sung sau
    @Valid
    private BankAccountRegistrationDTO bankAccount;


}
