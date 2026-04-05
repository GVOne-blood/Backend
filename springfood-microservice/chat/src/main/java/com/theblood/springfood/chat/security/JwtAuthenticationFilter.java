package com.theblood.springfood.chat.security;

import com.theblood.springfood.common.enums.TokenType;
import com.theblood.springfood.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter for Chat Service
 * Uses JwtUtil from common module (same as authentication service)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Skip JWT filter for WebSocket handshake - authentication handled by WebSocketAuthInterceptor
        String path = request.getRequestURI();
        return path.startsWith("/ws");
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);

            // Extract claims using JwtUtil (same as authentication service)
            final Claims claims = jwtUtil.extractAllClaims(jwt, TokenType.ACCESS);

            final String userId = claims.getSubject();
            final String username = claims.get("username", String.class);

            // Extract authorities from permissions claim
            // The permissions claim contains a list of authority objects with "authority" field
            @SuppressWarnings("unchecked")
            List<?> permissions = claims.get("permissions", List.class);

            List<SimpleGrantedAuthority> authorities;
            if (permissions != null && !permissions.isEmpty()) {
                authorities = permissions.stream()
                    .map(perm -> {
                        // Handle both Map structure and direct String
                        if (perm instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> permMap = (Map<String, Object>) perm;
                            Object authorityObj = permMap.get("authority");
                            if (authorityObj != null) {
                                return new SimpleGrantedAuthority(authorityObj.toString());
                            }
                        } else if (perm instanceof String) {
                            return new SimpleGrantedAuthority((String) perm);
                        }
                        return null;
                    })
                    .filter(auth -> auth != null)
                    .collect(Collectors.toList());
            } else {
                // If no permissions claim, assign default USER role
                authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                log.debug("No permissions in token, assigned default ROLE_USER");
            }

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Validate token
                if (!jwtUtil.isTokenExpired(jwt, TokenType.ACCESS)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId,  // Use userId as principal
                        null,
                        authorities
                    );

                    // Add username to details for logging
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("JWT authentication successful for user: {} (ID: {}), authorities: {}",
                        username, userId, authorities);
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage(), e);
            // Log more details for debugging
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    final String jwt = authHeader.substring(7);
                    final Claims claims = jwtUtil.extractAllClaims(jwt, TokenType.ACCESS);
                    log.debug("Token claims - subject: {}, username: {}, permissions: {}",
                        claims.getSubject(),
                        claims.get("username"),
                        claims.get("permissions"));
                } catch (Exception ex) {
                    log.error("Failed to extract claims for debugging: {}", ex.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
