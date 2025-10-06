package com.theblood.apigateway.config;

import com.theblood.apigateway.filter.AuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final AuthenticationFilter filter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("identity-service", r -> r.path("/api/auth/**", "/api/users/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://identity-service"))

                .route("product-service", r -> r.path("/api/products/**", "/api/categories/**", "/api/galleries/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://product-service"))

                .route("order-service", r -> r.path("/api/orders/**", "/api/carts/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://order-service"))
                .build();
    }
}
