package com.spring_food.springfood.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "payment")
@AttributeOverride(name = "id", column = @Column(name = "payment_name", nullable = true))
public class Payment extends AbstractEntity {

    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "paymentMethod")
    private List<Order> orders = new ArrayList<>();

    @
}
