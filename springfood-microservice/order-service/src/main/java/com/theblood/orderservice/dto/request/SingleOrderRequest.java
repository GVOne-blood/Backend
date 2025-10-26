package com.theblood.orderservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SingleOrderRequest {
    String orderId;
    String addressId;
    String customerNote;
}
