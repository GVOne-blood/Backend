package com.spring_food.springfood.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class AppConfig {

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate (DataSource dataSource){
        return new NamedParameterJdbcTemplate(dataSource);
    }

    // Các cấu hình chung của application (không liên quan đến Security)
    // Security config đã được tách ra file SecurityConfig.java riêng
    // @EnableJpaAuditing đã được khai báo trong SpringfoodApplication.java
}
