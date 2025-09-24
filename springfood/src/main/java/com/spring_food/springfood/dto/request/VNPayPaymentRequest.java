package com.spring_food.springfood.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Valid
public class VNPayPaymentRequest {

    Long amount;
    @NotBlank(message = "transaction id must be not blank")
    String generatedTransactionId;
    String orderInfo;
    

}
