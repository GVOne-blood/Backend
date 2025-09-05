package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.model.ENUM.TokenType;
import com.spring_food.springfood.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.accessTokenExpiration:900000}") // 15 minutes default
    private long accessTokenExpiration;
    
    @Value("${jwt.refreshTokenExpiration:604800000}") // 7 days default
    private long refreshTokenExpiration;
    
    @Value("${jwt.resetTokenExpiration:300000}") // 5 minutes default
    private long resetTokenExpiration;
    
    @Value("${jwt.secretKey}")
    private String secretKey;
    
    @Value("${jwt.refreshKey}")
    private String refreshKey;
    
    @Value("${jwt.resetKey}")
    private String resetKey;


    @Override
    public String generateToken(TokenType tokenType, UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", tokenType.name());
        claims.put("username", user.getUsername());
        
        return createToken(claims, user.getUsername(), tokenType);
    }

    @Override
    public String extractUsername(String token, TokenType tokenType) {
        return extractClaim(token, tokenType, Claims::getSubject);
    }

    @Override
    public boolean isValid(String token, UserDetails user, TokenType tokenType) {
        try {
            final String username = extractUsername(token, tokenType);
            final String tokenTypeInClaim = extractClaim(token, tokenType, claims -> claims.get("type", String.class));
            
            return username.equals(user.getUsername()) 
                    && tokenType.name().equals(tokenTypeInClaim)
                    && !isTokenExpired(token, tokenType);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public Long getTokenExpiration(TokenType tokenType) {
        switch (tokenType) {
            case ACCESS_TOKEN:
                return accessTokenExpiration;
            case REFRESH_TOKEN:
                return refreshTokenExpiration;
            case RESET_TOKEN:
                return resetTokenExpiration;
            default:
                throw new IllegalArgumentException("Unknown token type: " + tokenType);
        }
    }

    private String createToken(Map<String, Object> claims, String subject, TokenType tokenType) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + getTokenExpiration(tokenType));
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey(tokenType), getSignatureAlgorithm(tokenType))
                .compact();
    }
    
    private SignatureAlgorithm getSignatureAlgorithm(TokenType tokenType) {
        switch (tokenType) {
            case ACCESS_TOKEN:
            case REFRESH_TOKEN:
                return SignatureAlgorithm.HS256;
            case RESET_TOKEN:
                return SignatureAlgorithm.HS512;
            default:
                return SignatureAlgorithm.HS256;
        }
    }

    private Key getSigningKey(TokenType tokenType) {
        byte[] keyBytes;
        
        switch (tokenType) {
            case ACCESS_TOKEN:
                keyBytes = Decoders.BASE64.decode(secretKey);
                break;
            case REFRESH_TOKEN:
                keyBytes = Decoders.BASE64.decode(refreshKey);
                break;
            case RESET_TOKEN:
                keyBytes = Decoders.BASE64.decode(resetKey);
                break;
            default:
                throw new IllegalArgumentException("Unknown token type: " + tokenType);
        }
        
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private <T> T extractClaim(String token, TokenType tokenType, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, tokenType);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token, TokenType tokenType) {
        return extractExpiration(token, tokenType).before(new Date());
    }

    private Date extractExpiration(String token, TokenType tokenType) {
        return extractClaim(token, tokenType, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token, TokenType tokenType) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(tokenType))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
