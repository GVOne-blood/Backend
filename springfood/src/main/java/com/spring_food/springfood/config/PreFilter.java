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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

        // Nếu đã có người được xác thực, không cần làm gì nữa
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<String> accessTokenOpt = extractCookieValue(request, CookieKey.ACCESS_TOKEN.name());

        // Nếu không có Access Token, thử refresh.
        // Nếu có Access Token nhưng hết hạn, cũng thử refresh.
        // Các trường hợp khác (token sai chữ ký...), bỏ qua.
        if (accessTokenOpt.isEmpty() || isTokenExpired(accessTokenOpt.get())) {
            handleTokenRefresh(request, response);
        } else {
            // Nếu có Access Token và có vẻ còn hạn, thử xác thực
            try {
                String accessToken = accessTokenOpt.get();
                String username = jwtService.extractUsername(accessToken, TokenType.ACCESS);
                UserDetails userDetails = userService.userDetailsService().loadUserByUsername(username);
                if (jwtService.isValid(accessToken, userDetails, TokenType.ACCESS)) {
                    setAuthenticationContext(userDetails, request);
                }
            } catch (JwtException | UsernameNotFoundException ex) {
                log.warn("An error occurred during access token validation: {}", ex.getMessage());
            }
        }

        // Sau khi đã cố gắng xác thực (bằng access hoặc refresh token),
        // chuyển tiếp request. Spring Security sẽ lo phần còn lại.
        filterChain.doFilter(request, response);
    }

    private boolean isTokenExpired(String token) {
        try {
            return jwtService.isTokenExpired(token, TokenType.ACCESS);
        } catch (ExpiredJwtException e) {
            // Bắt ngoại lệ để biết chắc chắn là nó đã hết hạn
            log.info("Token het han ");
            return true;
        } catch (JwtException e) {
            log.warn("Loi token ");
            // Các lỗi khác (sai chữ ký, malformed...) coi như không hết hạn (mà là không hợp lệ)
            return false;
        }
    }

    private void handleTokenRefresh(HttpServletRequest request, HttpServletResponse response) {
        extractCookieValue(request, CookieKey.REFRESH_TOKEN.name())
                .ifPresent(refreshToken -> {
                    try {
                        String username = jwtService.extractUsername(refreshToken, TokenType.REFRESH);
                        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(username);

                        if (jwtService.isValid(refreshToken, userDetails, TokenType.REFRESH)) {
                            String newAccessToken = jwtService.generateToken(TokenType.ACCESS, userDetails);
                            addAccessTokenCookie(request, response, newAccessToken);
                            setAuthenticationContext(userDetails, request);
                            log.info("Successfully refreshed access token for user: {}", username);
                        }
                    } catch (JwtException | UsernameNotFoundException ex) {
                        log.warn("Could not refresh token: {}", ex.getMessage());
                    }
                });
    }

    private void addAccessTokenCookie(HttpServletRequest request, HttpServletResponse response, String token) {
        Cookie newAccessTokenCookie = new Cookie(CookieKey.ACCESS_TOKEN.name(), token);
        newAccessTokenCookie.setHttpOnly(true);
        newAccessTokenCookie.setSecure(request.isSecure());
        newAccessTokenCookie.setPath("/");
        newAccessTokenCookie.setMaxAge(900);
        response.addCookie(newAccessTokenCookie);
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