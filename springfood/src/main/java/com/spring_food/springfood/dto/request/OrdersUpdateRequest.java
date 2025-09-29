package com.spring_food.springfood.dto.request;

import com.spring_food.springfood.common.enums.OrderStatus;
import com.spring_food.springfood.common.enums.PaymentMethod;
import com.spring_food.springfood.common.enums.TransactionType;
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
