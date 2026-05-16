package com.theblood.paymentservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.common.enums.PaymentMethod;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import com.theblood.springfood.common.enums.TransactionStatus;
import com.theblood.paymentservice.common.enums.TransactionType;
import com.theblood.paymentservice.common.util.VNPayUtil;
import com.theblood.paymentservice.config.VNPayConfig;
import com.theblood.paymentservice.dto.request.PaymentInfoRequest;
import com.theblood.paymentservice.dto.request.VNPayPaymentRequest;
import com.theblood.paymentservice.model.PaymentTransactions;
import com.theblood.paymentservice.repository.PaymentRepository;
import com.theblood.paymentservice.repository.PaymentTransactionsRepository;
import com.theblood.paymentservice.service.PaymentService;
import com.theblood.paymentservice.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayServiceImpl implements VNPayService {

    PaymentService paymentService;
    PaymentRepository paymentRepository;
    PaymentTransactionsRepository paymentTransactionsRepository;
    //OrderRepository orderRepository;

    private String generateSecureHash(Map<String, String> params, String secretKey) throws UnsupportedEncodingException {
        // Theo sample của VNPay: sắp xếp tham số, nối field=value với GIÁ TRỊ ĐƯỢC URL-ENCODE UTF-8
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                // Encode GIÁ TRỊ theo UTF-8 để khớp với cách VNPay tính toán
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8);
                log.info("Adding to signature: {}={} (encoded)", fieldName, encodedValue);

                if (!isFirst) {
                    sb.append("&");
                }
                sb.append(fieldName);
                sb.append("=");
                sb.append(encodedValue);
                isFirst = false;
            } else {
                log.warn("Skipping empty parameter: {}", fieldName);
            }
        }

        String hashData = sb.toString();
        String signature = VNPayConfig.hmacSHA512(secretKey, hashData);


        return signature;
    }

    private String generateQueryDrSecureHash(Map<String, String> params, String secretKey) throws UnsupportedEncodingException {
        String data = params.get("vnp_RequestId") + "|" + params.get("vnp_Version") + "|" +
                params.get("vnp_Command") + "|" + params.get("vnp_TmnCode") + "|" +
                params.get("vnp_TxnRef") + "|" + params.get("vnp_TransactionDate") + "|" +
                params.get("vnp_CreateDate") + "|" + params.get("vnp_IpAddr") + "|" +
                params.get("vnp_OrderInfo");

        return VNPayConfig.hmacSHA512(secretKey, data);
    }

    private String generateRefundSecureHash(Map<String, String> params) {
        String data = params.get("vnp_RequestId") + "|" + params.get("vnp_Version") + "|" +
                params.get("vnp_Command") + "|" + params.get("vnp_TmnCode") + "|" +
                params.get("vnp_TransactionType") + "|" + params.get("vnp_TxnRef") + "|" +
                params.get("vnp_Amount") + "|" + params.get("vnp_TransactionNo") + "|" +
                params.get("vnp_TransactionDate") + "|" + params.get("vnp_CreateBy") + "|" +
                params.get("vnp_CreateDate") + "|" + params.get("vnp_IpAddr") + "|" +
                params.get("vnp_OrderInfo");

        return VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, data);
    }

    @Transactional
    @Override
    public String createPaymentUrl(HttpServletRequest request, VNPayPaymentRequest paymentRequest) throws UnsupportedEncodingException {
        if (paymentRequest == null) {
            throw new InvalidDataException("Payment request is required");
        }

        if (paymentRequest.getPaymentMethod() == null || !PaymentMethod.VNPAY.equals(paymentRequest.getPaymentMethod())) {
            throw new InvalidDataException("Payment method must be VNPAY");
        }

        if (paymentRequest.getAmount() == null || paymentRequest.getAmount() <= 0) {
            throw new InvalidDataException("Amount must be greater than zero");
        }

        if (paymentRequest.getOrderInfo() == null || paymentRequest.getOrderInfo().trim().isEmpty()) {
            throw new InvalidDataException("Order info is required");
        }

        String vnp_TxnRef = paymentRequest.getTxnRef();
        if (vnp_TxnRef == null || vnp_TxnRef.trim().isEmpty()) {
            log.error(" (vnp_TxnRef) is null or empty. Cannot create payment URL.");
            throw new IllegalArgumentException("vnp_TxnRef cannot be null or empty.");
        }

        long vnp_Amount = paymentRequest.getAmount() * 100;
        String vnp_IpAddr = VNPayConfig.getIpAddress(request);
        log.info("VNPay IpAddr: {}", vnp_IpAddr);

        Map<String, String> vnp_Params = new TreeMap<>(); // Dùng TreeMap để tự động sắp xếp theo alphabet
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        String returnUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + VNPayConfig.vnp_Returnurl;
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        log.info("VNPay ReturnUrl: {}", returnUrl);

        String cleanOrderInfo = paymentRequest.getOrderInfo()
                .replaceAll("[^a-zA-Z0-9\\s]", " ") // Chỉ giữ lại ký tự an toàn
                .replaceAll("\\s+", " ").trim();
        if (cleanOrderInfo.isEmpty()) {
            throw new InvalidDataException("Order info is invalid after sanitization");
        }
        vnp_Params.put("vnp_OrderInfo", cleanOrderInfo.substring(0, Math.min(cleanOrderInfo.length(), 255)));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));

        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        log.info("VNPay CreateDate: {}", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 30);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        log.info("VNPay ExpireDate: {}", vnp_ExpireDate);

        // Tạo chữ ký từ các tham số đã chuẩn bị
        String vnp_SecureHash = generateSecureHash(vnp_Params, VNPayConfig.vnp_HashSecret);

        // Build query string, URL-encode các giá trị
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            if (!query.isEmpty()) {
                query.append('&');
            }
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            query.append('=');
            query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        query.append("&vnp_SecureHashType=HmacSHA512"); // Thêm loại hash vào cuối, không cần ký

        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + query.toString();
        log.info("Generated VNPay URL: {}", paymentUrl);

        return paymentUrl;
    }

    @Transactional
    @Override
    public Map<String, String> queryTransactionStatus(String vnp_TxnRef, String vnp_TransactionNo, LocalDateTime vnp_TransDate, String requestIP) throws IOException {
        String vnp_RequestId = VNPayConfig.getRandomNumber(8);
        String vnp_Version = "2.1.0";
        String vnp_Command = "querydr";
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

        Map<String, String> vnp_params = new LinkedHashMap<>();
        vnp_params.put("vnp_RequestId", vnp_RequestId);
        vnp_params.put("vnp_Version", vnp_Version);
        vnp_params.put("vnp_Command", "querydr");
        vnp_params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_params.put("vnp_TxnRef", vnp_TxnRef);
        //vnp_params.put("vnp_TransactionNo", vnp_TransactionNo);
        vnp_params.put("vnp_TransactionDate", vnp_TransDate.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        vnp_params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        vnp_params.put("vnp_IpAddr", requestIP);
        vnp_params.put("vnp_OrderInfo", "Kiem tra trang thai cua giao dich " + vnp_TxnRef);

        // Create signature
        String vnp_SecureHash = generateQueryDrSecureHash(vnp_params, VNPayConfig.vnp_HashSecret);
        vnp_params.put("vnp_SecureHash", vnp_SecureHash);

        // Send request
        URL url = new URL(VNPayConfig.vnp_apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.writeBytes(new ObjectMapper().writeValueAsString(vnp_params));
            wr.flush();
        }

        StringBuilder response = new StringBuilder();

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            while ((inputLine = rd.readLine()) != null) {
                response.append(inputLine);
            }
        }
        // Convert JSON response -> Map
        Map<String, String> res = new ObjectMapper().readValue(response.toString(), new TypeReference<>() {
        });

        return res;
    }

    @Transactional
    @Override
    public Map<String, String> queryRefund(VNPayPaymentRequest vnPayPaymentRequest, String requestIP) throws IOException {
        String vnp_RequestId = VNPayConfig.getRandomNumber(8);
        String vnp_Version = "2.1.0";
        String vnp_Command = "refund";
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

        String vnp_TransactionType = "03"; // Default là hoàn tiền toàn phần

        Map<String, String> vnp_params = new LinkedHashMap<>();
        vnp_params.put("vnp_RequestId", vnp_RequestId);
        vnp_params.put("vnp_Version", vnp_Version);
        vnp_params.put("vnp_Command", vnp_Command);
        vnp_params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_params.put("vnp_TransactionType", vnp_TransactionType);
        vnp_params.put("vnp_TxnRef", vnPayPaymentRequest.getTxnRef());
        vnp_params.put("vnp_Amount", Long.toString(vnPayPaymentRequest.getAmount() * 100));
        vnp_params.put("vnp_TransactionNo", vnPayPaymentRequest.getTransactionNo());
        //   vnp_params.put("vnp_CreatedBy", vnPayPaymentRequest.getUserId());
        vnp_params.put("vnp_TransactionDate", vnPayPaymentRequest.getTransferDate().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        vnp_params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        vnp_params.put("vnp_IpAddr", requestIP);
        vnp_params.put("vnp_OrderInfo", "Hoan tien cho giao dich " + vnPayPaymentRequest.getTxnRef());

        // Create signature
        String vnp_SecureHash = generateRefundSecureHash(vnp_params);
        vnp_params.put("vnp_SecureHash", vnp_SecureHash);

        // Send request
        URL url = new URL(VNPayConfig.vnp_apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.writeBytes(new ObjectMapper().writeValueAsString(vnp_params));
            wr.flush();
        }

        StringBuilder response = new StringBuilder();

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            while ((inputLine = rd.readLine()) != null) {
                response.append(inputLine);
            }
        }

        // Convert JSON response -> Map
        Map<String, String> res = new ObjectMapper().readValue(response.toString(), new TypeReference<>() {
        });

        return res;
    }


    @Transactional
    @Override
    public String handlePaymentCheckingStatus(HttpServletRequest request, UUID userId, UUID paymentTransactionId) throws IOException {

        Optional<PaymentTransactions> paymentCheckingStatus = paymentTransactionsRepository.findById(paymentTransactionId);
        if (paymentCheckingStatus.isEmpty()) throw new InvalidDataException("Payment Transaction Not Found");

        if (!paymentCheckingStatus.get().getPaymentMethodName().equals(PaymentMethod.VNPAY.name()) || !paymentCheckingStatus.get().getStatus().equals(TransactionStatus.PENDING))
            throw new InvalidDataException("Payment transaction is not VNPAY method or not in PENDING status");

        String transactionStatus = "";

        Map<String, String> response =
                queryTransactionStatus(paymentCheckingStatus.get().getId().toString(), paymentCheckingStatus.get().getProviderTransactionRef(), paymentCheckingStatus.get().getCreatedAt(), VNPayConfig.getIpAddress(request));

        if (response.get("vnp_ResponseCode").equals("00")) {
            transactionStatus = VNPayUtil.getTransactionStatusDescription(response.get("vnp_TransactionStatus"));

        } else transactionStatus = "Send request fail: " + response.get("vnp_Message");

        return transactionStatus;
    }

    @Transactional
    @Override
    public String handlePaymentRefund(HttpServletRequest request, UUID userId, UUID referenceId) throws IOException {
        PaymentTransactions paymentRefund = null;
        PaymentTransactions paymentRefundSuccess = null;
        String transactionStatus = "";
        List<PaymentTransactions> paymentTransactions = paymentTransactionsRepository.findAllByReferenceId(referenceId);

        for (PaymentTransactions paymentTransaction : paymentTransactions) {
            if (paymentTransaction.getPaymentMethodName().equals(PaymentMethod.VNPAY.name()) && paymentTransaction.getSuccessAt() != null && paymentTransaction.getStatus().equals(TransactionStatus.PAID)) {
                paymentRefund = paymentTransaction;
                break;
            }
        }
        if (paymentRefund == null)
            throw new InvalidDataException("No VNPAY payment transaction found for this order. Order is not paid yet");

        PaymentInfoRequest paymentInfoRequest = new PaymentInfoRequest();
        paymentInfoRequest.setTransactionType(TransactionType.REFUND);
        paymentInfoRequest.setStatus(TransactionStatus.PENDING);
        paymentInfoRequest.setAmount(paymentRefund.getAmount().longValue());
        paymentInfoRequest.setUserId(userId);

        paymentRefundSuccess = paymentService.createPaymentTransaction(paymentInfoRequest);

        VNPayPaymentRequest vnpayRequest = new VNPayPaymentRequest();
        vnpayRequest.setAmount(paymentRefund.getAmount().longValue());
        // TxnRef = paymentTransaction.Id
        vnpayRequest.setTxnRef(paymentRefundSuccess.getId().toString());
        vnpayRequest.setTransferDate(paymentRefund.getSuccessAt());
        vnpayRequest.setPaymentMethod(PaymentMethod.VNPAY);
        vnpayRequest.setUserId(userId);
        vnpayRequest.setTransactionNo(paymentRefund.getProviderTransactionRef());
        Map<String, String> response =
                queryRefund(vnpayRequest, VNPayConfig.getIpAddress(request));

        if (response.get("vnp_ResponseCode").equals("00")) {
            transactionStatus = "Hoàn tiền thành công!";
            paymentService.updatePaymentTransaction(paymentRefundSuccess.getId(), paymentRefundSuccess.getProviderTransactionRef(), TransactionStatus.REFUNDED, LocalDateTime.now());

        } else transactionStatus = "Hoàn tiền thất bại:  " + response.get("vnp_Message");

        // request hoàn hàng cho order service qua gRPC

        return transactionStatus;
    }

    @Override
    @Transactional
    public int processVNPayReturn(HttpServletRequest request) {

        // 1. Thu thập tất cả các tham số từ VNPay một cách đáng tin cậy
        Map<String, String> paramsFromVnPay = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = (entry.getValue() != null && entry.getValue().length > 0) ? entry.getValue()[0] : "";
            if (!fieldValue.isEmpty()) {
                paramsFromVnPay.put(fieldName, fieldValue);
            }
        }

        log.info("Received all VNPay return params: {}", paramsFromVnPay);

        String receivedSecureHash = paramsFromVnPay.get("vnp_SecureHash");
        if (receivedSecureHash == null) {
            log.error("VNPay return error: vnp_SecureHash is missing for transaction ref: {}", paramsFromVnPay.get("vnp_TxnRef"));
            return -1; // Coi như sai chữ ký nếu không có hash
        }

        // 2. Chuẩn bị dữ liệu để tính toán lại chữ ký
        Map<String, String> paramsForHash = new HashMap<>(paramsFromVnPay);
        paramsForHash.remove("vnp_SecureHash");
        paramsForHash.remove("vnp_SecureHashType");

        try {
            String calculatedHash = generateSecureHash(paramsForHash, VNPayConfig.vnp_HashSecret);

            // 3. So sánh chữ ký
            if (!calculatedHash.equals(receivedSecureHash)) {
                log.error("INVALID VNPay signature for transaction ref: {}", paramsFromVnPay.get("vnp_TxnRef"));
                return -1; // -1: Chữ ký không hợp lệ
            }

            // Chữ ký hợp lệ, bây giờ xử lý nghiệp vụ
            log.info("VNPay signature is valid for transaction ref: {}", paramsFromVnPay.get("vnp_TxnRef"));

            if ("00".equals(paramsFromVnPay.get("vnp_ResponseCode"))) {
                // Giao dịch thành công -> Gọi service nghiệp vụ để xử lý thành công
                // Truyền toàn bộ Map data vào để service có thể lấy bất kỳ thông tin gì nó cần
                paymentService.handlePaymentReturnSuccess(paramsFromVnPay);
                return 1; // 1: Thành công
            } else {
                // Giao dịch thất bại -> Gọi service nghiệp vụ để xử lý thất bại
                paymentService.handlePaymentReturnFail(paramsFromVnPay);
                return 0; // 0: Thất bại
            }

        } catch (Exception e) {
            log.error("System error during VNPay return processing for transaction ref: {}", paramsFromVnPay.get("vnp_TxnRef"), e);
            return -2; // -2: Lỗi hệ thống
        }
    }
}
