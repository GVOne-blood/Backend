package com.theblood.orderservice.common.util;


import com.theblood.common.enums.OrderStatus;
import com.theblood.common.exception.custom.InvalidDataException;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class OrderStatusValidationUtil {

    public static boolean isValidStatusTransition(OrderStatus nextStatus) {
        return getValidStatusTransition(nextStatus).contains(nextStatus);
    }

    public static List<OrderStatus> getValidStatusTransition(OrderStatus currentStatus) {
        List<OrderStatus> validStatus = new ArrayList<>();
        switch (currentStatus) {
            case PENDING -> {
                validStatus.add(OrderStatus.PENDING_PAYMENT);
                validStatus.add(OrderStatus.CONFIRMED);
            }
            case PENDING_PAYMENT -> {
                validStatus.add(OrderStatus.PENDING);
                validStatus.add(OrderStatus.CONFIRMED);
            }
            case CONFIRMED -> {
                validStatus.add(OrderStatus.PENDING_PAYMENT);
                validStatus.add(OrderStatus.PROCESSING);
            }
            case PROCESSING -> {
                validStatus.add(OrderStatus.READY_FOR_PICKUP);
            }
            case READY_FOR_PICKUP -> {
                validStatus.add(OrderStatus.SHIPPING);
            }
            case SHIPPING -> {
                validStatus.add(OrderStatus.COMPLETED);
                validStatus.add(OrderStatus.FAILED);
            }
            case COMPLETED -> {
                validStatus.add(OrderStatus.ORDER_RETURN);
            }
            case ORDER_RETURN -> {
                validStatus.add(OrderStatus.SHIPPING);
            }
            case FAILED -> {
                validStatus.add(OrderStatus.DELETED);
            }
            default -> {
                throw new InvalidDataException("Order Status invalid");
            }
        }
        return validStatus;
    }
}
