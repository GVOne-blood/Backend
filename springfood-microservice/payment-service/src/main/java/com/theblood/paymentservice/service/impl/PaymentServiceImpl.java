package com.theblood.paymentservice.service.impl;


import com.theblood.common.exception.custom.InvalidDataException;
import com.theblood.paymentservice.common.enums.TransactionStatus;
import com.theblood.paymentservice.dto.request.PaymentInfoRequest;
import com.theblood.paymentservice.model.PaymentTransactions;
import com.theblood.paymentservice.repository.PaymentRepository;
import com.theblood.paymentservice.repository.PaymentTransactionsRepository;
import com.theblood.paymentservice.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    OrderService orderService;
    UserRepository userRepository;
    PaymentRepository paymentRepository;
    PaymentTransactionsRepository paymentTransactionsRepository;
    private final OrderMapper orderMapper;


    @Override
    public PaymentTransactions createPaymentTransaction(PaymentInfoRequest paymentInfoRequest, List<Order> orders) {

        User user = userRepository.findById(paymentInfoRequest.getUserId()).orElseThrow(() -> new InvalidDataException("User not found"));

        PaymentTransactions paymentTransactions = new PaymentTransactions();
        paymentTransactions.setAmount(new BigDecimal(paymentInfoRequest.getAmount()));
        paymentTransactions.setUser(user);
        // 1 or n order
        paymentTransactions.setOrders(orders);

        for (Order order : orders) {
            order.setPaymentTransactions(paymentTransactions);
        }
        Payment payment = paymentRepository.findById(paymentInfoRequest.getPaymentMethod().name()).orElseThrow(() -> new InvalidDataException("Payment method not found"));
        paymentTransactions.setPayment(payment);
        // default
        paymentTransactions.setStatus(TransactionStatus.PENDING);
        paymentTransactionsRepository.save(paymentTransactions);
        return paymentTransactions;
    }

    @Override
    public void updatePaymentTransaction(String id, String transactionNo, TransactionStatus transactionStatus) {


        PaymentTransactions paymentTransactions = paymentTransactionsRepository.findById(id).orElseThrow(() -> new InvalidDataException("Payment Transaction not found"));

        List<SingleOrderRequest> orders = orderMapper.toSingleOrderRequest(paymentTransactions.getOrders());
        OrdersUpdateRequest updateRequest = new OrdersUpdateRequest();
        updateRequest.setOrder(orders);

        switch (transactionStatus) {
            case PAID -> {

                orderService.updatePaymentPendingOrders(updateRequest);
            }
            case REFUNDED -> {

            }
        }
        paymentTransactions.setTransactionNo(transactionNo);
        paymentTransactions.setStatus(transactionStatus);

    }

    @Override
    public void handlePaymentReturnSuccess(Map<String, String> response) {
        OrdersUpdateRequest updateRequest = new OrdersUpdateRequest();

        String paymentTransactionsId = response.get("vnp_TxnRef");

        PaymentTransactions paymentTransactions = paymentTransactionsRepository.findById(paymentTransactionsId).orElseThrow(() -> new InvalidDataException("Payment Transaction not found"));
        List<Order> orders = paymentTransactions.getOrders();

        if (orders.isEmpty()) throw new InvalidDataException("Orders not found");

        String newTransactionNo = response.get("vnp_TransactionNo");
        // ĐỊNH NGHĨA ĐỊNH DẠNG CỦA VNPAY
        DateTimeFormatter vnpayFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String payDateString = response.get("vnp_PayDate");

        LocalDateTime transferDateTime = LocalDateTime.parse(payDateString, vnpayFormatter);

        for (Order order : orders) {
            order.setTransferDate(transferDateTime);
        }
//        orderService.updatePaymentPendingOrders(updateRequest);
        updatePaymentTransaction(paymentTransactionsId, newTransactionNo, TransactionStatus.PAID);
    }

    @Override
    public void handlePaymentReturnFail(Map<String, String> response) {
        OrdersUpdateRequest updateRequest = new OrdersUpdateRequest();

        String paymentTransactionsId = response.get("vnp_TxnRef");

        PaymentTransactions paymentTransactions = paymentTransactionsRepository.findById(paymentTransactionsId).orElseThrow(() -> new InvalidDataException("Payment Transaction not found"));

        List<Order> orders = paymentTransactions.getOrders();

        if (orders.isEmpty()) throw new InvalidDataException("Orders not found");

        String newTransactionNo = response.get("vnp_TransactionNo");
        for (Order order : orders) {
            order.setTransferDate(LocalDateTime.parse(response.get("vnp_PayDate")));
        }
//        orderService.updatePaymentPendingOrders(updateRequest);
        updatePaymentTransaction(paymentTransactionsId, newTransactionNo, TransactionStatus.PAID);
    }
}

