package com.spring_food.springfood.service;

import com.spring_food.springfood.dto.request.VNPayPaymentRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Map;

public interface VNPayService {

    String createPaymentUrl(HttpServletRequest request, VNPayPaymentRequest paymentRequest) throws UnsupportedEncodingException;

    Map<String, String> queryTransactionStatus(String vnp_TxnRef, LocalDateTime vnp_TransDate, String requestIP) throws IOException;

    Map<String, String> queryRefund(VNPayPaymentRequest vnPayPaymentRequest, String requestIP) throws IOException;

    int processVNPayReturn(HttpServletRequest request);
}
