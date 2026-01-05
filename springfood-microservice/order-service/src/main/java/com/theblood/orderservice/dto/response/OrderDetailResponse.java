package com.theblood.orderservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.theblood.common.dto.response.ProductDetail;
import com.theblood.common.enums.OrderStatus;
import com.theblood.orderservice.common.enums.TransactionStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDetailResponse implements Serializable {

    UUID orderId;
    UUID userId;
    UUID shopId;
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
