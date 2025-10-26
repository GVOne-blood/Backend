package com.spring_food.springfood.service;

import com.spring_food.springfood.common.enums.TransactionStatus;
import com.spring_food.springfood.dto.request.PaymentInfoRequest;
import com.spring_food.springfood.model.Order;
import com.spring_food.springfood.model.PaymentTransactions;

import java.util.List;
import java.util.Map;

public interface PaymentService {


    PaymentTransactions createPaymentTransaction(PaymentInfoRequest paymentInfoRequest, List<Order> orders);

    void updatePaymentTransaction(String id, String transactionNo, TransactionStatus transactionStatus);

    void handlePaymentReturnSuccess(Map<String, String> response);

    void handlePaymentReturnFail(Map<String, String> response);
}
