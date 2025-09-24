//package com.spring_food.springfood.config; // Đặt trong package config
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import org.springframework.web.servlet.HandlerExceptionResolver;
//
//import java.io.IOException;
//
//@Component
//@Slf4j
/// / không nên có thằng RequiredArgs...
//public class ExceptionHandlingFilter extends OncePerRequestFilter {
//
//    @Qualifier("handlerExceptionResolver")
//    private HandlerExceptionResolver resolver;
//
//    @Override
//    protected void doFilterInternal(
//            @NonNull HttpServletRequest request,
//            @NonNull HttpServletResponse response,
//            @NonNull FilterChain filterChain
//    ) throws ServletException, IOException {
//        try {
//            // Chuyển request cho filter tiếp theo trong chuỗi
//            filterChain.doFilter(request, response);
//        } catch (Exception e) {
//            // Nếu có bất kỳ exception nào từ các filter sau (PreFilter, etc.)
//            // nó sẽ được bắt ở đây.
//            log.error("Đã có lỗi xảy ra tại tầng Security Filter: {}", e.getMessage());
//            // Ủy quyền cho GlobalExceptionHandler xử lý thông qua HandlerExceptionResolver.
//            //resolver.resolveException(request, response, null, e);
//
//
//        }
//    }
//}