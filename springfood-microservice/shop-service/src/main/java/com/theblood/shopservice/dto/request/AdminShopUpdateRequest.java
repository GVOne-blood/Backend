package com.theblood.shopservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Body cho admin sửa thông tin shop. Tất cả field optional — null/missing
 * giữ nguyên giá trị hiện tại.
 */
@Data
public class AdminShopUpdateRequest {

    @Size(max = 255)
    private String shopName;

    @Size(max = 255)
    private String logo;

    @Size(max = 2000)
    private String introduction;

    @Size(max = 50)
    private String shopType;

    @Size(max = 50)
    private String businessType;

    @Size(max = 50)
    private String email;

    @Size(max = 50)
    private String phoneNumber;

    @Size(max = 50)
    private String taxId;

    @Size(max = 255)
    private String shopAddress;

    @Size(max = 50)
    private String city;

    @Size(max = 50)
    private String province;

    @Size(max = 50)
    private String postalCode;

    @Size(max = 1000)
    private String activeHours;
}
