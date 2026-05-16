package com.theblood.orderservice.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.theblood.orderservice.dto.request.OrderRequest;
import com.theblood.orderservice.dto.request.OrdersUpdateRequest;
import com.theblood.orderservice.dto.response.OrderDetailResponse;
import com.theblood.orderservice.dto.response.OrderPaymentResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.TransactionRolledbackException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public interface OrderService {

    Page<OrderDetailResponse> getListOrderForAdmin(Pageable pageable, String adminId);

    Page<OrderDetailResponse> getListOrderForShop(Pageable pageable, UUID shopId);

    Page<OrderDetailResponse> getListOrderForUser(Pageable pageable, UUID userId);

    OrderDetailResponse getOrderDetailForUser(UUID orderId, UUID userId);

    OrderDetailResponse getOrderDetailByOrderId(UUID orderId, UUID shopId);

    OrderPaymentResponse createOrders(HttpServletRequest request, OrderRequest orderRequest, String user) throws UnsupportedEncodingException, TransactionRolledbackException, JsonProcessingException;

    OrderPaymentResponse updatePendingOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest) throws UnsupportedEncodingException;

    /**
     * Shop owner phê duyệt 1 order: chuyển PENDING / PENDING_PAYMENT / PROCESSING → CONFIRMED
     * và push realtime notify cho user đặt hàng.
     */
    OrderDetailResponse approveOrder(UUID orderId, UUID shopId);

    OrderPaymentResponse updatePaymentPendingOrders(OrdersUpdateRequest updateRequest);

    OrderPaymentResponse updateConfirmedOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId);

    OrderPaymentResponse updateProcessingOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId);

    OrderPaymentResponse updateReadyPickupOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId);

    OrderPaymentResponse updateShippingOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId);

    OrderPaymentResponse updateCompletedOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId);

    OrderPaymentResponse updateOrderReturnOrders(HttpServletRequest request, OrdersUpdateRequest updateRequest, String userId);

    /**
     * Transition the status of a single shop order. Verifies that the order
     * belongs to {@code shopId} and that the requested transition is legal
     * according to {@code OrderStatusValidationUtil}.
     *
     * @return the updated order projection.
     */
    OrderDetailResponse updateShopOrderStatus(UUID orderId, UUID shopId, com.theblood.springfood.common.enums.OrderStatus targetStatus, String reason);

    void deleteOrder(String orderId);
}
