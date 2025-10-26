package com.theblood.orderservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.theblood.orderservice.common.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailResponse implements Serializable {

    String orderId;
    String userId;
    String shopId;
    LocalDateTime orderDate;
    BigDecimal subtotalAmount;
    BigDecimal discount;
    BigDecimal finalPrice;
    String paymentMethod;
    TransactionStatus paymentStatus;
    OrderStatus orderStatus;
    List<ProductDetail> items;
    String shippingAddress;
    BigDecimal shippingFee;
    LocalDateTime deliveredAt;

}
