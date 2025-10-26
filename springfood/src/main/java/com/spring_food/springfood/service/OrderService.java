package com.spring_food.springfood.service;

import com.spring_food.springfood.dto.request.OrderRequest;
import com.spring_food.springfood.dto.request.OrdersUpdateRequest;
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

    OrderPaymentResponse updatePendingOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest) throws UnsupportedEncodingException;

    OrderPaymentResponse updatePaymentPendingOrders(OrdersUpdateRequest updateRequest);

    OrderPaymentResponse updateConfirmedOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId);

    OrderPaymentResponse updateProcessingOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId);

    OrderPaymentResponse updateReadyPickupOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId);

    OrderPaymentResponse updateShippingOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId);

    OrderPaymentResponse updateCompletedOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId);

    OrderPaymentResponse updateOrderReturnOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId);

    void deleteOrder(String orderId);
}
