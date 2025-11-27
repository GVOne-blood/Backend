package com.theblood.common.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetail {
    UUID id;
    String name;
    String description;
    BigDecimal price;
    String images;
    Integer quantity;
    LocalDate msg;
    LocalDate exp;
    Double averageRating;
    Long totalFeedbacks;


}
