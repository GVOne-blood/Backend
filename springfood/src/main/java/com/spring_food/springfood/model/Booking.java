package com.spring_food.springfood.model;

import com.spring_food.springfood.model.ENUM.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "booking")
@AttributeOverride(name = "id", column = @Column(name = "booking_id"))
public class Booking extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "booking_status")
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @Column(name = "final_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @ManyToOne
    @JoinColumn(name = "payment_method_name")
    private Payment paymentMethod;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingItem> bookingItems = new ArrayList<>();

    // Gán giá trị mặc định trước khi lưu vào database
    @PrePersist
    public void prePersist() {
        if (bookingStatus == null) {
            bookingStatus = BookingStatus.PENDING;
        }
        if (totalPrice == null) {
            totalPrice = BigDecimal.ZERO;
        }
    }
}
