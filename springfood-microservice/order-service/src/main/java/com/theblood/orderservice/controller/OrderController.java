package com.theblood.orderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.response.ResponseData;
import com.theblood.springfood.common.enums.OrderStatus;
import com.theblood.orderservice.dto.request.OrderRequest;
import com.theblood.orderservice.dto.request.OrderStatusUpdateRequest;
import com.theblood.orderservice.dto.response.OrderDetailResponse;
import com.theblood.orderservice.dto.response.OrderPaymentResponse;
import com.theblood.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.TransactionRolledbackException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@RestController
@RequestMapping("/order")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Validated
public class OrderController {

    OrderService orderService;

    @PreAuthorize("hasAnyRole({'ADMIN', 'CUSTOMER', 'STAFF', 'SHOP_OWNER'})")
    @PostMapping("/checkout")
    public ResponseEntity<ResponseData<OrderPaymentResponse>> checkout(
            @Valid @RequestBody OrderRequest orderRequest,
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        try {
            OrderPaymentResponse orderDetailResponse = orderService.createOrders(request, orderRequest, user.getUserId().toString());
            return ResponseEntity.ok(new ResponseData<>(201, "Create order successfully", orderDetailResponse));
        } catch (UnsupportedEncodingException ex) {
            return new ResponseEntity<>(new ResponseData<>(400, "create order failed : " + ex.getMessage(), null), HttpStatus.OK);
        } catch (TransactionRolledbackException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasAnyRole({'SHOP_OWNER', 'STAFF', 'ADMIN'})")
    @GetMapping("/shop")
    public ResponseEntity<ResponseData<Page<OrderDetailResponse>>> getShopOrders(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        if (user.getShopId() == null) {
            return new ResponseEntity<>(new ResponseData<>(400, "Shop not found", null), HttpStatus.BAD_REQUEST);
        }
        Page<OrderDetailResponse> orders = orderService.getListOrderForShop(pageable, UUID.fromString(user.getShopId()));
        return ResponseEntity.ok(new ResponseData<>(200, "Get orders successfully", orders));
    }

    @PreAuthorize("hasAnyRole({'SHOP_OWNER', 'STAFF', 'ADMIN'})")
    @GetMapping("/shop/{id}")
    public ResponseEntity<ResponseData<OrderDetailResponse>> getShopOrderDetail(
            @PathVariable("id") UUID orderId,
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        if (user.getShopId() == null) {
            return new ResponseEntity<>(new ResponseData<>(400, "Shop not found", null), HttpStatus.BAD_REQUEST);
        }
        OrderDetailResponse order = orderService.getOrderDetailByOrderId(orderId, UUID.fromString(user.getShopId()));
        return ResponseEntity.ok(new ResponseData<>(200, "Get order detail successfully", order));
    }

    /**
     * Shop-side status transition.
     *
     * <p>The legal target statuses depend on the order's current status, see
     * {@code OrderStatusValidationUtil#getValidStatusTransition}. The service
     * also enforces shop ownership before doing anything else.</p>
     */
    @PreAuthorize("hasAnyRole({'SHOP_OWNER', 'STAFF', 'ADMIN'})")
    @PutMapping("/shop/{id}/status")
    public ResponseEntity<ResponseData<OrderDetailResponse>> updateShopOrderStatus(
            @PathVariable("id") UUID orderId,
            @AuthenticationPrincipal CustomUserPrincipal user,
            @Valid @RequestBody OrderStatusUpdateRequest body
    ) {
        if (user.getShopId() == null) {
            return new ResponseEntity<>(new ResponseData<>(400, "Shop not found", null), HttpStatus.BAD_REQUEST);
        }
        OrderDetailResponse updated = orderService.updateShopOrderStatus(
            orderId,
            UUID.fromString(user.getShopId()),
            body.getTargetStatus(),
            body.getReason()
        );
        return ResponseEntity.ok(new ResponseData<>(200, "Order status updated", updated));
    }

    /**
     * Shop owner phê duyệt 1 order. Chuyển trạng thái sang CONFIRMED và push realtime
     * notify tới user đặt hàng.
     */
    @PreAuthorize("hasAnyRole({'SHOP_OWNER', 'STAFF', 'ADMIN'})")
    @PatchMapping("/shop/{id}/approve")
    public ResponseEntity<ResponseData<OrderDetailResponse>> approveShopOrder(
            @PathVariable("id") UUID orderId,
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        if (user.getShopId() == null) {
            return new ResponseEntity<>(new ResponseData<>(400, "Shop not found", null), HttpStatus.BAD_REQUEST);
        }
        OrderDetailResponse order = orderService.approveOrder(orderId, UUID.fromString(user.getShopId()));
        return ResponseEntity.ok(new ResponseData<>(200, "Order approved successfully", order));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/history")
    public ResponseEntity<ResponseData<Page<OrderDetailResponse>>> getOrderHistory(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        Page<OrderDetailResponse> orders = orderService.getListOrderForUser(pageable, user.getUserId());
        return ResponseEntity.ok(new ResponseData<>(200, "Get order history successfully", orders));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/history/{id}")
    public ResponseEntity<ResponseData<OrderDetailResponse>> getOrderDetail(
            @PathVariable("id") UUID orderId,
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        OrderDetailResponse order = orderService.getOrderDetailForUser(orderId, user.getUserId());
        return ResponseEntity.ok(new ResponseData<>(200, "Get order detail successfully", order));
    }
}
