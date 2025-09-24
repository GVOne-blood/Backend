package com.spring_food.springfood.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "order_item")
@AttributeOverride(name = "id", column = @Column(name = "bill_id"))
public class OrderItem extends AbstractEntity {

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "price_at_booking", nullable = false, precision = 15, scale = 2)
    private BigDecimal priceAtBooking;
}
