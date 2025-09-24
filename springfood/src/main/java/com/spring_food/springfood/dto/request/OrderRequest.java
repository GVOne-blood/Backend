package com.spring_food.springfood.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {

    @NotEmpty(message = "Shop order must be not empty")
    @Valid
    List<ShopOrderRequest> shopOrderItems;

    @NotNull(message = "Payment info must be not blank")
    @Valid
    PaymentInfoRequest paymentInfo;

    @NotNull(message = "Shipping address id must be not null")
    String shippingAddressId;

    String globalVoucher;


}
