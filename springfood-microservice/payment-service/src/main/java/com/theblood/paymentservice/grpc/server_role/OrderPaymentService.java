package com.theblood.paymentservice.grpc.server_role;

import com.theblood.common.grpc.PaymentRequest;
import com.theblood.common.grpc.PaymentResponse;
import com.theblood.common.grpc.PaymentServiceGrpc;
import com.theblood.paymentservice.dto.request.PaymentInfoRequest;
import com.theblood.paymentservice.mapper.PaymentMapper;
import com.theblood.paymentservice.model.PaymentTransactions;
import com.theblood.paymentservice.service.PaymentService;
import io.grpc.stub.StreamObserver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderPaymentService extends PaymentServiceGrpc.PaymentServiceImplBase {

    PaymentService paymentService;
    PaymentMapper paymentMapper;

    @Override
    public void paymentCreation(PaymentRequest paymentInfoRequest, StreamObserver<PaymentResponse> responseObserver) {

        boolean success = true;
        String message = "create payment transaction successfully";
        PaymentInfoRequest req = paymentMapper.toPaymentInfoRequest(paymentInfoRequest);
        PaymentTransactions paymentTransactions = paymentService.createPaymentTransaction(req);

        if (paymentTransactions == null) {
            success = false;
            message = "Payment Transaction Failed";
            // throw new InvalidDataException("Payment Transactions cannot be created");
        }

        PaymentResponse ans = PaymentResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .setReferenceId(paymentTransactions.getReferenceId().toString())
                .build();
        responseObserver.onNext(ans);
        responseObserver.onCompleted();
    }
}
