package com.theblood.paymentservice.config;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class VNPayConfig {
    public static String vnp_PayUrl;
    public static String vnp_Returnurl;
    public static String vnp_TmnCode;
    public static String vnp_HashSecret;
    public static String vnp_apiUrl;

    public static String hashAllFields(Map fields) {
        List fieldNames = new ArrayList(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return hmacSHA512(vnp_HashSecret, sb.toString());
    }

    public static String hmacSHA512(final String key, final String data) {
        try {

            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            // Convert IPv6 localhost to IPv4
            if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
                ipAddress = "127.0.0.1";
            }
        } catch (Exception e) {
            ipAddress = "127.0.0.1"; // Default to IPv4 localhost
        }
        return ipAddress;
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Value("${vnpay.pay-url}")
    public void setVnpPayUrl(String vnpPayUrl) {
        VNPayConfig.vnp_PayUrl = vnpPayUrl;
    }

    @Value("${vnpay.return-url}")
    public void setVnpReturnurl(String vnpReturnurl) {
        VNPayConfig.vnp_Returnurl = vnpReturnurl;
    }

    @Value("${vnpay.tmn-code}")
    public void setVnpTmnCode(String vnpTmnCode) {
        VNPayConfig.vnp_TmnCode = vnpTmnCode;
    }

    @Value("${vnpay.hash-secret}")
    public void setVnpHashSecret(String vnpHashSecret) {
        VNPayConfig.vnp_HashSecret = vnpHashSecret;
    }

    @Value("${vnpay.api-url}")
    public void setVnpApiUrl(String vnpApiUrl) {
        VNPayConfig.vnp_apiUrl = vnpApiUrl;
    }
}