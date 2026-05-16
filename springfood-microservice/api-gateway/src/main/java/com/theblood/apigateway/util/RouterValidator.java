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
            "/api/v1/auth/register-with-role",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/user/",
            "/actuator/**",
            "/api/v1/products/**",
            "/api/v1/sales/**",
            "/api/v1/shop/featured",
            "/api/v1/shops/**",
            "/api/v1/categories",
            "/api/v1/categories/",
            "/api/v1/payment-methods/**",
            "/api/v1/payment/vnpay-payment-return/**",
            "/api/v1/payment/vnpay/pay-existing-order/**",
            "/api/v1/payment/vnpay/from-reference/**",
            // NOTE: /api/v1/statistical-reports/** is intentionally NOT public.
            // The /me/* endpoints rely on the X-Shop-ID header injected from
            // the JWT and must be authenticated.
            "/api/v1/conversations/**",
            "/api/v1/messages/**",
            "/api/v1/chat/**",
            // WebSocket handshake endpoints - auth handled by chat service via STOMP CONNECT frame
            "/ws/**",
            "/ws-sockjs/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**"
    );

    /**
     * Endpoints whose <b>read</b> verbs are public but whose mutations need
     * a logged-in user. We list path patterns + the method whitelist so the
     * gateway lets {@code GET} through anonymously while still routing
     * {@code POST/PUT/DELETE} via the auth filter.
     */
    private static final List<String> READ_ONLY_PUBLIC_ENDPOINTS = List.of(
            // Reviews / feedback list — anyone can read product or shop reviews
            // even before signing in. Writes (create/edit/delete) are gated by
            // @PreAuthorize on the controller side.
            "/api/v1/feedback/**"
    );
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    public Predicate<ServerHttpRequest> isSecured =
            request -> {
                String path = request.getURI().getPath();
                String method = request.getMethod() != null ? request.getMethod().name() : "GET";

                // 1) Fully open endpoints — every method passes anonymously.
                boolean isOpenEndpoint = OPEN_API_ENDPOINTS.stream()
                        .anyMatch(pattern -> pathMatcher.match(pattern, path));

                if (isOpenEndpoint) {
                    return false; // not secured → let through
                }

                // 2) Read-only public endpoints — only GET passes; mutations
                //    fall through to the auth filter.
                boolean isReadOnlyPublic = "GET".equalsIgnoreCase(method)
                        && READ_ONLY_PUBLIC_ENDPOINTS.stream()
                        .anyMatch(pattern -> pathMatcher.match(pattern, path));

                if (isReadOnlyPublic) {
                    return false;
                }

                // Everything else must carry valid auth.
                return true;
            };
}
