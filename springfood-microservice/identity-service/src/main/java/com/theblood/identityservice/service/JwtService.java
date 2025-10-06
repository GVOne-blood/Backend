package com.theblood.identityservice.service;

import com.theblood.common.enums.TokenType;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(TokenType tokenType, UserDetails user);

    String extractUsername(String token, TokenType tokenType);

    boolean isValid(String token, UserDetails user, TokenType tokenType);

    Long getTokenExpiration(TokenType tokenType);

    boolean isTokenExpired(String token, TokenType tokenType);
}