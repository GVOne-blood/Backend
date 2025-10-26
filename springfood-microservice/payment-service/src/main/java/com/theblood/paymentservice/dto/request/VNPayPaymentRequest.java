package com.theblood.paymentservice.dto.request;

import com.theblood.common.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Valid
public class VNPayPaymentRequest {

    // all
    String userId; // user transfer /
    String userEmail; // support vnpay send mail to notify
    PaymentMethod paymentMethod;
    Long amount;
    String orderInfo;

    // refund
    @NotNull(message = "transferDate couldn't be null")
    LocalDateTime transferDate;
    String transactionNo;
    String txnRef;

}
