package com.spring_food.springfood.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    // Các cấu hình chung của application (không liên quan đến Security)
    // Security config đã được tách ra file SecurityConfig.java riêng
    // @EnableJpaAuditing đã được khai báo trong SpringfoodApplication.java
}
