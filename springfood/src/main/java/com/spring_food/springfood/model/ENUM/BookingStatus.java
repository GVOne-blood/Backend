package com.spring_food.springfood.model.ENUM;

public enum BookingStatus {
    PENDING,      // Đơn hàng đang chờ xử lý
    PROCESSING,   // Đơn hàng đang được xử lý
    SHIPPED,      // Đơn hàng đã được giao đi
    COMPLETED,    // Đơn hàng đã hoàn thành
    CANCELLED     // Đơn hàng đã bị hủy
}
