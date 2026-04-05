package com.theblood.authentication.service.impl;

import com.theblood.authentication.dto.response.TokenResponse;
import com.theblood.authentication.model.User;
import com.theblood.authentication.repository.UserRepository;
import com.theblood.authentication.service.AuthService;
import com.theblood.authentication.service.JwtService;
import com.theblood.authentication.service.UserService;
import com.theblood.springfood.common.dto.response.LoginRequest;
import com.theblood.springfood.common.enums.CookieKey;
import com.theblood.springfood.common.enums.TokenType;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import com.theblood.springfood.common.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    JwtService jwtService;
    UserRepository userRepository;
    UserService userService;
    AuthenticationManager authenticationManager;
    RedisTemplate<String, String> redisTemplate;

    @Override
    public TokenResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        String redisKey = "auth:login:A+Rtoken:" + loginRequest.getUsername();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new InvalidDataException("Invalid username or password");
        }
        User user = userService.findByUsername(loginRequest.getUsername());
        String accessToken = jwtService.generateToken(TokenType.ACCESS, user);
        String refreshToken = jwtService.generateToken(TokenType.REFRESH, user);

        // Tạo Access Token Cookie
        response.addCookie(CookieUtil.createCookie(CookieKey.ACCESS_TOKEN.name(), accessToken, 15 * 60));

        // Tạo Refresh Token Cookie
        response.addCookie(CookieUtil.createCookie(CookieKey.REFRESH_TOKEN.name(), refreshToken, 7 * 24 * 60 * 60));


        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(accessToken);
        tokenResponse.setRefreshToken(refreshToken);
        tokenResponse.setUserId(user.getId().toString());
        tokenResponse.setUsername(user.getUsername());

//        redisTemplate.opsForValue().set(
//                "auth:access:" + loginRequest.getUsername(),
//                accessToken,
//                15, TimeUnit.MINUTES
//        );
//
//        // Lưu refresh token
//        redisTemplate.opsForValue().set(
//                "auth:refresh:" + loginRequest.getUsername(),
//                refreshToken,
//                7, TimeUnit.DAYS
//        );
        return tokenResponse;
    }

    @Override
    public TokenResponse refresh(String refreshToken) {  // ✅ Nhận token trực tiếp

        // Validate token not null/empty
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new InvalidDataException("Refresh token is required");
        }

        try {
            // Validate refresh token
            if (jwtService.isTokenExpired(refreshToken, TokenType.REFRESH)) {
                throw new InvalidDataException("Refresh token has expired");
            }

            // Extract username
            String username = jwtService.extractUsername(refreshToken, TokenType.REFRESH);

            // Load user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new InvalidDataException("User not found"));

            // Validate token belongs to user
            if (!jwtService.isValid(refreshToken, user, TokenType.REFRESH)) {
                throw new InvalidDataException("Invalid refresh token");
            }

            // Generate new access token
            String newAccessToken = jwtService.generateToken(TokenType.ACCESS, user);

            // Build response
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccessToken(newAccessToken);
            tokenResponse.setRefreshToken(refreshToken);  // Return same refresh token
            tokenResponse.setUserId(user.getId().toString());
            tokenResponse.setUsername(user.getUsername());
            tokenResponse.setExpiresIn(jwtService.getTokenExpiration(TokenType.ACCESS)); // Access token expiry

            return tokenResponse;

        } catch (Exception e) {
            throw new InvalidDataException("Failed to refresh token: " + e.getMessage());
        }
    }


    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // Add tokens to blacklist trong Redis

        String accessToken = CookieUtil.getElementFromCookie(request, "ACCESS_TOKEN");
        String refreshToken = CookieUtil.getElementFromCookie(request, "REFRESH_TOKEN");
        long accessExpiry = jwtService.getExpirationTime(accessToken, TokenType.ACCESS);
        long refreshExpiry = jwtService.getExpirationTime(refreshToken, TokenType.REFRESH);

        redisTemplate.opsForValue().set(
                "blacklist:access:" + accessToken,
                "revoked",
                accessExpiry - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS
        );

        redisTemplate.opsForValue().set(
                "blacklist:refresh:" + refreshToken,
                "revoked",
                refreshExpiry - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS
        );
        response.addCookie(CookieUtil.createCookie(CookieKey.ACCESS_TOKEN.name(), null, 0));
        response.addCookie(CookieUtil.createCookie(CookieKey.REFRESH_TOKEN.name(), null, 0));

    }
}