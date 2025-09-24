package com.spring_food.springfood.controller;

import com.spring_food.springfood.dto.request.OrderRequest;
import com.spring_food.springfood.dto.response.OrderDetailResponse;
import com.spring_food.springfood.dto.response.OrderPaymentResponse;
import com.spring_food.springfood.dto.response.ResponseData;
import com.spring_food.springfood.model.User;
import com.spring_food.springfood.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/api/order")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Validated
public class OrderController {


    OrderService orderService;

    @GetMapping("/")
    public ResponseEntity<ResponseData<Page<OrderDetailResponse>>> getListOrderForAdmin(
            @PageableDefault(size = 10, page = 0, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        return null;
    }

    @PreAuthorize("hasAnyRole({'ADMIN', 'CUSTOMER', 'STAFF', 'SHOP_OWNER'})")
    @PostMapping("/checkout")
    public ResponseEntity<ResponseData<OrderPaymentResponse>> checkout(
            @Valid @RequestBody OrderRequest orderRequest,
            HttpServletRequest request,
            @AuthenticationPrincipal User user
    ) {
        try {
            OrderPaymentResponse orderDetailResponse = orderService.createOrders(request, orderRequest, user.getId());
            return ResponseEntity.ok(new ResponseData<>(201, "Create order successfully", orderDetailResponse));
        } catch (UnsupportedEncodingException ex) {
            return new ResponseEntity<>(new ResponseData<>(400, "create order failed : " + ex.getMessage(), null), HttpStatus.OK);
        }
    }


}
