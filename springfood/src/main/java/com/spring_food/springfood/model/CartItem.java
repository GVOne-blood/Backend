package com.spring_food.springfood.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_item")
@AttributeOverride(name = "id", column = @Column(name = "cart_item_id"))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItem extends AbstractEntity {

    @Column(name = "quantity")
    int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    Cart cart;

}
