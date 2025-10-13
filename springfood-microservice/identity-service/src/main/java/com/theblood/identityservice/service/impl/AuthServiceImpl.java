package com.theblood.identityservice.service.impl;

import com.theblood.common.dto.response.LoginRequest;
import com.theblood.common.enums.CookieKey;
import com.theblood.common.enums.TokenType;
import com.theblood.common.exception.custom.InvalidDataException;
import com.theblood.common.util.CookieUtil;
import com.theblood.identityservice.config.RedisConfig;
import com.theblood.identityservice.dto.response.TokenResponse;
import com.theblood.identityservice.model.Token;
import com.theblood.identityservice.model.User;
import com.theblood.identityservice.repository.UserRepository;
import com.theblood.identityservice.service.AuthService;
import com.theblood.identityservice.service.JwtService;
import com.theblood.identityservice.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public TokenResponse refresh(HttpServletRequest request) {

        String token = request.getHeader("r_token");
        if (!StringUtils.isNotBlank(token)) throw new InvalidDataException("Token is not provided ");

        String username = jwtService.extractUsername(token, TokenType.REFRESH);
        Optional<User> user = userRepository.findByUsername(username);
        if (!jwtService.isValid(token, user.get(), TokenType.REFRESH)) throw new InvalidDataException("Token is not valid");

        String newAccessToken = jwtService.generateToken(TokenType.ACCESS, user.get());

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setRefreshToken(token);
        tokenResponse.setAccessToken(newAccessToken);
        tokenResponse.setUserId(user.get().getId().toString());
        tokenResponse.setUsername(user.get().getUsername());
        tokenResponse.setExpiresIn(jwtService.getTokenExpiration(TokenType.REFRESH));

        return tokenResponse;
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