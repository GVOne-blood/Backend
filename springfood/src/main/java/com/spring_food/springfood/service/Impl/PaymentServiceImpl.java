package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.common.enums.PaymentMethod;
import com.spring_food.springfood.common.enums.TransactionStatus;
import com.spring_food.springfood.common.util.VNPayUtil;
import com.spring_food.springfood.config.VNPayConfig;
import com.spring_food.springfood.dto.request.OrdersUpdateRequest;
import com.spring_food.springfood.dto.request.PaymentInfoRequest;
import com.spring_food.springfood.dto.request.VNPayPaymentRequest;
import com.spring_food.springfood.exception.custom.InvalidDataException;
import com.spring_food.springfood.model.Order;
import com.spring_food.springfood.model.PaymentTransactions;
import com.spring_food.springfood.model.User;
import com.spring_food.springfood.repository.OrderRepository;
import com.spring_food.springfood.repository.PaymentTransactionsRepository;
import com.spring_food.springfood.repository.UserRepository;
import com.spring_food.springfood.service.PaymentService;
import com.spring_food.springfood.service.UserService;
import com.spring_food.springfood.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.NotActiveException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    OrderRepository orderRepository;
    VNPayService vnPayService;
    UserService userService;
    UserRepository userRepository;
    PaymentTransactionsRepository paymentTransactionsRepository;


    public void createPaymentTransaction(PaymentInfoRequest paymentInfoRequest, List<Order> orders) {

        User user = userRepository.findById(paymentInfoRequest.getUserId()).orElseThrow(() -> new InvalidDataException("User not found"));

        PaymentTransactions paymentTransactions = new PaymentTransactions();
        paymentTransactions.setAmount(paymentInfoRequest.getAmount());
        paymentTransactions.setUser(user);
        // 1 or n order
        paymentTransactions.setOrders(orders);
        // default
        paymentTransactions.setStatus(TransactionStatus.PENDING);
        paymentTransactionsRepository.save(paymentTransactions);
    }

    public void updatePaymentTransaction(String id, String transactionNo, PaymentMethod paymentMethod, TransactionStatus transactionStatus) {
        PaymentTransactions paymentTransactions = paymentTransactionsRepository.findById(id).orElseThrow(() -> new InvalidDataException("Payment Transaction not found"));

        paymentTransactions.setTransactionNo(transactionNo);
        paymentTransactions.setStatus(transactionStatus);

    }

    @Override
    public void handlePaymentReturnSuccess(String orderId, Map<String, String> response) {
        OrdersUpdateRequest updateRequest = new OrdersUpdateRequest();

        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) throw new InvalidDataException("Order not found");

        String newTransactionNo = response.get("vnp_TransactionNo");
        order.get().setTransferDate(LocalDateTime.parse(response.get("vnp_PayDate")));
        updatePaymentTransaction(order.get().getPaymentTransactions().getId(), newTransactionNo, PaymentMethod.VNPAY, TransactionStatus.PAID);

    }

    @Override
    public void handlePaymentReturnFail(String orderId, Map<String, String> response) {
        OrdersUpdateRequest updateRequest = new OrdersUpdateRequest();

        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) throw new InvalidDataException("Order not found");

        String newTransactionNo = response.get("vnp_TransactionNo");
        order.get().setTransferDate(LocalDateTime.parse(response.get("vnp_PayDate")));
        updatePaymentTransaction(order.get().getPaymentTransactions().getId(), newTransactionNo, PaymentMethod.VNPAY, TransactionStatus.FAILED);

    }

    @Override
    public String handlePaymentCheckingStatus(HttpServletRequest request, String userId, String orderId) throws IOException {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new InvalidDataException("Order Not Found"));
        String transactionStatus = "";
        String paymentMethod = order.getPaymentMethod().getId();
        if (paymentMethod.equals(PaymentMethod.VNPAY.name())) {

            Map<String, String> response =
                    vnPayService.queryTransactionStatus(orderId, order.getCreatedAt(), VNPayConfig.getIpAddress(request));

            if (response.get("vnp_ResponseCode").equals("00")) {
                transactionStatus = VNPayUtil.getTransactionStatusDescription(response.get("vnp_TransactionStatus"));

            } else transactionStatus = "Send request fail: " + response.get("vnp_Message");

        } else if (paymentMethod.equals(PaymentMethod.COD.name())) {
        } //bla bla

        return transactionStatus;
    }

    @Override
    public String handlePaymentRefund(HttpServletRequest request, String userId, String orderId) throws IOException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new InvalidDataException("Order Not Found"));
        String transactionStatus = "";
        String paymentMethod = order.getPaymentMethod().getId();
        if (!paymentMethod.equals(PaymentMethod.VNPAY.name()) || !order.getPaymentStatus().equals(TransactionStatus.PAID))
            throw new NotActiveException("User haven't paid yet");

        VNPayPaymentRequest vnpayRequest = new VNPayPaymentRequest();
        vnpayRequest.setAmount(order.getFinalPrice().longValue());
        vnpayRequest.setGeneratedTransactionId(order.getPaymentTransactions().getTransactionNo());
        // TxnRef = paymentTransaction.Id
        vnpayRequest.setTxnRef(order.getPaymentTransactions().getId());
        vnpayRequest.setTransferDate(order.getTransferDate());
        vnpayRequest.setPaymentMethod(PaymentMethod.VNPAY);
        Map<String, String> response =
                vnPayService.queryRefund(vnpayRequest, VNPayConfig.getIpAddress(request));

        if (response.get("vnp_ResponseCode").equals("00"))
            transactionStatus = VNPayUtil.getRefundResponseDescription(response.get("vnp_TransactionStatus"));

        else transactionStatus = "Send request fail: " + response.get("vnp_Message");

        return transactionStatus;
    }
}

