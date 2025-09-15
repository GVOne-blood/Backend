package com.spring_food.springfood.repository;

import com.spring_food.springfood.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CustomProductRepository {
    Page<Product> findByPrice(BigDecimal from, BigDecimal to, Pageable pageable);
}
