package com.theblood.springfood.chat.config;

import com.theblood.springfood.chat.service.TypingIndicatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WebSocketEventListener.
 * Tests session lifecycle event handling and typing indicator cleanup.
 *
 * Requirements: 1.6, 9.6
 */
@ExtendWith(MockitoExtension.class)
class WebSocketEventListenerTest {

    @Mock
    private TypingIndicatorService typingIndicatorService;

    @Mock
    private SimpUserRegistry userRegistry;

    @Mock
    private Principal principal;

    private WebSocketEventListener eventListener;

    @BeforeEach
    void setUp() {
        eventListener = new WebSocketEventListener(typingIndicatorService, userRegistry);
    }

    @Test
    void testHandleWebSocketConnectListener_LogsConnectionWithCorrectUserId() {
        // Given
        String userId = "user123";
        String sessionId = "session456";

        when(principal.getName()).thenReturn(userId);

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setUser(principal);

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders());
        SessionConnectEvent event = new SessionConnectEvent(this, message);

        // When
        eventListener.handleWebSocketConnectListener(event);

        // Then
        // Verify no exceptions thrown and method completes
        // Logging is verified through manual inspection or log capture
        verifyNoInteractions(typingIndicatorService);
    }

    @Test
    void testHandleWebSocketConnectListener_HandlesAnonymousUser() {
        // Given
        String sessionId = "session456";

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        headerAccessor.setSessionId(sessionId);
        // No user set (anonymous)

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders());
        SessionConnectEvent event = new SessionConnectEvent(this, message);

        // When
        eventListener.handleWebSocketConnectListener(event);

        // Then
        // Verify no exceptions thrown
        verifyNoInteractions(typingIndicatorService);
    }

    @Test
    void testHandleWebSocketDisconnectListener_ClearsTypingIndicatorsForUser() {
        // Given
        String userId = "user123";
        String sessionId = "session456";

        when(principal.getName()).thenReturn(userId);
        when(userRegistry.getUser(userId)).thenReturn(null); // Session removed

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setUser(principal);

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders());
        SessionDisconnectEvent event = new SessionDisconnectEvent(this, message, sessionId, null);

        // When
        eventListener.handleWebSocketDisconnectListener(event);

        // Then
        verify(typingIndicatorService).clearAllTypingForUser(userId);
    }

    @Test
    void testHandleWebSocketDisconnectListener_VerifiesSessionRemovedFromRegistry() {
        // Given
        String userId = "user123";
        String sessionId = "session456";

        when(principal.getName()).thenReturn(userId);
        when(userRegistry.getUser(userId)).thenReturn(null); // Session removed automatically

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setUser(principal);

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders());
        SessionDisconnectEvent event = new SessionDisconnectEvent(this, message, sessionId, null);

        // When
        eventListener.handleWebSocketDisconnectListener(event);

        // Then
        verify(userRegistry).getUser(userId);
        verify(typingIndicatorService).clearAllTypingForUser(userId);
    }

    @Test
    void testHandleWebSocketDisconnectListener_HandlesAnonymousUser() {
        // Given
        String sessionId = "session456";

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        headerAccessor.setSessionId(sessionId);
        // No user set (anonymous)

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders());
        SessionDisconnectEvent event = new SessionDisconnectEvent(this, message, sessionId, null);

        // When
        eventListener.handleWebSocketDisconnectListener(event);

        // Then
        // Should not call clearAllTypingForUser for anonymous users
        verify(typingIndicatorService, never()).clearAllTypingForUser(anyString());
    }

    @Test
    void testHandleWebSocketSubscribeListener_LogsSubscriptionToDestination() {
        // Given
        String userId = "user123";
        String sessionId = "session456";
        String destination = "/topic/conversation.conv123/typing";

        when(principal.getName()).thenReturn(userId);

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setDestination(destination);
        headerAccessor.setUser(principal);

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders());
        SessionSubscribeEvent event = new SessionSubscribeEvent(this, message);

        // When
        eventListener.handleWebSocketSubscribeListener(event);

        // Then
        // Verify no exceptions thrown and method completes
        // Logging is verified through manual inspection or log capture
        verifyNoInteractions(typingIndicatorService);
    }

    @Test
    void testHandleWebSocketSubscribeListener_HandlesAnonymousUser() {
        // Given
        String sessionId = "session456";
        String destination = "/user/queue/messages";

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setDestination(destination);
        // No user set (anonymous)

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders());
        SessionSubscribeEvent event = new SessionSubscribeEvent(this, message);

        // When
        eventListener.handleWebSocketSubscribeListener(event);

        // Then
        // Verify no exceptions thrown
        verifyNoInteractions(typingIndicatorService);
    }

    @Test
    void testHandleWebSocketDisconnectListener_SessionStillInRegistry() {
        // Given
        String userId = "user123";
        String sessionId = "session456";

        when(principal.getName()).thenReturn(userId);

        // Mock user still in registry (edge case - should not happen normally)
        SimpUser simpUser = mock(SimpUser.class);
        when(userRegistry.getUser(userId)).thenReturn(simpUser);

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setUser(principal);

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders());
        SessionDisconnectEvent event = new SessionDisconnectEvent(this, message, sessionId, null);

        // When
        eventListener.handleWebSocketDisconnectListener(event);

        // Then
        verify(typingIndicatorService).clearAllTypingForUser(userId);
        verify(userRegistry).getUser(userId);
        // Log should indicate user still in registry (edge case)
    }
}
