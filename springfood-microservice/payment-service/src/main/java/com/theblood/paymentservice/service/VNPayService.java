package com.theblood.paymentservice.service;

import com.theblood.paymentservice.dto.request.VNPayPaymentRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public interface VNPayService {

    String handlePaymentRefund(HttpServletRequest request, UUID userId, UUID referenceId) throws IOException;

    String handlePaymentCheckingStatus(HttpServletRequest request, UUID userId, UUID paymentTransactionId) throws IOException;

    String createPaymentUrl(HttpServletRequest request, VNPayPaymentRequest paymentRequest) throws UnsupportedEncodingException;

    Map<String, String> queryTransactionStatus(String vnp_TxnRef, String vnp_TransactionNo, LocalDateTime vnp_TransDate, String requestIP) throws IOException;

    Map<String, String> queryRefund(VNPayPaymentRequest vnPayPaymentRequest, String requestIP) throws IOException;

    int processVNPayReturn(HttpServletRequest request);
}
