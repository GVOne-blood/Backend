package com.theblood.paymentservice.service.impl;


import com.theblood.common.exception.custom.InvalidDataException;
import com.theblood.common.grpc.OrderUpdateRequest;
import com.theblood.paymentservice.common.enums.TransactionStatus;
import com.theblood.paymentservice.common.enums.TransactionType;
import com.theblood.paymentservice.dto.request.PaymentInfoRequest;
import com.theblood.paymentservice.grpc.client_role.OrderUpdateService;
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
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    //    OrderService orderService;
//    UserRepository userRepository;
    PaymentRepository paymentRepository;
    PaymentTransactionsRepository paymentTransactionsRepository;
    OrderUpdateService orderUpdateService;
//    private final OrderMapper orderMapper;


    /**
     * chỉ được gọi từ order service khi order mới được tạo. Order service call gRPC qua để tạo 1 object PaymentTransaction mới
     *
     * @param paymentInfoRequest - request này nên được truy xuất và lấy từ trong order đã save của backend. Có thể lấy từ client nhưng bỏa mật không cao
     * @return
     */
    @Override
    public PaymentTransactions createPaymentTransaction(PaymentInfoRequest paymentInfoRequest) {

        //User user = userRepository.findById(paymentInfoRequest.getUserId()).orElseThrow(() -> new InvalidDataException("User not found"));

        PaymentTransactions paymentTransactions = new PaymentTransactions();
        paymentTransactions.setAmount(new BigDecimal(paymentInfoRequest.getAmount()));
        paymentTransactions.setUserId(paymentInfoRequest.getUserId().toString());
        if (paymentInfoRequest.getTransactionType() == null)
            paymentTransactions.setReferenceType(TransactionType.PAYMENT.toString());
        paymentTransactions.setReferenceType(paymentInfoRequest.getTransactionType().toString());
        paymentTransactions.setPaymentMethodName(paymentInfoRequest.getPaymentMethod().name());
        paymentTransactions.setStatus(TransactionStatus.PENDING);
        paymentTransactionsRepository.save(paymentTransactions);
        return paymentTransactions;
    }

    @Override
    public void updateCodPaymentTransaction() {

    }

    //
    @Override
    public void updatePaymentTransaction(UUID id, String transactionNo, TransactionStatus transactionStatus, LocalDateTime transferSuccessAt) {

        PaymentTransactions paymentTransactions = paymentTransactionsRepository.findById(id).orElseThrow(() -> new InvalidDataException("Payment Transaction not found"));

        paymentTransactions.setSuccessAt(transferSuccessAt);
        paymentTransactions.setProviderTransactionRef(transactionNo);
        paymentTransactions.setStatus(transactionStatus);

    }

    /**
     *
     * @param response
     */
    @Override
    public void handlePaymentReturnSuccess(Map<String, String> response) {

        String paymentTransactionsId = response.get("vnp_TxnRef");

        String newTransactionNo = response.get("vnp_TransactionNo");
        // ĐỊNH NGHĨA ĐỊNH DẠNG CỦA VNPAY
        DateTimeFormatter vnpayFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String payDateString = response.get("vnp_PayDate");
        LocalDateTime transferSuccessAt = (LocalDateTime.parse(payDateString, vnpayFormatter));

        PaymentTransactions paymentTransactions = paymentTransactionsRepository.findById(UUID.fromString(paymentTransactionsId))
                .orElseThrow(() -> new InvalidDataException("Payment Transaction not found"));

        // cập nhật chính payment transaction
        updatePaymentTransaction(UUID.fromString(paymentTransactionsId), newTransactionNo, TransactionStatus.PAID, transferSuccessAt);
        // cập nhật trạng thái của order khi thanh toán thành công qua gRPC

        OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.newBuilder()
                .setReferenceId(paymentTransactions.getReferenceId().toString())
                .setSuccessTransactionId(newTransactionNo)
                .build();
        orderUpdateService.updateOrder(orderUpdateRequest);

//        orderService.updatePaymentPendingOrders(updateRequest);
    }

    @Override
    public void handlePaymentReturnFail(Map<String, String> response) {

        String paymentTransactionsId = response.get("vnp_TxnRef");

        PaymentTransactions paymentTransactions = paymentTransactionsRepository.findById(UUID.fromString(paymentTransactionsId)).orElseThrow(() -> new InvalidDataException("Payment Transaction not found"));

//        List<Order> orders = paymentTransactions.getOrders();
//
//        if (orders.isEmpty()) throw new InvalidDataException("Orders not found");
//
//        for (Order order : orders) {
//            order.setTransferDate(LocalDateTime.parse(response.get("vnp_PayDate")));
//        }

        // ĐỊNH NGHĨA ĐỊNH DẠNG CỦA VNPAY
        DateTimeFormatter vnpayFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String payDateString = response.get("vnp_PayDate");
        LocalDateTime transferFailAt = (LocalDateTime.parse(payDateString, vnpayFormatter));
        String newTransactionNo = response.get("vnp_TransactionNo");
//        orderService.updatePaymentPendingOrders(updateRequest);
        updatePaymentTransaction(UUID.fromString(paymentTransactionsId), newTransactionNo, TransactionStatus.FAILED, transferFailAt);
    }
}

