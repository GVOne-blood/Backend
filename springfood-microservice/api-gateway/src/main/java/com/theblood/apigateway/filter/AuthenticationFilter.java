package com.theblood.apigateway.filter;

import com.theblood.apigateway.util.JwtUtil;
import com.theblood.apigateway.util.RouterValidator;
import com.theblood.common.dto.request.TokenRefreshRequest;
import com.theblood.common.dto.response.TokenResponse;
import com.theblood.common.enums.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
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

import java.util.*;

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
        String path = request.getURI().getPath();

        // Kiểm tra endpoint có cần authentication không
        boolean isSecuredEndpoint = routerValidator.isSecured.test(request);

        Optional<String> accessTokenOpt = extractCookieValue(request, "ACCESS_TOKEN");
        Optional<String> refreshTokenOpt = extractCookieValue(request, "REFRESH_TOKEN");

        // Case 1: PUBLIC ENDPOINT
        if (!isSecuredEndpoint) {
            log.debug("Public endpoint accessed: {}", path);

            // Nếu có token, validate và populate headers (optional authentication)
            if (accessTokenOpt.isPresent()) {
                String accessToken = accessTokenOpt.get();

                return isTokenBlacklisted(accessToken, TokenType.ACCESS)
                        .flatMap(isBlacklisted -> {
                            if (isBlacklisted) {
                                log.warn("Blacklisted token used on public endpoint, ignoring authentication");
                                return chain.filter(exchange); // Cho qua nhưng không set headers
                            }

                            try {
                                jwtUtil.validateToken(accessToken);

                                if (jwtUtil.isTokenExpired(accessToken)) {
                                    log.debug("Expired token on public endpoint, proceeding without authentication");
                                    return chain.filter(exchange);
                                }

                                // Token hợp lệ → populate headers
                                log.info("Valid token found on public endpoint, populating user headers");
                                ServerWebExchange newExchange = populateRequestWithHeaders(exchange, accessToken);
                                return chain.filter(newExchange);

                            } catch (JwtException e) {
                                log.warn("Invalid token on public endpoint: {}, proceeding without authentication", e.getMessage());
                                return chain.filter(exchange);
                            }
                        })
                        .onErrorResume(e -> {
                            log.error("Error validating token on public endpoint: {}, proceeding without authentication", e.getMessage());
                            return chain.filter(exchange);
                        });
            }

            // Case 2: SECURED ENDPOINT - Token is MANDATORY
            log.debug("Secured endpoint accessed: {}", path);

            // Không có access token
            if (accessTokenOpt.isEmpty()) {
                // Không có cả refresh token → reject ngay
                if (refreshTokenOpt.isEmpty()) {
                    log.warn("Secured endpoint accessed without tokens: {}", path);
                    return onError(exchange, "Authentication required. Please login.", HttpStatus.UNAUTHORIZED);
                }

                // Có refresh token → attempt refresh
                log.info("Access token missing on secured endpoint, attempting refresh");
                return handleTokenRefresh(exchange, chain, refreshTokenOpt.get());
            }

            // Có access token → validate
            String accessToken = accessTokenOpt.get();

            return isTokenBlacklisted(accessToken, TokenType.ACCESS)
                    .flatMap(isBlacklisted -> {
                        if (isBlacklisted) {
                            log.warn("Blacklisted access token used on secured endpoint");
                            return onError(exchange, "Access token has been revoked", HttpStatus.UNAUTHORIZED);
                        }

                        try {
                            jwtUtil.validateToken(accessToken);
                        } catch (JwtException e) {
                            if (!jwtUtil.isTokenExpired(accessToken)) {
                                log.warn("Invalid access token on secured endpoint: {}", e.getMessage());
                                return onError(exchange, "Invalid access token", HttpStatus.UNAUTHORIZED);
                            }
                        }

                        // Check expiration
                        if (jwtUtil.isTokenExpired(accessToken)) {
                            log.info("Access token expired on secured endpoint, attempting refresh");

                            if (refreshTokenOpt.isEmpty()) {
                                log.warn("Access token expired and no refresh token available");
                                return onError(exchange, "Access token expired. Please login again.", HttpStatus.UNAUTHORIZED);
                            }

                            return handleTokenRefresh(exchange, chain, refreshTokenOpt.get());
                        }

                        // Token valid → proceed
                        log.debug("Valid access token, proceeding with request");
                        ServerWebExchange newExchange = populateRequestWithHeaders(exchange, accessToken);
                        return chain.filter(newExchange);
                    });
            
        }
        // Không có token → cho qua luôn
        log.debug("No token provided for public endpoint, proceeding without authentication");
        return chain.filter(exchange);
    }

    /**
     * Check if token is blacklisted in Redis
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
                    // SECURITY: Fail-closed approach
                    return Mono.error(new RuntimeException("Token validation service unavailable"));
                });
    }

    /**
     * Handle token refresh
     */
    private Mono<Void> handleTokenRefresh(ServerWebExchange exchange, GatewayFilterChain chain, String refreshToken) {
        return isTokenBlacklisted(refreshToken, TokenType.REFRESH)
                .flatMap(isBlacklisted -> {
                    if (isBlacklisted) {
                        log.warn("Blacklisted refresh token used");
                        return onError(exchange, "Refresh token has been revoked", HttpStatus.UNAUTHORIZED);
                    }

                    // Validate refresh token
                    try {
                        if (jwtUtil.isTokenExpired(refreshToken)) {
                            log.warn("Refresh token is expired");
                            return onError(exchange, "Refresh token has expired. Please login again.", HttpStatus.UNAUTHORIZED);
                        }
                    } catch (JwtException e) {
                        log.error("Refresh token validation failed: {}", e.getMessage());
                        return onError(exchange, "Invalid refresh token", HttpStatus.UNAUTHORIZED);
                    }

                    // Call identity-service để refresh
                    TokenRefreshRequest refreshRequest = new TokenRefreshRequest(refreshToken);

                    return webClientBuilder.build().post()
                            .uri("lb://identity-service/auth/refresh")
                            .bodyValue(refreshRequest)
                            .retrieve()
                            .bodyToMono(TokenResponse.class)
                            .flatMap(response -> {
                                log.info("Successfully refreshed access token");
                                String newAccessToken = response.getAccessToken();

                                // Set new access token cookie
                                exchange.getResponse().addCookie(ResponseCookie.from("ACCESS_TOKEN", newAccessToken)
                                        .httpOnly(true)
                                        .secure(true)
                                        .sameSite("Strict")
                                        .path("/")
                                        .maxAge(900) // 15 minutes
                                        .build());

                                // Populate headers và proceed
                                ServerWebExchange newExchange = populateRequestWithHeaders(exchange, newAccessToken);
                                return chain.filter(newExchange);
                            })
                            .onErrorResume(error -> {
                                log.error("Token refresh failed: {}", error.getMessage());
                                return onError(exchange, "Failed to refresh token. Please login again.", HttpStatus.UNAUTHORIZED);
                            });
                });
    }

    /**
     * Extract user info từ JWT và populate headers
     */
    private ServerWebExchange populateRequestWithHeaders(ServerWebExchange exchange, String token) {
        Claims claims = jwtUtil.extractAllClaims(token);

        String username = claims.get("username", String.class);
        UUID userId = UUID.fromString(claims.getSubject());
        List<?> authoritiesFromJwt = claims.get("permissions", List.class);

        if (authoritiesFromJwt == null) {
            authoritiesFromJwt = Collections.emptyList();
        }

        List<String> roles = new ArrayList<>();
        List<String> authorities = new ArrayList<>();

        // Parse authorities: {"authority": "ROLE_XXX"} hoặc {"authority": "permission:action"}
        for (Object item : authoritiesFromJwt) {
            if (item instanceof Map) {
                Map<?, ?> authMap = (Map<?, ?>) item;
                String authority = (String) authMap.get("authority");

                if (authority != null) {
                    if (authority.startsWith("ROLE_")) {
                        roles.add(authority);
                    } else {
                        authorities.add(authority);
                    }
                }
            }
        }

        String rolesHeader = String.join(",", roles);
        String authoritiesHeader = String.join(",", authorities);

        log.info("Populating headers for user '{}': roles=[{}], authorities=[{}]",
                username, rolesHeader, authoritiesHeader);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-ID", userId.toString())
                .header("X-User-Username", username)
                .header("X-User-Roles", rolesHeader)
                .header("X-User-Authorities", authoritiesHeader)
                .build();

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
