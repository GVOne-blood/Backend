package com.theblood.apigateway.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    // Public endpoints không cần authentication
    private static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/api/v1/user/",
            "/actuator/**",
            "/api/v1/products/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**"
    );
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    public Predicate<ServerHttpRequest> isSecured =
            request -> {
                String path = request.getURI().getPath();

                // Nếu path match bất kỳ open endpoint nào → KHÔNG secured (cho qua)
                boolean isOpenEndpoint = OPEN_API_ENDPOINTS.stream()
                        .anyMatch(pattern -> pathMatcher.match(pattern, path));

                // Return true nếu KHÔNG phải open endpoint (cần secured)
                return !isOpenEndpoint;
            };
}
