package com.spring_food.springfood.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "booking_item")
@AttributeOverride(name = "id", column = @Column(name = "bill_id"))
public class BookingItem extends AbstractEntity{

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price_at_booking", nullable = false, precision = 15, scale = 2)
    private BigDecimal priceAtBooking;
}
