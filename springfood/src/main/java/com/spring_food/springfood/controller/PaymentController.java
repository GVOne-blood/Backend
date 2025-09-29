package com.spring_food.springfood.controller;


import com.spring_food.springfood.common.enums.PaymentMethod;
import com.spring_food.springfood.dto.request.VNPayPaymentRequest;
import com.spring_food.springfood.dto.response.ResponseData;
import com.spring_food.springfood.model.User;
import com.spring_food.springfood.service.PaymentService;
import com.spring_food.springfood.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@Validated
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PaymentController {

    VNPayService vnPayService;
    PaymentService paymentService;


    @GetMapping("/status/{orderId}")
    public ResponseEntity<ResponseData<?>> handlePaymentStatus(HttpServletRequest request,
                                                               @PathVariable String orderId,
                                                               @AuthenticationPrincipal User user) throws IOException {
        String message = paymentService.handlePaymentCheckingStatus(request, user.getId(), orderId);
        return ResponseEntity.ok(new ResponseData<>(200, "check chung successfully", message));
    }

    @GetMapping("/refund/{orderId}")
    @PreAuthorize("hasAnyRole({'ADMIN', 'CUSTOMER', 'SHOP_OWNER', 'STAFF'})")
    public ResponseEntity<ResponseData<?>> refundPaymentForOrder(HttpServletRequest request,
                                                                 @PathVariable String orderId,
                                                                 @AuthenticationPrincipal User user) throws IOException {
        String message = paymentService.handlePaymentRefund(request, user.getId(), orderId);
        return ResponseEntity.ok(new ResponseData<>(200, "refund refund successfully", message));
    }

    // Endpoint này client sẽ gọi để lấy URL thanh toán
    @PreAuthorize("hasAnyRole({'ADMIN', 'CUSTOMER', 'SHOP_OWNER', 'STAFF'})")
    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(HttpServletRequest request, @RequestBody VNPayPaymentRequest payload) {
        String paymentUrl = "";
        if (payload.getPaymentMethod().equals(PaymentMethod.VNPAY))
            try {
                paymentUrl = vnPayService.createPaymentUrl(request, payload);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        // Trả về URL cho frontend để redirect
        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }

    // Endpoint này VNPay sẽ redirect về sau khi thanh toán
    // Nó phải khớp với `vnp_Returnurl` trong config
    @GetMapping("/vnpay-payment-return")
    @PreAuthorize("hasAnyRole({'ADMIN', 'CUSTOMER', 'SHOP_OWNER', 'STAFF'})")
    public RedirectView handleVnpayReturn(HttpServletRequest request) {
        int paymentStatus = vnPayService.processVNPayReturn(request);

        // Dựa vào kết quả, redirect người dùng đến trang thành công hoặc thất bại trên frontend
        String redirectUrl;
        if (paymentStatus == 1) {
            redirectUrl = "http://localhost:3000/payment-success"; // URL của trang thành công trên frontend
            log.info("payment complete without error");
        } else if (paymentStatus == 0) {
            redirectUrl = "http://localhost:3000/payment-failure"; // URL của trang thất bại trên frontend
            log.warn("payment return a unreachable error");
        } else {
            redirectUrl = "http://localhost:3000/payment-invalid"; // URL của trang chữ ký không hợp lệ
            log.error("invalid signature");
        }

        return new RedirectView(redirectUrl);
    }
}
