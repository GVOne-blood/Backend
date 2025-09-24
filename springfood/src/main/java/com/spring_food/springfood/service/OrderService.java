package com.spring_food.springfood.service;

import com.spring_food.springfood.dto.request.OrderRequest;
import com.spring_food.springfood.dto.response.OrderDetailResponse;
import com.spring_food.springfood.dto.response.OrderPaymentResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.UnsupportedEncodingException;

public interface OrderService {

    Page<OrderDetailResponse> getListOrderForAdmin(Pageable pageable, String adminId);

    Page<OrderDetailResponse> getListOrderForShop(Pageable pageable, String ShopOwnerId);

    Page<OrderDetailResponse> getListOrderForUser(Pageable pageable, String userId);


    OrderPaymentResponse createOrders(HttpServletRequest request, OrderRequest orderRequest, String user) throws UnsupportedEncodingException;

    void deleteOrder(String orderId);
}
