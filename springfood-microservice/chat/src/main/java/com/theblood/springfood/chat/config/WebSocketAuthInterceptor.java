package com.theblood.springfood.chat.config;

import com.theblood.springfood.common.enums.TokenType;
import com.theblood.springfood.common.util.JwtUtil;
import com.theblood.springfood.chat.service.ChatMetricsService;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * WebSocket Authentication Interceptor
 * <p>
 * Intercepts STOMP CONNECT frames to extract and verify JWT tokens.
 * Sets authenticated user principal for the WebSocket session.
 * <p>
 * Requirements: 1.2, 1.3, 1.4
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final JwtUtil jwtUtil;
    private final ChatMetricsService metricsService;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil, @Lazy ChatMetricsService metricsService) {
        this.jwtUtil = jwtUtil;
        this.metricsService = metricsService;
    }

    /**
     * Intercept STOMP frames to authenticate WebSocket connections and validate tokens
     * <p>
     * Requirement 1.2: Extract JWT from Authorization header during STOMP CONNECT
     * Requirement 1.3: Verify JWT and set authenticated user principal
     * Requirement 1.4: Reject connection for invalid/expired tokens
     * Requirement 1.5: Validate token on SEND/SUBSCRIBE to detect expiration
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        
        // Process CONNECT frames
        if (StompCommand.CONNECT.equals(command)) {
            String sessionId = accessor.getSessionId();
            LoggingMDCUtil.setSessionId(sessionId);

            try {
                // Log để debug
                log.info("WebSocket CONNECT headers: {}", accessor.toNativeHeaderMap());
                
                String authHeader = accessor.getFirstNativeHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);

                    try {
                        // Requirement 1.3: Verify JWT signature and expiration
                        jwtUtil.validateToken(token, TokenType.ACCESS);
                        Claims claims = jwtUtil.extractAllClaims(token, TokenType.ACCESS);

                        String userId = claims.getSubject();
                        String username = claims.get("username", String.class);

                        if (username == null) {
                            username = userId;
                        }

                        LoggingMDCUtil.setUserId(userId);

                        // Requirement 1.3: Set authenticated user principal
                        Authentication auth = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.emptyList()
                        );

                        accessor.setUser(auth);
                        
                        // Store token in session attributes for later validation
                        accessor.getSessionAttributes().put("jwt_token", token);

                        log.info("WebSocket connection authenticated - username: {}", username);

                    } catch (Exception e) {
                        log.error("WebSocket authentication failed - reason: {} - exception: {}", e.getMessage(), e.getClass().getName(), e);
                        metricsService.incrementAuthFailures();
                        throw new IllegalArgumentException("Authentication failed: " + e.getMessage());
                    }
                } else {
                    log.error("WebSocket connection attempt without valid Authorization header");
                    metricsService.incrementAuthFailures();
                    throw new IllegalArgumentException("Authentication failed: Missing or invalid Authorization header");
                }
            } finally {
                LoggingMDCUtil.clear();
            }
        }
        
        // Validate token on SEND and SUBSCRIBE commands
        else if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
            String sessionId = accessor.getSessionId();
            LoggingMDCUtil.setSessionId(sessionId);
            
            try {
                // Get token from session attributes (stored during CONNECT)
                String token = (String) accessor.getSessionAttributes().get("jwt_token");
                
                if (token != null) {
                    try {
                        // Check if token is expired
                        jwtUtil.validateToken(token, TokenType.ACCESS);
                    } catch (Exception e) {
                        // Token expired - send error message to client
                        log.warn("Token expired during {} command - sessionId: {}", command, sessionId);
                        throw new IllegalArgumentException("TOKEN_EXPIRED: Your session has expired. Please refresh your token and reconnect.");
                    }
                }
            } finally {
                LoggingMDCUtil.clear();
            }
        }

        return message;
    }
}
