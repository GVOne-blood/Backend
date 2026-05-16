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

                // Wishlist (lives in product-service) — /api/v1/wishlist/** -> /wishlist/**
                .route("product-service-wishlist", r -> r.path("/api/v1/wishlist/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://PRODUCT-SERVICE"))

                // Categories (lives in product-service)
                .route("category-service", r -> r.path("/api/v1/categories/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://PRODUCT-SERVICE"))

                // Sale (promotion) endpoints - hosted in product-service
                .route("product-service-sales", r -> r.path("/api/v1/sales/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://PRODUCT-SERVICE"))

                // Feedback (reviews + ratings) — hosted in product-service.
                // GET endpoints are public (read reviews on product detail);
                // POST/PUT/DELETE require auth, enforced by @PreAuthorize at
                // the controller layer.
                .route("product-service-feedback", r -> r.path("/api/v1/feedback/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://PRODUCT-SERVICE"))

                // Shop Service
                .route("shop-service", r -> r.path("/api/v1/shop/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://shop-service"))

                // Shop Registration (lives in shop-service)
                .route("shop-registration", r -> r.path("/api/v1/shop-registration/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://shop-service"))

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

                // Chat Service
                .route("chat-service", r -> r.path("/api/v1/chat/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://CHAT"))

                // Realtime push API (chat-service hosts STOMP) — đường dẫn nội bộ giữ /api/realtime
                .route("chat-realtime", r -> r.path("/api/realtime/**")
                        .uri("lb://CHAT"))
                
                // Chat WebSocket
                .route("chat-websocket", r -> r.path("/ws/**")
                        .uri("lb://CHAT"))

                // Statistical Report Service
                .route("statistical-report", r -> r.path("/api/v1/statistical-reports/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://statisticalReport"))
                
                .build();
    }
}
