package com.spring_food.springfood.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartDetailResponse {

    String productId;
    String productName;
    int quantity;
    BigDecimal price;
    String image;

}
