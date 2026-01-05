package com.theblood.paymentservice.mapper;

import com.theblood.common.grpc.PaymentRequest;
import com.theblood.paymentservice.dto.request.PaymentInfoRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentInfoRequest toPaymentInfoRequest(PaymentRequest paymentRequest);
}
