package com.spring_food.springfood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpringfoodApplication {

    public static void main(String[] args) {
        System.setProperty("spring.cglib.debugLocation", "target/cglib"); // Lưu vào thư mục target/cglib
        SpringApplication.run(SpringfoodApplication.class, args);

    }

}
