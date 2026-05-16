package com.theblood.paymentservice.controller;


import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.response.ResponseData;
import com.theblood.springfood.common.enums.PaymentMethod;
import com.theblood.paymentservice.dto.request.BankAccountCreateRequest;
import com.theblood.paymentservice.dto.request.VNPayPaymentRequest;
import com.theblood.paymentservice.dto.response.BankAccountResponse;
import com.theblood.paymentservice.model.PaymentTransactions;
import com.theblood.paymentservice.repository.PaymentTransactionsRepository;
import com.theblood.paymentservice.service.OrderQueryService;
import com.theblood.paymentservice.service.PaymentService;
import com.theblood.paymentservice.service.VNPayService;
import com.theblood.springfood.common.enums.TransactionStatus;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payment")
@Validated
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PaymentController {

    VNPayService vnPayService;
    PaymentService paymentService;
    OrderQueryService orderQueryService;
    PaymentTransactionsRepository paymentTransactionsRepository;

    @lombok.experimental.NonFinal
    @org.springframework.beans.factory.annotation.Value("${vnpay.frontend-return-url:}")
    String frontendReturnUrl;


    @GetMapping("/vnpay/status/{paymentTransactionId}")
    @PreAuthorize("hasAnyRole({'ADMIN', 'CUSTOMER', 'SHOP_OWNER', 'STAFF'})")
    public ResponseEntity<ResponseData<?>> handlePaymentStatus(HttpServletRequest request,
                                                               @PathVariable UUID paymentTransactionId,
                                                               @AuthenticationPrincipal CustomUserPrincipal user) throws IOException {
        String message = vnPayService.handlePaymentCheckingStatus(request, user.getUserId(), paymentTransactionId);
        return ResponseEntity.ok(new ResponseData<>(200, "Query payment status successfully", message));
    }

    @PostMapping("/vnpay/refund/{referenceId}")
    @PreAuthorize("hasAnyRole({'ADMIN', 'SHOP_OWNER'})")
    public ResponseEntity<ResponseData<?>> refundPaymentForOrder(HttpServletRequest request,
                                                                 @PathVariable UUID referenceId,
                                                                 @AuthenticationPrincipal CustomUserPrincipal user) throws IOException {
        String message = vnPayService.handlePaymentRefund(request, user.getUserId(), referenceId);
        return ResponseEntity.ok(new ResponseData<>(200, "Refund requested", message));
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

    /**
     * Tạo URL VNPay cho 1 order ĐÃ TỒN TẠI trong DB (đọc trực tiếp từ Neon
     * qua JdbcTemplate).
     *
     * <p>Use case: Demo / fix lỗi payment flow — không cần đi qua /order/checkout
     * trước, chỉ cần biết orderId. Service sẽ:
     * <ol>
     *   <li>Đọc <code>springfood_order.orders</code> qua JdbcTemplate.</li>
     *   <li>Tạo PaymentTransaction mới với status PENDING, reference_id = orderId.</li>
     *   <li>Gọi VNPay sandbox để build payment URL với
     *       <code>vnp_TxnRef = paymentTransactionId</code>.</li>
     * </ol>
     */
    /**
     * Tạo URL VNPay cho 1 PaymentTransaction đã tồn tại.
     * <p>Use case chính: sau khi /order/checkout trả về `referenceId` (chính
     * là paymentTransactionId), FE gọi endpoint này để lấy URL VNPay rồi
     * redirect — không phải tự build {@link VNPayPaymentRequest}.
     *
     * <p>Nếu transaction đang PENDING thì re-use, nếu đã PAID/FAILED thì 400.
     */
    @PostMapping("/vnpay/from-reference/{paymentTransactionId}")
    public ResponseEntity<ResponseData<Map<String, Object>>> payByReference(
            HttpServletRequest request,
            @PathVariable("paymentTransactionId") UUID paymentTransactionId
    ) {
        try {
            PaymentTransactions tx = paymentTransactionsRepository.findById(paymentTransactionId)
                    .orElseThrow(() -> new InvalidDataException(
                            "PaymentTransaction not found: " + paymentTransactionId));

            if (tx.getStatus() != null && tx.getStatus() != TransactionStatus.PENDING) {
                throw new InvalidDataException(
                        "PaymentTransaction is not pending, current status: " + tx.getStatus());
            }
            if (tx.getAmount() == null || tx.getAmount().signum() <= 0) {
                throw new InvalidDataException("PaymentTransaction has invalid amount");
            }

            // Force VNPAY method (đề phòng tx được tạo cho method khác trước đó)
            tx.setPaymentMethodName(PaymentMethod.VNPAY.name());
            paymentTransactionsRepository.save(tx);

            VNPayPaymentRequest vnpRequest = new VNPayPaymentRequest();
            vnpRequest.setPaymentMethod(PaymentMethod.VNPAY);
            vnpRequest.setAmount(tx.getAmount().longValue());
            vnpRequest.setOrderInfo("Thanh toan giao dich " + tx.getId());
            vnpRequest.setTxnRef(tx.getId().toString());
            if (tx.getUserId() != null) {
                try {
                    vnpRequest.setUserId(UUID.fromString(tx.getUserId()));
                } catch (IllegalArgumentException ignore) {
                    // user_id ở payment_transactions là varchar; có thể không phải UUID hợp lệ
                }
            }
            vnpRequest.setTransferDate(java.time.LocalDateTime.now());

            String paymentUrl = vnPayService.createPaymentUrl(request, vnpRequest);

            Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("paymentTransactionId", tx.getId().toString());
            data.put("amount", tx.getAmount());
            data.put("paymentUrl", paymentUrl);
            return ResponseEntity.ok(new ResponseData<>(200, "Payment URL created", data));
        } catch (InvalidDataException ex) {
            log.warn("payByReference validation failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("payByReference failed for tx={}", paymentTransactionId, ex);
            Map<String, Object> err = new java.util.LinkedHashMap<>();
            err.put("paymentTransactionId", paymentTransactionId.toString());
            err.put("error", ex.getClass().getSimpleName());
            err.put("message", ex.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(500, "Failed to create VNPay URL", err));
        }
    }

    @PostMapping("/vnpay/pay-existing-order/{orderId}")
    public ResponseEntity<ResponseData<Map<String, Object>>> payExistingOrder(
            HttpServletRequest request,
            @PathVariable("orderId") UUID orderId
    ) {
        try {
            OrderQueryService.OrderSnapshot order = orderQueryService.findById(orderId)
                    .orElseThrow(() -> new InvalidDataException("Order not found: " + orderId));

            if (order.finalPrice() == null || order.finalPrice().signum() <= 0) {
                throw new InvalidDataException("Order has invalid amount: " + order.finalPrice());
            }

            // Tạo PaymentTransaction mới (PENDING) để có 1 ID dùng làm vnp_TxnRef
            PaymentTransactions tx = new PaymentTransactions();
            tx.setUserId(order.userId() != null ? order.userId().toString() : null);
            tx.setPaymentMethodName(PaymentMethod.VNPAY.name());
            tx.setAmount(order.finalPrice());
            tx.setStatus(TransactionStatus.PENDING);
            tx.setReferenceType("ORDER");
            tx.setReferenceId(order.orderId());
            // Set createdAt explicitly để không phụ thuộc @EnableJpaAuditing
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            tx.setCreatedAt(now);
            tx.setUpdatedAt(now);
            PaymentTransactions saved = paymentTransactionsRepository.save(tx);
            log.info("Created PaymentTransaction id={} for orderId={} amount={}",
                    saved.getId(), order.orderId(), order.finalPrice());

            // Build URL VNPay
            VNPayPaymentRequest vnpRequest = new VNPayPaymentRequest();
            vnpRequest.setPaymentMethod(PaymentMethod.VNPAY);
            vnpRequest.setAmount(order.finalPrice().longValue());
            vnpRequest.setOrderInfo("Thanh toan don hang " + order.orderId());
            vnpRequest.setTxnRef(saved.getId().toString());
            vnpRequest.setUserId(order.userId());
            vnpRequest.setTransferDate(now);

            String paymentUrl = vnPayService.createPaymentUrl(request, vnpRequest);

            Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("orderId", order.orderId().toString());
            data.put("paymentTransactionId", saved.getId().toString());
            data.put("amount", order.finalPrice());
            data.put("paymentUrl", paymentUrl);
            return ResponseEntity.ok(new ResponseData<>(200, "Payment URL created", data));
        } catch (InvalidDataException ex) {
            log.warn("payExistingOrder validation failed: {}", ex.getMessage());
            throw ex; // GlobalHandleException sẽ map → 400
        } catch (Exception ex) {
            log.error("payExistingOrder failed for orderId={}", orderId, ex);
            Map<String, Object> err = new java.util.LinkedHashMap<>();
            err.put("orderId", orderId.toString());
            err.put("error", ex.getClass().getSimpleName());
            err.put("message", ex.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(500, "Failed to create VNPay URL", err));
        }
    }

    @PreAuthorize("hasAnyRole({'ADMIN', 'SHOP_OWNER'})")
    @PostMapping("/bank-accounts")
    public ResponseEntity<?> createBankAccount(@RequestBody BankAccountCreateRequest request) {
        BankAccountResponse response = paymentService.createBankAccount(request);
        return ResponseEntity.ok(new ResponseData<>(200, "Bank account created", response));
    }

    // Endpoint này VNPay sẽ redirect về sau khi thanh toán
    // Nó phải khớp với `vnp_Returnurl` trong config
    @GetMapping("/vnpay-payment-return/")
    public ResponseEntity<?> handleVnpayReturn(HttpServletRequest request) {
        int paymentStatus = vnPayService.processVNPayReturn(request);

        String message;
        if (paymentStatus == 1) {
            message = "Payment successful";
            log.info("payment complete without error");
        } else if (paymentStatus == 0) {
            message = "Payment failed";
            log.warn("payment return a unreachable error");
        } else if (paymentStatus == -1) {
            message = "Invalid VNPay signature";
            log.error("invalid signature");
        } else {
            message = "Payment processing error";
            log.error("payment return system error");
        }

        // Nếu có config FE return URL → 302 redirect kèm các query params gốc của VNPay
        // (vnp_TxnRef, vnp_ResponseCode, vnp_Amount, ...) và `paymentStatus`.
        // FE sẽ render kết quả tương ứng. Nếu không có config → giữ behaviour cũ (trả JSON).
        if (frontendReturnUrl != null && !frontendReturnUrl.isBlank()) {
            String redirectUrl = buildFrontendRedirectUrl(request, paymentStatus);
            log.info("Redirecting VNPay return to FE: {}", redirectUrl);
            return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                    .location(java.net.URI.create(redirectUrl))
                    .build();
        }

        return ResponseEntity.ok(new ResponseData<>(200, message, Map.of("status", paymentStatus)));
    }

    /**
     * Build URL FE từ template (có thể có sẵn ?abc=...) + tất cả query param VNPay
     * gửi về + paymentStatus do BE đánh giá.
     */
    private String buildFrontendRedirectUrl(HttpServletRequest request, int paymentStatus) {
        StringBuilder sb = new StringBuilder(frontendReturnUrl);
        sb.append(frontendReturnUrl.contains("?") ? '&' : '?');

        boolean first = true;
        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (values == null || values.length == 0) continue;
            if (!first) sb.append('&');
            sb.append(java.net.URLEncoder.encode(key, java.nio.charset.StandardCharsets.UTF_8))
              .append('=')
              .append(java.net.URLEncoder.encode(values[0], java.nio.charset.StandardCharsets.UTF_8));
            first = false;
        }
        if (!first) sb.append('&');
        sb.append("paymentStatus=").append(paymentStatus);
        return sb.toString();
    }
}
