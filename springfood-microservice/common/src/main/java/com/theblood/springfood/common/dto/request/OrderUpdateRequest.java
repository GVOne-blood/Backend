package com.theblood.springfood.common.dto.request;

import com.theblood.springfood.common.enums.OrderStatus;
import com.theblood.springfood.common.enums.PaymentMethod;

import java.util.UUID;

public class OrderUpdateRequest {

    UUID referenceId;
    String successTransactionId;
    PaymentMethod paymentMethod;
    OrderStatus orderStatusUpdate;
    String transactionStatus;

}
