package com.theblood.shopservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountRegistrationDTO {

    @NotBlank
    @Size(max = 100)
    private String bankName;            // maps to bank_accounts.bank_name

    @NotBlank
    @Size(max = 50)
    private String accountNumber;       // maps to bank_accounts.account_number

    @NotBlank
    @Size(max = 255)
    private String accountHolderName;   // maps to bank_accounts.account_holder_name
    // Phải khớp với fullName trong KYC
}
