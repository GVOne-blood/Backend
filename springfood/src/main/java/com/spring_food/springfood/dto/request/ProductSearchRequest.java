package com.spring_food.springfood.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSearchRequest {

    String name;
    String description;
    String priceRange;
    String category;
    String address;
    String shopName;


}
