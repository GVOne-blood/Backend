package com.spring_food.springfood.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "address")
@AttributeOverride(name = "id", column = @Column(name = "address_id"))
public class Address extends AbstractEntity {

    private String ward;

    private String street;

    private String city;

    @Column(name = "details")
    private String address;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "address")
    private Order order;
}
