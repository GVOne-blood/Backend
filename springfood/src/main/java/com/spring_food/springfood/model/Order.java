package com.spring_food.springfood.model;

import com.spring_food.springfood.common.enums.OrderStatus;
import com.spring_food.springfood.common.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "orders")
@AttributeOverride(name = "id", column = @Column(name = "booking_id"))
public class Order extends AbstractEntity {

    @Column(name = "booking_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(name = "final_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalPrice;

    @ManyToOne
    @JoinColumn(name = "payment_method_name")
    private Payment paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

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

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus paymentStatus;

    @Column(name = "payment_transaction_id")
    private String paymentTransactionId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> bookingItems = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Gán giá trị mặc định trước khi lưu vào database
    @PrePersist
    public void prePersist() {
        if (orderStatus == null) {
            orderStatus = OrderStatus.PENDING;
        }
        if (finalPrice == null) {
            finalPrice = BigDecimal.ZERO;
        }
        if (subtotalAmount == null) {
            subtotalAmount = BigDecimal.ZERO;
        }
        if (shippingFee == null) {
            shippingFee = BigDecimal.ZERO;
        }
        if (discount == null) {
            discount = BigDecimal.ZERO;
        }
        if (paymentStatus == null) {
            paymentStatus = TransactionStatus.PENDING;
        }
    }
}
