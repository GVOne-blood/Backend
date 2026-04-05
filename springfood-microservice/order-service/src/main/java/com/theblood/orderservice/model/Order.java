package com.theblood.orderservice.model;

import com.theblood.springfood.common.enums.OrderStatus;
import com.theblood.springfood.common.enums.PaymentMethod;
import com.theblood.springfood.common.model.AbstractEntity;
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

    // id tham chiếu đến payment transaction.
    // Một list các đơn hàng có thể được thanh toán nhiều lần (tất nhiên thanh toán thành công thì sẽ thôi),
    // vậy nên sẽ có nhiều bản ghi payment transaction được tạo ra, muốn tìm đến các order được thanh toán của những lần đó ta có referenId
    @Column(name = "payment_transaction_id")
    UUID referenceId;

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

    @Column(name = "paid_at")
    LocalDateTime paidAt;
}
