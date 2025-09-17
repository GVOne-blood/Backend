package com.spring_food.springfood.dto.request;

import com.spring_food.springfood.common.enums.ProductStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ProductRequest {
    @NotBlank(message = "Shop ID is required")
    String shopId;
    @NotBlank(message = "Categories name is required")
    String categoryNames;
    @NotBlank(message = "Product name is required")
    @Size(max = 100, min = 3, message = "Product name must be between 3 and 100 characters")
    String name;

    String description;

    @NotBlank(message = "Price is required")
    @Min(0)
    @Max(1000000000)
    String price;

    @NotBlank(message = "Image is required")
    String images;

    @Min(0)
    @Max(327670)
    Integer quantity;

    @NotBlank(message = "sku is required")
    String sku;

    LocalDate msg;
    LocalDate exp;


    ProductStatus status = ProductStatus.AVAILABLE;
    @NotBlank(message = "Wholesale price is required")
    @Min(0)
    @Max(1000000000)
    String wholesalePrice;
    
    @Min(0)
    @Max(5)
    java.math.BigDecimal avgRate;

}

