package com.theblood.springfood.common.util;

import jakarta.servlet.http.HttpServletRequest;

public class CommonHttpRequestUtil {
    /**
     * Lấy IP address từ HTTP request.
     * Kiểm tra các header phổ biến để lấy IP thực sự trong trường hợp có proxy/load balancer.
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Lấy IP đầu tiên nếu có nhiều IP (separated by comma)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return normalizeIpAddress(ip);
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return normalizeIpAddress(remoteAddr);
    }


    /**
     * Normalize IP address.
     */
    private static String normalizeIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "unknown";
        }

        return ip;
    }

    /**
     * Get User-Agent from HTTP request.
     *
     * @param request the HTTP servlet request
     * @return the user agent string
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader("User-Agent");
    }
}
