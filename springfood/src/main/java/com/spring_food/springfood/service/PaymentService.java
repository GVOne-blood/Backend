package com.spring_food.springfood.service;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Map;

public interface PaymentService {


    void handlePaymentReturnSuccess(String orderId, Map<String, String> response);

    void handlePaymentReturnFail();

    String handlePaymentCheckingStatus(HttpServletRequest request, String orderId) throws IOException;

    String handlePaymentRefund(HttpServletRequest request, String orderId) throws IOException;
}
