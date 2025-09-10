package com.spring_food.springfood.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetail {
    String id;
    String name;
    String description;
    BigDecimal price;
    String images;
    Integer quantity;
    LocalDate msg;
    LocalDate exp;

}
