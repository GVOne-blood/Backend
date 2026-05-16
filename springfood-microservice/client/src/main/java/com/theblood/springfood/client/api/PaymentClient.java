package com.theblood.springfood.client.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@BaseClient.ServiceClient(value = "payment-service", path = "/payment")
public interface PaymentClient extends BaseClient {

    @ClientMethod(httpMethod = "POST", path = "/bank-accounts")
    ClientResponse<BankAccountResponse> createBankAccount(BankAccountCreateRequest request);

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class BankAccountCreateRequest {
        private UUID shopId;
        private String bankName;
        private String accountNumber;
        private String accountHolderName;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class BankAccountResponse {
        private UUID accountId;
        private UUID shopId;
        private String bankName;
        private String accountNumber;
        private String accountHolderName;
        private Boolean isDefault;
        private Boolean isVerified;
    }
}
