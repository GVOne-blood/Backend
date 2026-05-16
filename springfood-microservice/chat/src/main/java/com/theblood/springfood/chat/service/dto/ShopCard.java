package com.theblood.springfood.chat.service.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopCard {
    String id;
    String name;
    String logo;
    String introduction;
    Integer totalProducts;
    Integer totalSold;
}
