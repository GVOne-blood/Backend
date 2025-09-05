package com.spring_food.springfood.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "product_tag")
@Getter
@Setter
@IdClass(ProductTag.ProductTagId.class)
public class ProductTag {
    @Id
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Id
    @ManyToOne
    @JoinColumn(name = "tag_name", referencedColumnName = "tag_name")
    private Tag tag;

    @Getter
    @Setter
    public static class ProductTagId implements Serializable {
        private String product;
        private String tag;
    }
}
