package com.theblood.identityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.theblood.common", "com.theblood.identityservice"})
// Annotation này báo cho Spring Cloud biết rằng ứng dụng này cần phải
// tìm và đăng ký với một Discovery Server (như Eureka, Consul...).
@EnableDiscoveryClient
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }


}
