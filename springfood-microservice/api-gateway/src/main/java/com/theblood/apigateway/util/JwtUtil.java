package com.theblood.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    @Value("${app.jwt.secretKey}")
    private String secretKey;

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void validateToken(String token) {
        Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new java.util.Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    private java.util.Date extractExpiration(String token) {
        return extractClaim(token, "exp", java.util.Date.class);
    }

    public <T> T extractClaim(String token, String name, Class<T> type) {
        final Claims claims = extractAllClaims(token);
        return claims.get(name, type);
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
