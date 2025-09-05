package com.spring_food.springfood.service;

import com.spring_food.springfood.model.ENUM.TokenType;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(TokenType tokenType, UserDetails user);
    
    String extractUsername(String token, TokenType tokenType);

    boolean isValid(String token, UserDetails user, TokenType tokenType);
    
    Long getTokenExpiration(TokenType tokenType);
}
