package com.spring_food.springfood.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "product_category")
public class ProductCategory {

    @Id
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Id
    @ManyToOne
    @JoinColumn(name = "slug")
    private Categories categories;

    @Column(name = "category_name")
    private String categoryName;
}
