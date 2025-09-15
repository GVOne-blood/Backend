package com.spring_food.springfood.specification;

import com.spring_food.springfood.model.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> priceBetween(BigDecimal from, BigDecimal to){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.between(root.get("price"), from, to);
    }

    public static Specification<Product> nameContain(String name){
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.like(root.get("product_name"), name);
    }


}
