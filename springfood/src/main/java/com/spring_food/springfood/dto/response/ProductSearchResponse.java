package com.spring_food.springfood.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class ProductSearchResponse {

    String id;
    List<String> categories;
    String name;
    String description;
    BigDecimal price;
    String images;
    Integer quantity;
    LocalDateTime updatedAt;

}
