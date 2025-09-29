package com.spring_food.springfood.dto.request;

import com.spring_food.springfood.common.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    String userId; // user transfer / txmRef code
    String userEmail; // support vnpay send mail to notify
    PaymentMethod paymentMethod;
    Long amount;
    @NotBlank(message = "transaction id must be not blank")
    String generatedTransactionId;
    String orderInfo;

    // refund
    @NotNull(message = "transferDate couldn't be null")
    LocalDateTime transferDate;
    String transactionNo;
    String txnRef;

}
