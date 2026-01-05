package com.theblood.orderservice.dto.request;

import com.theblood.common.dto.request.ShopOrderRequest;
import com.theblood.common.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

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
    PaymentMethod paymentMethod;

    @NotNull(message = "Shipping address id must be not null")
    UUID shippingAddressId;

    String globalVoucher;

}