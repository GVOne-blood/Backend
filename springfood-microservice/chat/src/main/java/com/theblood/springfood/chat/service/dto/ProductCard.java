package com.theblood.springfood.chat.service.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCard {
    String id;
    String name;
    BigDecimal price;
    String image;
    String description;
    Double averageRating;
    String shopName;
}
