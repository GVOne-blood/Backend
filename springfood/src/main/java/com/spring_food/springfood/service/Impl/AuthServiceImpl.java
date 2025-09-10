package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.dto.request.LoginRequest;
import com.spring_food.springfood.dto.response.TokenResponse;
import com.spring_food.springfood.exception.custom.InvalidDataException;
import com.spring_food.springfood.model.ENUM.TokenType;
import com.spring_food.springfood.model.User;
import com.spring_food.springfood.repository.UserRepository;
import com.spring_food.springfood.service.AuthService;
import com.spring_food.springfood.service.JwtService;
import com.spring_food.springfood.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    JwtService jwtService;
    UserRepository userRepository;
    UserService userService;
    AuthenticationManager authenticationManager;
    @Override
    public TokenResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new AuthenticationException("Invalid username or password") {
            };
        }

            User user = userService.findByUsername(loginRequest.getUsername());
            String accessToken = jwtService.generateToken(TokenType.ACCESS, user);
            String refreshToken = jwtService.generateToken(TokenType.REFRESH, user);
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccessToken(accessToken);
            tokenResponse.setRefreshToken(refreshToken);
            tokenResponse.setUserId(user.getId());
            tokenResponse.setUsername(user.getUsername());

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
        tokenResponse.setUserId(user.get().getId());
        tokenResponse.setUsername(user.get().getUsername());
       tokenResponse.setExpiresIn(jwtService.getTokenExpiration(TokenType.REFRESH));

        return tokenResponse;
    }
}
