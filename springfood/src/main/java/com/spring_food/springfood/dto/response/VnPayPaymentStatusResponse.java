package com.spring_food.springfood.dto.response;

import com.spring_food.springfood.common.enums.TransactionStatus;

public class VnPayPaymentStatusResponse {
    private String orderId;
    private TransactionStatus systemStatus; // Trạng thái trong hệ thống của bạn
    private String vnpayTransactionStatus; // Mã trạng thái gốc từ VNPay (ví dụ: "00", "02")
    private String vnpayMessage; // Mô tả từ VNPay
    private String message; // Mô tả thân thiện cho người dùng
}
