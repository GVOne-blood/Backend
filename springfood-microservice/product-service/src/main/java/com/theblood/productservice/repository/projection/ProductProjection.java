package com.theblood.productservice.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface ProductProjection {
    UUID getId();
    String getName();
    String getDescription();
    BigDecimal getPrice();
    String getImages();
    Integer getQuantity();
    LocalDate getMsg();
    LocalDate getExp();
}
