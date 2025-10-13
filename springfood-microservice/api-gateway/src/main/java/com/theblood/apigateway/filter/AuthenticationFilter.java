package com.theblood.apigateway.filter;


import com.theblood.apigateway.dto.TokenRefreshRequest;
import com.theblood.apigateway.dto.TokenRefreshResponse;
import com.theblood.apigateway.util.JwtUtil;
import com.theblood.apigateway.util.RouterValidator;
import com.theblood.common.enums.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
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
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements GlobalFilter, GatewayFilter, Ordered {

    private final RouterValidator routerValidator;
    private final JwtUtil jwtUtil;
    private final WebClient.Builder webClientBuilder;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (routerValidator.isSecured.test(request)) {
            Optional<String> accessTokenOpt = extractCookieValue(request, "ACCESS_TOKEN");
            Optional<String> refreshTokenOpt = extractCookieValue(request, "REFRESH_TOKEN");

            if (accessTokenOpt.isEmpty()) {
                if (refreshTokenOpt.isEmpty()) {
                    return onError(exchange, "Both access and refresh token cookies are missing", HttpStatus.UNAUTHORIZED);
                } else {
                    log.info("Access token missing, attempting to refresh from refresh token");
                    return handleTokenRefresh(exchange, chain, refreshTokenOpt.get());
                }
            }

            String accessToken = accessTokenOpt.get();

            return isTokenBlacklisted(accessToken, TokenType.ACCESS)
                    .flatMap(isBlacklisted -> {
                        if (isBlacklisted) {
                            log.warn("Access token is blacklisted (revoked)");
                            return onError(exchange, "Access token has been revoked", HttpStatus.UNAUTHORIZED);
                        }

                        try {
                            jwtUtil.validateToken(accessToken);
                        } catch (JwtException e) {
                            if (!jwtUtil.isTokenExpired(accessToken)) {
                                log.warn("Access token validation failed: {}", e.getMessage());
                                return onError(exchange, "JWT validation error", HttpStatus.UNAUTHORIZED);
                            }
                        }

                        if (jwtUtil.isTokenExpired(accessToken)) {
                            log.info("Access token is expired, attempting refresh...");
                            if (refreshTokenOpt.isEmpty()) {
                                return onError(exchange, "Access token expired and refresh token is missing", HttpStatus.UNAUTHORIZED);
                            }
                            return handleTokenRefresh(exchange, chain, refreshTokenOpt.get());
                        } else {
                            ServerWebExchange newExchange = populateRequestWithHeaders(exchange, accessToken);
                            return chain.filter(newExchange);
                        }
                    });
        }

        return chain.filter(exchange);
    }

    /**
     * Check if token is blacklisted in Redis
     * @param token The JWT token to check
     * @param tokenType Type of token: TokenType.ACCESS or TokenType.REFRESH
     * @return Mono<Boolean> true if blacklisted, false otherwise
     */
    private Mono<Boolean> isTokenBlacklisted(String token, TokenType tokenType) {
        String key = "blacklist:" + tokenType.name().toLowerCase() + ":" + token;
        return reactiveRedisTemplate.opsForValue().get(key)
                .map("revoked"::equals)
                .defaultIfEmpty(false)
                .doOnNext(isBlacklisted -> {
                    if (isBlacklisted) {
                        log.warn("{} token found in blacklist: {}", tokenType, key);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Redis error while checking {} token blacklist: {}", tokenType, e.getMessage());
                    // SECURITY: Fail-closed approach - reject token if Redis is unavailable
                    // Alternative: Return Mono.just(false) for fail-open (allow if Redis fails)
                    return Mono.error(new RuntimeException("Token validation service unavailable"));
                });
    }

    private Mono<Void> handleTokenRefresh(ServerWebExchange exchange, GatewayFilterChain chain, String refreshToken) {
        // CRITICAL: Check if refresh token is blacklisted before using it
        return isTokenBlacklisted(refreshToken, TokenType.REFRESH)
                .flatMap(isBlacklisted -> {
                    if (isBlacklisted) {
                        log.warn("Refresh token is blacklisted (revoked). Rejecting refresh attempt.");
                        return onError(exchange, "Refresh token has been revoked", HttpStatus.UNAUTHORIZED);
                    }

                    // Validate refresh token expiration
                    try {
                        if (jwtUtil.isTokenExpired(refreshToken)) {
                            log.warn("Refresh token is expired");
                            return onError(exchange, "Refresh token has expired. Please login again.", HttpStatus.UNAUTHORIZED);
                        }
                    } catch (JwtException e) {
                        log.error("Refresh token validation failed: {}", e.getMessage());
                        return onError(exchange, "Invalid refresh token", HttpStatus.UNAUTHORIZED);
                    }

                    // Proceed with token refresh
                    TokenRefreshRequest refreshRequest = new TokenRefreshRequest(refreshToken);

                    return webClientBuilder.build().post()
                            .uri("lb://identity-service/api/auth/refresh-token")
                            .bodyValue(refreshRequest)
                            .retrieve()
                            .bodyToMono(TokenRefreshResponse.class)
                            .flatMap(response -> {
                                log.info("Successfully refreshed access token.");
                                String newAccessToken = response.getAccessToken();

                                exchange.getResponse().addCookie(ResponseCookie.from("ACCESS_TOKEN", newAccessToken)
                                        .httpOnly(true)
                                        .secure(true)  // Enable in production with HTTPS
                                        .sameSite("Strict")  // CSRF protection
                                        .path("/")
                                        .maxAge(900)
                                        .build());

                                ServerWebExchange newExchange = populateRequestWithHeaders(exchange, newAccessToken);
                                return chain.filter(newExchange);
                            })
                            .onErrorResume(error -> {
                                log.error("Could not refresh token: {}", error.getMessage());
                                return onError(exchange, "Failed to refresh token. Please login again.", HttpStatus.UNAUTHORIZED);
                            });
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

    @Override
    public int getOrder() {
        return -1;
    }
}
