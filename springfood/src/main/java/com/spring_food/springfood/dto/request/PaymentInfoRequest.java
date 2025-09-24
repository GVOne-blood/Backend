package com.spring_food.springfood.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentInfoRequest {

    @NotBlank(message = "Payment method must be not blank")
    String paymentMethod;

}
