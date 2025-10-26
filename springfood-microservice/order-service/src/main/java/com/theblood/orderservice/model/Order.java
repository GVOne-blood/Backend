package com.theblood.orderservice.model;

import com.theblood.common.enums.PaymentMethod;
import com.theblood.common.model.AbstractEntity;
import com.theblood.orderservice.common.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AttributeOverride(name = "id", column = @Column(name = "order_id"))
public class Order extends AbstractEntity {

    @Column(name = "user_id")
    UUID userId;

    @Column(name = "shop_id")
    UUID shopId;

    @Column(name = "shipper_id")
    UUID shipperId;

    @Column(name = "payment_transaction_id")
    UUID paymentTransactionId;

    @Column(name = "order_status")
    @Enumerated(EnumType.STRING)
    OrderStatus orderStatus;

    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "subtotal_amount")
    private BigDecimal subtotalAmount;

    @Column(name = "shipping_fee")
    private BigDecimal shippingFee;

    @Column(name = "discount_amount")
    private BigDecimal discount;

    @Column(name = "final_price")
    BigDecimal finalPrice;

    @Column(name = "shipping_address_street")
    String shippingAddressStreet;

    @Column(name = "shipping_address_ward")
    String shippingAddressWard;

    @Column(name = "shipping_address_city")
    String shippingAddressCity;

    @Column(name = "shipping_address_details")
    String shippingAddressDetails;

    @Column(name = "payment_method_name")
    @Enumerated(EnumType.STRING)
    PaymentMethod paymentMethod;

}
