package com.theblood.apigateway.util;

import com.theblood.common.enums.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@Slf4j
public class JwtUtil {

    @Value("${app.jwt.secretKey}")
    private String secretKey;

    @Value("${app.jwt.refreshKey}")
    private String refreshKey;

    @Value("${app.jwt.resetKey:#{null}}")  // Optional
    private String resetKey;

    /**
     * Extract all claims from token using appropriate key based on TokenType
     */
    public Claims extractAllClaims(String token, TokenType tokenType) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(tokenType))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract all claims - Auto-detect token type from claims
     */
    public Claims extractAllClaims(String token) {
        // Try ACCESS token first (most common)
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(TokenType.ACCESS))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Verify token type matches
            String type = claims.get("type", String.class);
            if ("ACCESS".equals(type)) {
                return claims;
            }
        } catch (Exception e) {
            // Not an access token or invalid, try refresh
        }

        // Try REFRESH token
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(TokenType.REFRESH))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String type = claims.get("type", String.class);
            if ("REFRESH".equals(type)) {
                return claims;
            }
        } catch (Exception e) {
            // Not a refresh token
        }

        // Last resort: try RESET token
        if (resetKey != null) {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(TokenType.RESET))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }

        throw new IllegalArgumentException("Token is not valid for any known token type");
    }

    /**
     * Validate token with specific token type
     */
    public void validateToken(String token, TokenType tokenType) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(tokenType))
                    .build()
                    .parseClaimsJws(token);

            log.debug("Token validation successful for type: {}", tokenType);

        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("Malformed token: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.warn("Invalid token signature: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validate token - Auto-detect type (backward compatible)
     */
    public void validateToken(String token) {
        // Extract claims to auto-detect and validate
        extractAllClaims(token);
    }

    /**
     * Check if token is expired with specific type
     */
    public boolean isTokenExpired(String token, TokenType tokenType) {
        try {
            Claims claims = extractAllClaims(token, tokenType);
            return claims.getExpiration().before(new java.util.Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true; // Treat errors as expired for security
        }
    }

    /**
     * Check if token is expired - Auto-detect type (backward compatible)
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().before(new java.util.Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Extract specific claim
     */
    public <T> T extractClaim(String token, String claimName, Class<T> type) {
        final Claims claims = extractAllClaims(token);
        return claims.get(claimName, type);
    }

    /**
     * Extract specific claim with token type
     */
    public <T> T extractClaim(String token, TokenType tokenType, String claimName, Class<T> type) {
        final Claims claims = extractAllClaims(token, tokenType);
        return claims.get(claimName, type);
    }

    /**
     * Get signing key based on token type
     */
    private Key getSigningKey(TokenType tokenType) {
        byte[] keyBytes;

        switch (tokenType) {
            case ACCESS:
                keyBytes = Decoders.BASE64.decode(secretKey);
                break;
            case REFRESH:
                keyBytes = Decoders.BASE64.decode(refreshKey);
                break;
            case RESET:
                if (resetKey == null) {
                    throw new IllegalArgumentException("Reset key is not configured");
                }
                keyBytes = Decoders.BASE64.decode(resetKey);
                break;
            default:
                throw new IllegalArgumentException("Unknown token type: " + tokenType);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get token type from token claims
     */
    public TokenType getTokenType(String token) {
        try {
            // Try to extract without validation first
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }

            // Decode payload (without signature verification)
            String payload = new String(Decoders.BASE64.decode(parts[1]));

            if (payload.contains("\"type\":\"ACCESS\"")) {
                return TokenType.ACCESS;
            } else if (payload.contains("\"type\":\"REFRESH\"")) {
                return TokenType.REFRESH;
            } else if (payload.contains("\"type\":\"RESET\"")) {
                return TokenType.RESET;
            }

            // Fallback: try extracting claims
            Claims claims = extractAllClaims(token);
            String type = claims.get("type", String.class);
            return TokenType.valueOf(type);

        } catch (Exception e) {
            log.error("Unable to determine token type: {}", e.getMessage());
            throw new IllegalArgumentException("Cannot determine token type");
        }
    }
}
