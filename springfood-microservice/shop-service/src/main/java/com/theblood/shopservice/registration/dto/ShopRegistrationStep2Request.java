package com.theblood.shopservice.registration.dto;

import com.theblood.shopservice.dto.request.BusinessDocDTO;
import com.theblood.shopservice.dto.request.IndividualKycDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopRegistrationStep2Request {

    @NotNull
    private UUID requestId;

    @NotBlank
    private String shopType; // INDIVIDUAL | HOUSEHOLD | COMPANY

    @NotNull
    @Valid
    private IndividualKycDTO kyc;

    @Valid
    private BusinessDocDTO businessDoc;
}
