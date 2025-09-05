package com.spring_food.springfood.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "product_sale")
@Getter
@Setter
@IdClass(ProductSale.ProductSaleId.class)
public class ProductSale {
    @Id
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Id
    @ManyToOne
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @Getter
    @Setter
    public static class ProductSaleId implements Serializable {
        private String product;
        private String sale;
    }
}
