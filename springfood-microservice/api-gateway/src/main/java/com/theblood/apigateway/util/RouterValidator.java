package com.theblood.apigateway.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    @Value("${app.jwt.open-api-endpoints}")
    private List<String> openApiEndpoints;

    public Predicate<ServerHttpRequest> isSecured;

    @jakarta.annotation.PostConstruct
    private void init() {
        isSecured = request -> openApiEndpoints
                .stream()
                .noneMatch(uri -> request.getURI().getPath().contains(uri));
    }
}
