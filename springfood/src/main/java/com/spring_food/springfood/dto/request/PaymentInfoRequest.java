package com.spring_food.springfood.dto.request;

import com.spring_food.springfood.common.enums.PaymentMethod;
import com.spring_food.springfood.common.enums.TransactionStatus;
import com.spring_food.springfood.common.util.EnumPattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentInfoRequest {

    @NotBlank(message = "User id must be not blank ")
    String userId;

    @Min(0)
    Long amount;

    @EnumPattern(name = "status", regexp = "PENDING|PAID|FAILED|CANCELLED")
    TransactionStatus status;

    @EnumPattern(name = "paymentMethod", regexp = "VNP|COD")
    PaymentMethod paymentMethod;


}
