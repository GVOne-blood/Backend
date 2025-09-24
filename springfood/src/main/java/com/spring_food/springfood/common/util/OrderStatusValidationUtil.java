package com.spring_food.springfood.common.util;

import com.spring_food.springfood.common.enums.OrderStatus;

import java.util.ArrayList;
import java.util.List;

public class OrderStatusValidationUtil {


    public static boolean isValidStatusTransition(OrderStatus currentStatus) {
        return getValidStatusTransition(currentStatus).contains(currentStatus);
    }

    public static List<OrderStatus> getValidStatusTransition(OrderStatus currentStatus) {
        List<OrderStatus> validStatus = new ArrayList<>();
        switch (currentStatus) {
            case PENDING -> {
                validStatus.add(OrderStatus.PENDING_PAYMENT);
                validStatus.add(OrderStatus.CONFIRMED);
            }
            case PENDING_PAYMENT -> {
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
                validStatus.add(OrderStatus.CANCELLED);
            }
        }
        return validStatus;
    }
}
