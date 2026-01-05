package com.theblood.common.dto.request;

import com.theblood.common.enums.OrderStatus;
import com.theblood.common.enums.PaymentMethod;

import java.util.UUID;

public class OrderUpdateRequest {

    UUID referenceId;
    String successTransactionId;
    PaymentMethod paymentMethod;
    OrderStatus orderStatusUpdate;
    String transactionStatus;

}
