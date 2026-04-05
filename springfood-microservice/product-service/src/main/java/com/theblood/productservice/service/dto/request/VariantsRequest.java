package com.theblood.productservice.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariantsRequest {

    String variantName;
    String variantValue;
    BigDecimal price;
    Integer stock;
    String imageUrl;
    String[] attributes;

}
