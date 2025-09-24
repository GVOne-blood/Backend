package com.spring_food.springfood.dto.request;

import com.spring_food.springfood.common.enums.OrderStatus;
import com.spring_food.springfood.common.enums.PaymentMethod;
import com.spring_food.springfood.common.enums.TransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderUpdateRequest {

    String orderId;
    
    OrderStatus orderStatus;

    PaymentMethod paymentMethod;

    String addressId;

    TransactionType transactionType;

    String customerNote;


}
