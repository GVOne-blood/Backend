package com.theblood.paymentservice.service;

import com.theblood.paymentservice.common.enums.TransactionStatus;
import com.theblood.paymentservice.dto.request.PaymentInfoRequest;
import com.theblood.paymentservice.model.PaymentTransactions;

import java.util.List;
import java.util.Map;

public interface PaymentService {


    PaymentTransactions createPaymentTransaction(PaymentInfoRequest paymentInfoRequest, List<Order> orders);

    void updatePaymentTransaction(String id, String transactionNo, TransactionStatus transactionStatus);

    void handlePaymentReturnSuccess(Map<String, String> response);

    void handlePaymentReturnFail(Map<String, String> response);
}