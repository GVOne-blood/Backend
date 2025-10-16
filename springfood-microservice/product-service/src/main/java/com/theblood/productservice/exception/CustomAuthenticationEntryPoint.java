package com.theblood.productservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // --- BƯỚC QUAN TRỌNG ĐỂ DEBUG VÀ LẤY LỖI GỐC ---
        Throwable rootCause = authException.getCause();
        String errorMessage = authException.getMessage();

        if (rootCause != null) {
            // Nếu có lỗi gốc được gói bên trong, lấy message của nó
            errorMessage = rootCause.getMessage();
        }

        // Ghi log lỗi đầy đủ ra console để debug
        log.error("Authentication failed. EntryPoint triggered. Root cause: {}", errorMessage, authException);
        authException.printStackTrace(response.getWriter());
        // --------------------------------------------------------

        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        // Sử dụng errorMessage đã được lấy ra một cách chính xác
        body.put("message", "Yêu cầu xác thực không thành công: " + errorMessage);
        body.put("path", request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}