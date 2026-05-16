package com.theblood.springfood.chat.config;

import com.theblood.springfood.chat.domain.UserPresence;
import com.theblood.springfood.chat.repository.UserPresenceRepository;
import com.theblood.springfood.chat.service.TypingIndicatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.time.Instant;

@Component
public class WebSocketEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final TypingIndicatorService typingIndicatorService;
    private final SimpUserRegistry userRegistry;
    private final UserPresenceRepository presenceRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(
        TypingIndicatorService typingIndicatorService,
        SimpUserRegistry userRegistry,
        UserPresenceRepository presenceRepository,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.typingIndicatorService = typingIndicatorService;
        this.userRegistry = userRegistry;
        this.presenceRepository = presenceRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private record PresenceEvent(String userId, String status, Instant timestamp) {}

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Principal user = headerAccessor.getUser();
        String userId = user != null ? user.getName() : "anonymous";

        LoggingMDCUtil.setSessionId(sessionId);
        LoggingMDCUtil.setUserId(userId);

        try {
            LOG.info("WebSocket connection established");

            if (user != null) {
                UserPresence presence = presenceRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        UserPresence p = new UserPresence();
                        p.setUserId(userId);
                        p.setLastSeenAt(Instant.now());
                        p.setDeviceType("WEB");
                        p.setSessionId(sessionId);
                        return p;
                    });
                presence.setStatus("ONLINE");
                presence.setLastSeenAt(Instant.now());
                presence.setSessionId(sessionId);
                presenceRepository.save(presence);

                messagingTemplate.convertAndSend("/topic/presence",
                    new PresenceEvent(userId, "ONLINE", Instant.now()));
            }
        } finally {
            LoggingMDCUtil.clear();
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Principal user = headerAccessor.getUser();
        String userId = user != null ? user.getName() : "anonymous";

        LoggingMDCUtil.setSessionId(sessionId);
        LoggingMDCUtil.setUserId(userId);

        try {
            if (user != null) {
                typingIndicatorService.clearAllTypingForUser(userId);

                presenceRepository.findByUserId(userId).ifPresent(presence -> {
                    if (userRegistry.getUser(userId) == null || userRegistry.getUser(userId).getSessions().isEmpty()) {
                        presence.setStatus("OFFLINE");
                        presence.setLastSeenAt(Instant.now());
                        presenceRepository.save(presence);

                        messagingTemplate.convertAndSend("/topic/presence",
                            new PresenceEvent(userId, "OFFLINE", Instant.now()));
                    }
                });
            }

            LOG.info("WebSocket connection closed");
        } finally {
            LoggingMDCUtil.clear();
        }
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        Principal user = headerAccessor.getUser();
        String userId = user != null ? user.getName() : "anonymous";

        LoggingMDCUtil.setSessionId(sessionId);
        LoggingMDCUtil.setUserId(userId);

        try {
            LOG.info("WebSocket subscription - destination: {}", destination);
        } finally {
            LoggingMDCUtil.clear();
        }
    }
}
