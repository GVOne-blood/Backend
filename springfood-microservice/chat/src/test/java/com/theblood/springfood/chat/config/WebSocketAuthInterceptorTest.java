package com.theblood.springfood.chat.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.theblood.springfood.common.util.JwtUtil;
import com.theblood.springfood.chat.service.ChatMetricsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.security.Principal;

/**
 * Unit tests for {@link WebSocketAuthInterceptor}.
 * <p>
 * Tests JWT authentication for WebSocket connections including:
 * - CONNECT with valid JWT sets user principal
 * - CONNECT with missing Authorization header throws exception
 * - CONNECT with expired JWT throws exception
 * - Non-CONNECT frames pass through without authentication
 * <p>
 * Requirements: 1.2, 1.3, 1.4
 */
@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ChatMetricsService metricsService;

    @Mock
    private MessageChannel messageChannel;

    private WebSocketAuthInterceptor interceptor;

    @BeforeEach
    void setup() {
        interceptor = new WebSocketAuthInterceptor(jwtUtil, metricsService);
    }

    /**
     * Test CONNECT with valid JWT sets user principal
     * <p>
     * Requirement 1.2: Extract JWT from Authorization header during STOMP CONNECT
     * Requirement 1.3: Verify JWT and set authenticated user principal
     */
    @Test
    void shouldSetUserPrincipalWhenConnectWithValidJwt() {
        // Given
        String validToken = "valid.jwt.token";
        String userId = "user123";
        String username = "john.doe";

        // Create mutable accessor
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);  // Keep it mutable
        accessor.addNativeHeader("Authorization", "Bearer " + validToken);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Claims claims = new DefaultClaims();
        claims.setSubject(userId);
        claims.put("preferred_username", username);

        // Mock both validateToken and extractAllClaims to be called in sequence
        doAnswer(invocation -> {
            // validateToken is called first, do nothing (success)
            return null;
        }).when(jwtUtil).validateToken(validToken);

        when(jwtUtil.extractAllClaims(validToken)).thenReturn(claims);

        // When
        Message<?> result = interceptor.preSend(message, messageChannel);

        // Then
        assertThat(result).isNotNull();

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        Principal principal = resultAccessor.getUser();

        assertThat(principal).isNotNull();
        assertThat(principal).isInstanceOf(UsernamePasswordAuthenticationToken.class);

        Authentication auth = (Authentication) principal;
        assertThat(auth.getPrincipal()).isEqualTo(userId);
        assertThat(auth.getCredentials()).isNull();
        assertThat(auth.getAuthorities()).isEmpty();

        verify(jwtUtil).validateToken(validToken);
        verify(jwtUtil).extractAllClaims(validToken);
    }

    /**
     * Test CONNECT with valid JWT but no preferred_username uses userId as fallback
     * <p>
     * Requirement 1.3: Handle JWT claims gracefully
     */
    @Test
    void shouldUseUserIdAsFallbackWhenPreferredUsernameIsMissing() {
        // Given
        String validToken = "valid.jwt.token";
        String userId = "user456";

        // Create mutable accessor
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);  // Keep it mutable
        accessor.addNativeHeader("Authorization", "Bearer " + validToken);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Claims claims = new DefaultClaims();
        claims.setSubject(userId);
        // No preferred_username claim

        // Mock both validateToken and extractAllClaims
        doAnswer(invocation -> {
            // validateToken is called first, do nothing (success)
            return null;
        }).when(jwtUtil).validateToken(validToken);

        when(jwtUtil.extractAllClaims(validToken)).thenReturn(claims);

        // When
        Message<?> result = interceptor.preSend(message, messageChannel);

        // Then
        assertThat(result).isNotNull();

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        Principal principal = resultAccessor.getUser();

        assertThat(principal).isNotNull();
        Authentication auth = (Authentication) principal;
        assertThat(auth.getPrincipal()).isEqualTo(userId);

        verify(jwtUtil).validateToken(validToken);
        verify(jwtUtil).extractAllClaims(validToken);
    }

    /**
     * Test CONNECT with missing Authorization header throws exception
     * <p>
     * Requirement 1.4: Reject connections without valid Authorization header
     */
    @Test
    void shouldThrowExceptionWhenAuthorizationHeaderIsMissing() {
        // Given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        // No Authorization header
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // When & Then
        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Authentication failed: Missing or invalid Authorization header");

        verify(jwtUtil, never()).validateToken(anyString());
        verify(jwtUtil, never()).extractAllClaims(anyString());
    }

    /**
     * Test CONNECT with invalid Authorization header format throws exception
     * <p>
     * Requirement 1.4: Reject connections with invalid Authorization header format
     */
    @Test
    void shouldThrowExceptionWhenAuthorizationHeaderDoesNotStartWithBearer() {
        // Given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Basic invalid.token");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // When & Then
        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Authentication failed: Missing or invalid Authorization header");

        verify(jwtUtil, never()).validateToken(anyString());
        verify(jwtUtil, never()).extractAllClaims(anyString());
    }

    /**
     * Test CONNECT with expired JWT throws exception
     * <p>
     * Requirement 1.4: Reject invalid/expired tokens
     */
    @Test
    void shouldThrowExceptionWhenJwtIsExpired() {
        // Given
        String expiredToken = "expired.jwt.token";

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer " + expiredToken);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        doThrow(new ExpiredJwtException(null, null, "Token expired"))
            .when(jwtUtil).validateToken(expiredToken);

        // When & Then
        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Authentication failed: Invalid or expired token");

        verify(jwtUtil).validateToken(expiredToken);
        verify(jwtUtil, never()).extractAllClaims(anyString());
    }

    /**
     * Test CONNECT with malformed JWT throws exception
     * <p>
     * Requirement 1.4: Reject invalid tokens
     */
    @Test
    void shouldThrowExceptionWhenJwtIsMalformed() {
        // Given
        String malformedToken = "malformed.jwt.token";

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer " + malformedToken);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        doThrow(new MalformedJwtException("Malformed JWT"))
            .when(jwtUtil).validateToken(malformedToken);

        // When & Then
        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Authentication failed: Invalid or expired token");

        verify(jwtUtil).validateToken(malformedToken);
        verify(jwtUtil, never()).extractAllClaims(anyString());
    }

    /**
     * Test CONNECT with invalid signature throws exception
     * <p>
     * Requirement 1.4: Reject tokens with invalid signatures
     */
    @Test
    void shouldThrowExceptionWhenJwtSignatureIsInvalid() {
        // Given
        String invalidToken = "invalid.signature.token";

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer " + invalidToken);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        doThrow(new io.jsonwebtoken.security.SignatureException("Invalid signature"))
            .when(jwtUtil).validateToken(invalidToken);

        // When & Then
        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Authentication failed: Invalid or expired token");

        verify(jwtUtil).validateToken(invalidToken);
        verify(jwtUtil, never()).extractAllClaims(anyString());
    }

    /**
     * Test non-CONNECT frames pass through without authentication
     * <p>
     * Verifies that only CONNECT frames are authenticated, other STOMP commands
     * (SEND, SUBSCRIBE, DISCONNECT, etc.) pass through without JWT validation
     */
    @Test
    void shouldPassThroughNonConnectFramesWithoutAuthentication() {
        // Given - SEND command (not CONNECT)
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination("/app/chat.send");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // When
        Message<?> result = interceptor.preSend(message, messageChannel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(message);

        verify(jwtUtil, never()).validateToken(anyString());
        verify(jwtUtil, never()).extractAllClaims(anyString());
    }

    /**
     * Test SUBSCRIBE command passes through without authentication
     */
    @Test
    void shouldPassThroughSubscribeCommandWithoutAuthentication() {
        // Given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/user/queue/messages");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // When
        Message<?> result = interceptor.preSend(message, messageChannel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(message);

        verify(jwtUtil, never()).validateToken(anyString());
        verify(jwtUtil, never()).extractAllClaims(anyString());
    }

    /**
     * Test DISCONNECT command passes through without authentication
     */
    @Test
    void shouldPassThroughDisconnectCommandWithoutAuthentication() {
        // Given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // When
        Message<?> result = interceptor.preSend(message, messageChannel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(message);

        verify(jwtUtil, never()).validateToken(anyString());
        verify(jwtUtil, never()).extractAllClaims(anyString());
    }

    /**
     * Test message with null accessor passes through
     */
    @Test
    void shouldPassThroughMessageWithNullAccessor() {
        // Given - Message without StompHeaderAccessor
        Message<?> message = MessageBuilder.withPayload(new byte[0]).build();

        // When
        Message<?> result = interceptor.preSend(message, messageChannel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(message);

        verify(jwtUtil, never()).validateToken(anyString());
        verify(jwtUtil, never()).extractAllClaims(anyString());
    }

    /**
     * Test CONNECT with empty Authorization header throws exception
     */
    @Test
    void shouldThrowExceptionWhenAuthorizationHeaderIsEmpty() {
        // Given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // When & Then
        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Authentication failed: Missing or invalid Authorization header");

        verify(jwtUtil, never()).validateToken(anyString());
        verify(jwtUtil, never()).extractAllClaims(anyString());
    }

    /**
     * Test CONNECT with Bearer but no token throws exception
     */
    @Test
    void shouldThrowExceptionWhenBearerTokenIsEmpty() {
        // Given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer ");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // When & Then
        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Authentication failed: Invalid or expired token");

        verify(jwtUtil).validateToken("");
    }
}
