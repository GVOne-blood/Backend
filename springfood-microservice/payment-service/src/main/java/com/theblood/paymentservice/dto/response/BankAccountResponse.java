package com.theblood.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankAccountResponse {

    private UUID accountId;
    private UUID shopId;
    private String bankName;
    private String accountNumber;
    private String accountHolderName;
    private Boolean isDefault;
    private Boolean isVerified;
}
