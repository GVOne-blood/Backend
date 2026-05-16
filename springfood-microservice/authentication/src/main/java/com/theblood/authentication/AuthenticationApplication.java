package com.theblood.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(
    scanBasePackages = {"com.theblood.springfood.common", "com.theblood.authentication"},
    exclude = {org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class}
)
// Annotation này báo cho Spring Cloud biết rằng ứng dụng này cần phải
// tìm và đăng ký với một Discovery Server (như Eureka, Consul...).
@EnableDiscoveryClient
public class AuthenticationApplication {

    public static void main(String[] args) {
        io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(AuthenticationApplication.class, args);
    }


}
