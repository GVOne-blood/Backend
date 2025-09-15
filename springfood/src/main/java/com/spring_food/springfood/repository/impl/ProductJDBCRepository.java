package com.spring_food.springfood.repository.impl;

import com.spring_food.springfood.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class ProductJDBCRepository {

    private final JdbcTemplate jdbcTemplate;

    public Product findById(String id){

        
    }
}
