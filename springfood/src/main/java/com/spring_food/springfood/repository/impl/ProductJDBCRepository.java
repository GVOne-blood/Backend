package com.spring_food.springfood.repository.impl;

import com.spring_food.springfood.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductJDBCRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    // private final BeanPropertyRowMapper beanPropertyRowMapper;
    public Optional<Product> findById(String productId) {

        String sql = "SELECT p.* FROM product p WHERE product_id = ?";
        try {
            Product product = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Product.class), productId); // query, mapper, field variable
            return Optional.ofNullable(product);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

//named
    public Page<Product> findByPrice(BigDecimal from, BigDecimal to, Pageable pageable) {
        String sql = " SELECT * FROM product WHERE price > :from and price < :to " +
                        "ORDER BY product_id " +
                        "LIMIT :limit OFFSET :offset ";
        String sqlCount = " SELECT COUNT(*) FROM product WHERE price > :from and price < :to ";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("from", from)
                .addValue("to", to)
               // .addValue("sort", pageable.getSort().toString())
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        SqlParameterSource paramsCount = new MapSqlParameterSource()
                .addValue("from", from)
                .addValue("to", to);

        List<Product> products 
         = namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Product.class));

        Integer total = namedParameterJdbcTemplate.queryForObject(sqlCount, paramsCount, Integer.class);

        return new PageImpl<>(products, pageable, total == null ? 0 : total);
    }
}