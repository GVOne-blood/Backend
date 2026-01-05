package com.theblood.common.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopOrderRequest {

    @NotBlank(message = "Shop id is require")
    String shopId;

    @NotBlank(message = "shop shipping method must be not blank")
    String shippingMethod;

    BigDecimal shippingFee;

    String shopVoucher;

    String note;

    @Valid
    List<ItemRequest> items;
}
