package com.theblood.shopservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopResponse {
    UUID shopId;
    String shopName;
    String logo;
    String introduction;
    Integer totalProducts;
    Integer totalSold;
}
