package com.theblood.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Authentication Service
                .route("authentication-service-user", r -> r.path("/api/v1/user/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://AUTHENTICATION"))
                
                .route("authentication-service-auth", r -> r.path("/api/v1/auth/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://AUTHENTICATION"))
                
                .route("authentication-service-address", r -> r.path("/api/v1/profile/addr/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://AUTHENTICATION"))

                // Product Service
                .route("product-service", r -> r.path("/api/v1/products/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://PRODUCT-SERVICE"))

                // Order Service
                .route("order-service", r -> r.path("/api/v1/order/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://ORDER-SERVICE"))

                // Cart Service
                .route("cart-service", r -> r.path("/api/v1/cart/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://CART-SERVICE"))

                // Payment Service
                .route("payment-service", r -> r.path("/api/v1/payment/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://PAYMENT-SERVICE"))
                
                .build();
    }
}
