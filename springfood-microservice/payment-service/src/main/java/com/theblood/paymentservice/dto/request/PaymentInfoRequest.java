package com.theblood.paymentservice.dto.request;


import com.theblood.springfood.common.enums.PaymentMethod;
import com.theblood.springfood.common.util.EnumPattern;
import com.theblood.paymentservice.common.enums.TransactionStatus;
import com.theblood.paymentservice.common.enums.TransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentInfoRequest {

    @NotBlank(message = "User id must be not blank ")
    UUID userId;

    @Min(0)
    Long amount;

    @EnumPattern(name = "status", regexp = "PENDING|PAID|FAILED|CANCELLED")
    TransactionStatus status;

    @EnumPattern(name = "transactionType", regexp = "PAYMENT|REFUND")
    TransactionType transactionType;

    @EnumPattern(name = "paymentMethod", regexp = "VNP|COD")
    PaymentMethod paymentMethod;


}
