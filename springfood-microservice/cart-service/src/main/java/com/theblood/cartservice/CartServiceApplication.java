package com.theblood.cartservice;

import com.theblood.cartservice.config.DotenvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class CartServiceApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CartServiceApplication.class);
        app.addInitializers(new DotenvConfig());
        app.run(args);
    }

}
