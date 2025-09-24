package com.spring_food.springfood.dto.request;

import com.spring_food.springfood.common.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Valid
public class VNPayPaymentRequest {

    String orderId;
    PaymentMethod paymentMethod;
    Long amount;
    @NotBlank(message = "transaction id must be not blank")
    String generatedTransactionId;
    String orderInfo;


}
