package com.spring_food.springfood.config;

import com.spring_food.springfood.common.enums.CookieKey;
import com.spring_food.springfood.common.enums.TokenType;
import com.spring_food.springfood.service.JwtService;
import com.spring_food.springfood.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class PreFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        Optional<String> accessTokenOpt = extractCookieValue(request, CookieKey.ACCESS_TOKEN.name());

        if (accessTokenOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = accessTokenOpt.get();
        String username = null;

        try {
            username = jwtService.extractUsername(accessToken, TokenType.ACCESS);
        } catch (ExpiredJwtException e) {
            log.warn("JWT Access Token expired for URI: {}", request.getRequestURI());
            handleTokenRefresh(request, response);
            filterChain.doFilter(request, response);
            return;
        } catch (JwtException e) {
            log.error("Invalid JWT Access Token: {}. URI: {}", e.getMessage(), request.getRequestURI());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(username);
            if (jwtService.isValid(accessToken, userDetails, TokenType.ACCESS)) {
                setAuthenticationContext(userDetails, request);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleTokenRefresh(HttpServletRequest request, HttpServletResponse response) {
        extractCookieValue(request, CookieKey.REFRESH_TOKEN.name())
                .ifPresent(refreshToken -> {
                    try {
                        String username = jwtService.extractUsername(refreshToken, TokenType.REFRESH);
                        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(username);

                        if (jwtService.isValid(refreshToken, userDetails, TokenType.REFRESH)) {
                            String newAccessToken = jwtService.generateToken(TokenType.ACCESS, userDetails);

                            Cookie newAccessTokenCookie = new Cookie(CookieKey.ACCESS_TOKEN.name(), newAccessToken);
                            newAccessTokenCookie.setHttpOnly(true);
                            newAccessTokenCookie.setSecure(request.isSecure());
                            newAccessTokenCookie.setPath("/");
                            newAccessTokenCookie.setMaxAge(900);
                            response.addCookie(newAccessTokenCookie);

                            setAuthenticationContext(userDetails, request);
                            log.info("Successfully refreshed access token for user: {}", username);
                        }
                    } catch (JwtException ex) {
                        log.error("Could not refresh token: {}", ex.getMessage());
                    }
                });
    }

    private Optional<String> extractCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private void setAuthenticationContext(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}