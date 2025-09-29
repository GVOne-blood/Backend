package com.spring_food.springfood.service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring_food.springfood.config.VNPayConfig;
import com.spring_food.springfood.dto.request.VNPayPaymentRequest;
import com.spring_food.springfood.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    private String generateSecureHash(Map<String, String> params, String secretKey) throws UnsupportedEncodingException {
        // Theo sample của VNPay: sắp xếp tham số, nối field=value với GIÁ TRỊ ĐƯỢC URL-ENCODE UTF-8
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        log.info("=== DEBUG VNPay Signature Generation ===");
        log.info("Parameters to sign (sorted): {}", fieldNames);

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
        log.info("Final signature string (encoded values): [{}]", hashData);
        log.info("Secret key (last 4 chars): ***{}", secretKey != null ? secretKey.substring(Math.max(0, secretKey.length() - 4)) : "NULL");

        String signature = VNPayConfig.hmacSHA512(secretKey, hashData);
        log.info("Generated signature: {}", signature);
        log.info("=== END DEBUG ===");

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
        String vnp_TxnRef = paymentRequest.getTxnRef();
        if (vnp_TxnRef == null || vnp_TxnRef.trim().isEmpty()) {
            log.error("User id (vnp_TxnRef) is null or empty. Cannot create payment URL.");
            throw new IllegalArgumentException("User id cannot be null or empty.");
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

    @Override
    public Map<String, String> queryTransactionStatus(String vnp_TxnRef, LocalDateTime vnp_TransDate, String requestIP) throws IOException {
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

    @Override
    public Map<String, String> queryRefund(VNPayPaymentRequest vnPayPaymentRequest, String requestIP) throws IOException {
        String vnp_RequestId = VNPayConfig.getRandomNumber(8);
        String vnp_Version = "2.1.0";
        String vnp_Command = "refund";
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

        String vnp_TransactionType = "02"; // Default là hoàn tiền toàn phần

        Map<String, String> vnp_params = new LinkedHashMap<>();
        vnp_params.put("vnp_RequestId", vnp_RequestId);
        vnp_params.put("vnp_Version", vnp_Version);
        vnp_params.put("vnp_Command", "querydr");
        vnp_params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_params.put("vnp_TransactionType", vnp_TransactionType);
        vnp_params.put("vnp_TxnRef", vnPayPaymentRequest.getTxnRef());
        vnp_params.put("vnp_Amount", Long.toString(vnPayPaymentRequest.getAmount() * 100));
        vnp_params.put("vnp_TransactionNo", vnPayPaymentRequest.getTransactionNo());
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

    @Override
    public int processVNPayReturn(HttpServletRequest request) {
        Map<String, String> paramsFromVnPay = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                paramsFromVnPay.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = paramsFromVnPay.remove("vnp_SecureHash"); // Lấy ra và xóa khỏi map
        paramsFromVnPay.remove("vnp_SecureHashType"); // Xóa khỏi map

        // --- Sử DỤNG LẠI HÀM CHUNG ĐỂ TẠO CHỮ KÝ TỪ Dữ LIỆU TRẢ VỀ ---
        String calculatedHash;
        try {
            calculatedHash = generateSecureHash(paramsFromVnPay, VNPayConfig.vnp_HashSecret);
        } catch (UnsupportedEncodingException e) {
            log.error("Encoding error during signature verification", e);
            return -2;
        }

        log.debug("VNPay return - received hash: {} vs calculated: {}", vnp_SecureHash, calculatedHash);

        // So sánh chữ ký để xác thực
        if (calculatedHash.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                // paymentService.handlePaymentReturnSuccess();
                // TODO: Logic nghiệp vụ khi thanh toán thành công
                return 1; // Thành công
            } else {
                //paymentService.handlePaymentReturnFail();
                // TODO: Logic nghiệp vụ khi thanh toán thất bại
                return 0; // Thất bại
            }
        } else {
            return -1; // Chữ ký không hợp lệ
        }
    }
}