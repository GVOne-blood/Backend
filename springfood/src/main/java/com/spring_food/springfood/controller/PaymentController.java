package com.spring_food.springfood.controller;


import com.spring_food.springfood.config.VNPayConfig;
import com.spring_food.springfood.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@Validated
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PaymentController {

    VNPayService vnPayService;

    // Endpoint này client sẽ gọi để lấy URL thanh toán - TẠM THỜI Bỏ AUTHENTICATION
    // @PreAuthorize("hasAnyRole({'ADMIN', 'CUSTOMER', 'SHOP_OWNER', 'STAFF'})")
    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(HttpServletRequest request, @RequestBody Map<String, Object> payload) {
        // Lấy thông tin từ payload gửi lên từ frontend
        long amount = Long.parseLong(payload.get("amount").toString());
        String orderInfo = payload.get("orderInfo").toString();
        String paymentUrl = "";
        try {
            paymentUrl = vnPayService.createPaymentUrl(request, amount, orderInfo);
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
        } else if (paymentStatus == 0) {
            redirectUrl = "http://localhost:3000/payment-failure"; // URL của trang thất bại trên frontend
        } else {
            redirectUrl = "http://localhost:3000/payment-invalid"; // URL của trang chữ ký không hợp lệ
        }

        return new RedirectView(redirectUrl);
    }

    // DEBUG endpoint - xóa sau khi sử xong
    @GetMapping("/debug-config")
    @PreAuthorize("hasAnyRole({'ADMIN', 'CUSTOMER', 'SHOP_OWNER', 'STAFF'})")
    public ResponseEntity<?> debugConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("vnp_PayUrl", VNPayConfig.vnp_PayUrl);
        config.put("vnp_Returnurl", VNPayConfig.vnp_Returnurl);
        config.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        config.put("vnp_HashSecret", VNPayConfig.vnp_HashSecret != null ? "***" + VNPayConfig.vnp_HashSecret.substring(Math.max(0, VNPayConfig.vnp_HashSecret.length() - 4)) : "NULL");
        config.put("vnp_apiUrl", VNPayConfig.vnp_apiUrl);

        log.info("VNPay DEBUG Config: {}", config);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/test-signature")
    @PreAuthorize("hasAnyRole({'ADMIN', 'CUSTOMER', 'SHOP_OWNER', 'STAFF'})")
    public ResponseEntity<?> testSignature() {
        // Test với dữ liệu mẫu từ VNPay
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "10000000");
        params.put("vnp_Command", "pay");
        params.put("vnp_CreateDate", "20210801153333");
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderInfo", "Thanh toan don hang:123456");
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", "https://domainmerchant.vn/ReturnUrl");
        params.put("vnp_TmnCode", "DEMOV210");
        params.put("vnp_TxnRef", "123456");
        params.put("vnp_Version", "2.1.0");

        // Sử dụng secret key mẫu của VNPay demo
        String testSecret = "RAOEXHYVSDDIIENYWSLDIIZTANXUXZFJ";
        String signature = VNPayConfig.hmacSHA512(testSecret, "vnp_Amount=10000000&vnp_Command=pay&vnp_CreateDate=20210801153333&vnp_CurrCode=VND&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_OrderInfo=Thanh toan don hang:123456&vnp_OrderType=other&vnp_ReturnUrl=https://domainmerchant.vn/ReturnUrl&vnp_TmnCode=DEMOV210&vnp_TxnRef=123456&vnp_Version=2.1.0");

        Map<String, String> result = new HashMap<>();
        result.put("expectedSignature", "cf40899a85ba0b5a8aacf8f1f29767785b5e7825a616e5c9b98c8b4bb8a9f8b5");
        result.put("actualSignature", signature);
        result.put("match", signature.equals("cf40899a85ba0b5a8aacf8f1f29767785b5e7825a616e5c9b98c8b4bb8a9f8b5") ? "YES" : "NO");

        return ResponseEntity.ok(result);
    }

    // TEST endpoint - KHÔNG CẦN AUTHENTICATION
    @GetMapping("/test-create-payment")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> testCreatePayment(HttpServletRequest request) {
        try {
            String paymentUrl = vnPayService.createPaymentUrl(request, 10000, "Test payment simple");
            return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl, "status", "SUCCESS"));
        } catch (Exception ex) {
            log.error("Test create payment error: ", ex);
            return ResponseEntity.ok(Map.of("error", ex.getMessage(), "status", "ERROR"));
        }
    }
}
