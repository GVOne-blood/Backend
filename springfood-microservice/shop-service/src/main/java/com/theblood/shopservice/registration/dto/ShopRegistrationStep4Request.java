package com.theblood.shopservice.registration.dto;

import com.theblood.shopservice.dto.request.BankAccountRegistrationDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopRegistrationStep4Request {

    @NotNull
    private UUID requestId;

    @Size(max = 50)
    private String taxId;

    @Valid
    private BankAccountRegistrationDTO bankAccount;
}
