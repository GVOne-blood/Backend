package com.theblood.shopservice.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopRegistrationStep1Request {

    @NotBlank
    @Size(max = 255)
    private String shopName;

    private String logoMediaId;

    @Size(max = 2000)
    private String introduction;

    @NotBlank
    private String shopType; // INDIVIDUAL | HOUSEHOLD | COMPANY

    @NotBlank
    private String businessType; // main category
}
