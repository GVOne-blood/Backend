package com.theblood.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Instant;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class OrderServiceApplication {

    public static void main(String[] args) {
        System.out.println(Instant.now());

        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
