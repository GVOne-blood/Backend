package com.theblood.orderservice.grpc.client_role;


import com.theblood.springfood.common.enums.OrderStatus;
import com.theblood.springfood.common.enums.TransactionStatus;
import com.theblood.springfood.common.grpc.PaymentRequest;
import com.theblood.springfood.common.grpc.PaymentResponse;
import com.theblood.springfood.common.grpc.PaymentServiceGrpc;
import com.theblood.orderservice.repository.OrderRepository;
import com.theblood.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderTranfer {

    OrderService orderService;
    OrderRepository orderRepository;

    @GrpcClient("payment-service")
    private PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceStub;

    /**
     *
     * @param userId
     * @param amount
     * @param orderStatus
     * @param paymentMethod
     * @return a paymentTransaction id to pay orders
     */
    public String creationPaymentTransactionRequest(UUID userId, Long amount, OrderStatus orderStatus, String paymentMethod) {
        PaymentRequest req = PaymentRequest.newBuilder()
                .setAmount(amount.toString())
                .setPaymentMethod(paymentMethod)
                .setTransactionStatus(TransactionStatus.PENDING.name())
                .setUserId(userId.toString())
                .build();

        PaymentResponse ans = paymentServiceStub
                .withDeadlineAfter(5, TimeUnit.SECONDS)
                .paymentCreation(req);
        if (!ans.getSuccess()) {
            log.error("create paymentTransaction failed");
            return null;
        }
        return ans.getReferenceId();

    }


}

