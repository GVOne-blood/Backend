package com.spring_food.springfood.service;

import com.spring_food.springfood.dto.request.VNPayPaymentRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Map;

public interface VNPayService {

    String handlePaymentRefund(HttpServletRequest request, String userId, String orderId) throws IOException;

    String handlePaymentCheckingStatus(HttpServletRequest request, String userId, String orderId) throws IOException;

    String createPaymentUrl(HttpServletRequest request, VNPayPaymentRequest paymentRequest) throws UnsupportedEncodingException;

    Map<String, String> queryTransactionStatus(String vnp_TxnRef, String vnp_TransactionNo, LocalDateTime vnp_TransDate, String requestIP) throws IOException;

    Map<String, String> queryRefund(VNPayPaymentRequest vnPayPaymentRequest, String requestIP) throws IOException;

    int processVNPayReturn(HttpServletRequest request);
}
