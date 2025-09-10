package com.spring_food.springfood.model;

import com.spring_food.springfood.model.ENUM.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "orders")
@AttributeOverride(name = "id", column = @Column(name = "booking_id"))
public class Order extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "booking_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(name = "final_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalPrice;

    @ManyToOne
    @JoinColumn(name = "payment_method_name")
    private Payment paymentMethod;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> bookingItems = new ArrayList<>();

    // Gán giá trị mặc định trước khi lưu vào database
    @PrePersist
    public void prePersist() {
        if (orderStatus == null) {
            orderStatus = OrderStatus.PENDING;
        }
        if (finalPrice == null) {
            finalPrice = BigDecimal.ZERO;
        }
    }
}
