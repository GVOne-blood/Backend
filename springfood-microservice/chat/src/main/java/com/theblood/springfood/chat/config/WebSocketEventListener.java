package com.theblood.springfood.chat.config;

import com.theblood.springfood.chat.service.TypingIndicatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;

/**
 * WebSocket event listener for handling session lifecycle events.
 * Logs connection/disconnection events and cleans up typing indicators on disconnect.
 * 
 * Requirements: 1.6, 9.6
 */
@Component
public class WebSocketEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final TypingIndicatorService typingIndicatorService;
    private final SimpUserRegistry userRegistry;

    public WebSocketEventListener(
        TypingIndicatorService typingIndicatorService,
        SimpUserRegistry userRegistry
    ) {
        this.typingIndicatorService = typingIndicatorService;
        this.userRegistry = userRegistry;
    }

    /**
     * Handle WebSocket connection events.
     * Logs the connection with userId and sessionId.
     * 
     * @param event The session connect event
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        Principal user = headerAccessor.getUser();
        String userId = user != null ? user.getName() : "anonymous";
        
        // Set MDC context for structured logging
        LoggingMDCUtil.setSessionId(sessionId);
        LoggingMDCUtil.setUserId(userId);
        
        try {
            // INFO level: WebSocket connection events with sessionId and userId
            LOG.info("WebSocket connection established");
        } finally {
            LoggingMDCUtil.clear();
        }
    }

    /**
     * Handle WebSocket disconnection events.
     * Cleans up typing indicators for the disconnected user and logs the disconnect.
     * SimpUserRegistry automatically removes disconnected sessions.
     * 
     * @param event The session disconnect event
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        Principal user = headerAccessor.getUser();
        String userId = user != null ? user.getName() : "anonymous";
        
        // Set MDC context for structured logging
        LoggingMDCUtil.setSessionId(sessionId);
        LoggingMDCUtil.setUserId(userId);
        
        try {
            // Clear typing indicators for disconnected user
            if (user != null) {
                typingIndicatorService.clearAllTypingForUser(userId);
                LOG.debug("Cleared typing indicators for disconnected user");
            }
            
            // INFO level: WebSocket connection events with sessionId and userId
            LOG.info("WebSocket connection closed");
            
            // Verify session removed from registry (automatic by Spring)
            // This is just for logging/verification purposes
            if (userRegistry.getUser(userId) == null) {
                LOG.debug("User session removed from SimpUserRegistry");
            }
        } finally {
            LoggingMDCUtil.clear();
        }
    }

    /**
     * Handle WebSocket subscription events.
     * Logs when users subscribe to specific destinations.
     * 
     * @param event The session subscribe event
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        Principal user = headerAccessor.getUser();
        String userId = user != null ? user.getName() : "anonymous";
        
        // Set MDC context for structured logging
        LoggingMDCUtil.setSessionId(sessionId);
        LoggingMDCUtil.setUserId(userId);
        
        try {
            // INFO level: WebSocket subscription events
            LOG.info("WebSocket subscription - destination: {}", destination);
        } finally {
            LoggingMDCUtil.clear();
        }
    }
}
