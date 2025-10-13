package com.theblood.identityservice.service;

import com.theblood.common.enums.TokenType;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(TokenType tokenType, UserDetails user);

    /**
     * Extract expiration timestamp (milliseconds) from existing token
     * @param token JWT token
     * @param tokenType Token type for signature verification
     * @return Expiration time in milliseconds since epoch
     */
    long getExpirationTime(String token, TokenType tokenType);

    /**
     * Calculate remaining TTL (time to live) in milliseconds
     * @param token JWT token
     * @param tokenType Token type for signature verification
     * @return Remaining TTL in milliseconds
     */
    long getRemainingTTL(String token, TokenType tokenType);

    String extractUsername(String token, TokenType tokenType);

    boolean isValid(String token, UserDetails user, TokenType tokenType);

    Long getTokenExpiration(TokenType tokenType);

    boolean isTokenExpired(String token, TokenType tokenType);
}