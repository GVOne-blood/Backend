package com.theblood.orderservice.dto.request;


import com.theblood.common.enums.PaymentMethod;
import com.theblood.orderservice.common.enums.OrderStatus;
import com.theblood.orderservice.common.enums.TransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrdersUpdateRequest {


    boolean wantToRefund;

    boolean wantToDestroy;

    List<SingleOrderRequest> order;

    OrderStatus orderStatus;

    PaymentMethod paymentMethod;

    TransactionType transactionType;


}
