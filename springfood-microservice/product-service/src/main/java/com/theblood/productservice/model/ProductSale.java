package com.theblood.productservice.model;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "product_sales")
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
    @EqualsAndHashCode
    public static class ProductSaleId implements Serializable {
        private UUID product;
        private UUID sale;
    }
}
