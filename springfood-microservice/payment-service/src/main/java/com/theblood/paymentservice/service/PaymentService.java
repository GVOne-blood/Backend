package com.theblood.paymentservice.service;

import com.theblood.springfood.common.enums.TransactionStatus;
import com.theblood.paymentservice.dto.request.BankAccountCreateRequest;
import com.theblood.paymentservice.dto.request.PaymentInfoRequest;
import com.theblood.paymentservice.dto.response.BankAccountResponse;
import com.theblood.paymentservice.model.PaymentTransactions;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public interface PaymentService {


    PaymentTransactions createPaymentTransaction(PaymentInfoRequest paymentInfoRequest);

    void updateCodPaymentTransaction();

    void updatePaymentTransaction(UUID id, String transactionNo, TransactionStatus transactionStatus, LocalDateTime transferSuccessAt);

    void handlePaymentReturnSuccess(Map<String, String> response);

    void handlePaymentReturnFail(Map<String, String> response);

    BankAccountResponse createBankAccount(BankAccountCreateRequest request);
}
