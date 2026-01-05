package com.theblood.orderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.theblood.common.dto.request.CustomUserPrincipal;
import com.theblood.common.dto.response.ResponseData;
import com.theblood.orderservice.dto.request.OrderRequest;
import com.theblood.orderservice.dto.response.OrderPaymentResponse;
import com.theblood.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.TransactionRolledbackException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

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
        } catch (TransactionRolledbackException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
