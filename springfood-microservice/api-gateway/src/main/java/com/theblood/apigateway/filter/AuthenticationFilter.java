package com.theblood.apigateway.filter;

import com.theblood.apigateway.dto.TokenRefreshRequest;
import com.theblood.apigateway.dto.TokenRefreshResponse;
import com.theblood.apigateway.util.JwtUtil;
import com.theblood.apigateway.util.RouterValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@Slf4j
public class AuthenticationFilter implements GatewayFilter {

    private final RouterValidator routerValidator;
    private final JwtUtil jwtUtil;
    private final WebClient.Builder webClientBuilder;

    public AuthenticationFilter(RouterValidator routerValidator, JwtUtil jwtUtil, WebClient.Builder webClientBuilder) {
        this.routerValidator = routerValidator;
        this.jwtUtil = jwtUtil;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (routerValidator.isSecured.test(request)) {
            Optional<String> accessTokenOpt = extractCookieValue(request, "access_token");

            if (accessTokenOpt.isEmpty()) {
                return onError(exchange, "Access token cookie is missing", HttpStatus.UNAUTHORIZED);
            }

            String accessToken = accessTokenOpt.get();

            try {
                jwtUtil.validateToken(accessToken);
            } catch (JwtException e) {
                // If any JWT exception other than expiration, reject
                if (!jwtUtil.isTokenExpired(accessToken)) {
                    log.warn("Access token validation failed: {}", e.getMessage());
                    return onError(exchange, "JWT validation error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                }
            }

            if (jwtUtil.isTokenExpired(accessToken)) {
                log.info("Access token is expired, attempting refresh...");
                return handleTokenRefresh(exchange, chain);
            } else {
                // Token is valid
                ServerWebExchange newExchange = populateRequestWithHeaders(exchange, accessToken);
                return chain.filter(newExchange);
            }
        }

        return chain.filter(exchange);
    }

    private Mono<Void> handleTokenRefresh(ServerWebExchange exchange, GatewayFilterChain chain) {
        Optional<String> refreshTokenOpt = extractCookieValue(exchange.getRequest(), "refresh_token");

        if (refreshTokenOpt.isEmpty()) {
            return onError(exchange, "Refresh token cookie is missing", HttpStatus.UNAUTHORIZED);
        }

        TokenRefreshRequest refreshRequest = new TokenRefreshRequest(refreshTokenOpt.get());

        return webClientBuilder.build().post()
                .uri("lb://identity-service/api/auth/refresh-token")
                .bodyValue(refreshRequest)
                .retrieve()
                .bodyToMono(TokenRefreshResponse.class)
                .flatMap(response -> {
                    log.info("Successfully refreshed access token.");
                    String newAccessToken = response.getAccessToken();
                    // Add new access token to the response cookies
                    exchange.getResponse().addCookie(ResponseCookie.from("access_token", newAccessToken)
                            .httpOnly(true)
                            .path("/")
                            .maxAge(900)
                            .build());

                    // Populate headers and continue the chain
                    ServerWebExchange newExchange = populateRequestWithHeaders(exchange, newAccessToken);
                    return chain.filter(newExchange);
                })
                .onErrorResume(error -> {
                    log.error("Could not refresh token: {}", error.getMessage());
                    return onError(exchange, "Failed to refresh token", HttpStatus.UNAUTHORIZED);
                });
    }

    private ServerWebExchange populateRequestWithHeaders(ServerWebExchange exchange, String token) {
        Claims claims = jwtUtil.extractAllClaims(token);
        String username = claims.get("username", String.class);
        Object roles = claims.get("roles");

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Username", username)
                .header("X-User-Roles", roles != null ? roles.toString() : "")
                .build();

        log.info("Forwarding request for user '{}' with roles '{}' to downstream service", username, roles);
        return exchange.mutate().request(mutatedRequest).build();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        log.error("Authentication Error: {}", err);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private Optional<String> extractCookieValue(ServerHttpRequest request, String cookieName) {
        HttpCookie cookie = request.getCookies().getFirst(cookieName);
        return Optional.ofNullable(cookie).map(HttpCookie::getValue);
    }
}
