package com.spring_food.springfood.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AttributeOverride(name = "id", column = @Column(name = "cart_id"))
public class Cart extends AbstractEntity {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    User user;

    @OneToMany(mappedBy = "cart")
    List<CartItem> cartItems;


}
