package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args); // <--- Thằng này tạo ra ApplicationContext, nó sẽ scan các class có annotation @Component, @Service, @Repository, @Controller, @RestController, @Configuration
    }

}
