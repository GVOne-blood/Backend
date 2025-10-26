package com.theblood.orderservice.model;

import com.theblood.common.model.AbstractEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AttributeOverride(name = "id", column = @Column(name = "order_item_id"))
public class OrderItem extends AbstractEntity {

    @Column(name = "order_id")
    UUID orderId;

    @Column(name = "product_id")
    UUID productId;

    @Column(name = "product_name")
    String productName;

    @Column(name = "quantity")
    int quantity;

    @Column(name = "price_at_booking")
    BigDecimal priceAtBooking;


}
