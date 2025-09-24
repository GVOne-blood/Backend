package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.common.enums.OrderStatus;
import com.spring_food.springfood.common.enums.PaymentMethod;
import com.spring_food.springfood.common.enums.TransactionType;
import com.spring_food.springfood.common.util.VNPayUtil;
import com.spring_food.springfood.config.VNPayConfig;
import com.spring_food.springfood.dto.request.OrderUpdateRequest;
import com.spring_food.springfood.exception.custom.InvalidDataException;
import com.spring_food.springfood.model.Order;
import com.spring_food.springfood.repository.OrderRepository;
import com.spring_food.springfood.service.OrderService;
import com.spring_food.springfood.service.PaymentService;
import com.spring_food.springfood.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    OrderService orderService;
    OrderRepository orderRepository;
    VNPayService vnPayService;

    @Override
    public void handlePaymentReturnSuccess(String orderId, Map<String, String> response) {
        OrderUpdateRequest updateRequest = new OrderUpdateRequest();

        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) throw new InvalidDataException("Order not found");

        order.get().setPaymentTransactionId(response.get("vnp_TransactionNo"));


        OrderStatus orderStatus;

        PaymentMethod paymentMethod;

        String addressId;

        TransactionType transactionType;

        String customerNote;


    }

    @Override
    public void handlePaymentReturnFail() {

    }

    @Override
    public String handlePaymentCheckingStatus(HttpServletRequest request, String orderId) throws IOException {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new InvalidDataException("Order Not Found"));
        String transactionStatus = "";
        String paymentMethod = order.getPaymentMethod().getId();
        if (paymentMethod.equals(PaymentMethod.VNPAY.name())) {

            Map<String, String> response =
                    vnPayService.queryTransactionStatus(order.getPaymentTransactionId(), order.getCreatedAt(), VNPayConfig.getIpAddress(request));

            if (response.get("vnp_ResponseCode").equals("00"))
                transactionStatus = VNPayUtil.getTransactionStatusDescription(response.get("vnp_TransactionStatus"));

            else transactionStatus = "Send request fail: " + response.get("vnp_Message");

        } else if (paymentMethod.equals(PaymentMethod.COD.name())) {
        } //bla bla

        return transactionStatus;
    }

    @Override
    public String handlePaymentRefund(HttpServletRequest request, String orderId) throws IOException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new InvalidDataException("Order Not Found"));
        String transactionStatus = "";
        String paymentMethod = order.getPaymentMethod().getId();
        if (paymentMethod.equals(PaymentMethod.VNPAY.name())) {

            Map<String, String> response =
                    vnPayService.queryTransactionStatus(order.getPaymentTransactionId(), order.getCreatedAt(), VNPayConfig.getIpAddress(request));

            if (response.get("vnp_ResponseCode").equals("00"))
                transactionStatus = VNPayUtil.getTransactionStatusDescription(response.get("vnp_TransactionStatus"));

            else transactionStatus = "Send request fail: " + response.get("vnp_Message");

        } else if (paymentMethod.equals(PaymentMethod.COD.name())) {
        } //bla bla

        return transactionStatus;
    }


        return "";
    }
}
