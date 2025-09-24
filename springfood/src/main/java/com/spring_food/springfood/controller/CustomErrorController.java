package com.spring_food.springfood.controller; // Đặt trong package controller

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Đây là "cây cầu" kiến trúc giữa tầng Filter và tầng MVC.
 * Khi một exception chưa được xử lý thoát ra khỏi Filter Chain, Servlet Container (Tomcat)
 * sẽ chuyển hướng request đến endpoint /error.
 * <p>
 * Controller này sẽ bắt request đó, lấy ra exception gốc và NÉM NÓ LẠI VÀO
 * trong context của Spring MVC, cho phép GlobalExceptionHandler bắt được.
 */
@Controller
public class CustomErrorController implements ErrorController {

    // Đây là key chuẩn của Servlet API để lưu exception gốc.
    private static final String ERROR_ATTRIBUTE = "jakarta.servlet.error.exception";

    @RequestMapping("/error")
    public void handleError(final HttpServletRequest request) throws Throwable {
        // Lấy ra exception gốc mà Tomcat đã đính kèm vào request.
        final Throwable exception = (Throwable) request.getAttribute(ERROR_ATTRIBUTE);

        if (exception != null) {
            throw exception;
        }

        // Trường hợp ai đó truy cập trực tiếp /error
        throw new Exception("Endpoint không tồn tại hoặc có lỗi không xác định.");
    }
}