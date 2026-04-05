package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.service.ai.AIAssistantService;
import com.theblood.springfood.chat.service.dto.AIMessageRequest;
import com.theblood.springfood.chat.service.dto.AIMessageResponse;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.request.UserContextHolder;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.util.UUID;

/**
 * AI Assistant Controller
 * 
 * Provides both REST and WebSocket endpoints for AI chat functionality.
 * Supports synchronous responses and streaming for real-time experience.
 * 
 * Note: REST endpoints don't require authentication as they go through API Gateway
 * which already extracts user info into UserContextHolder.
 * WebSocket endpoints still require authentication due to WebSocket protocol nature.
 */
@Controller
@RequestMapping("/api/ai-assistant")
public class AIAssistantController {

    private static final Logger log = LoggerFactory.getLogger(AIAssistantController.class);

    private final AIAssistantService aiAssistantService;
    private final SimpMessagingTemplate messagingTemplate;

    public AIAssistantController(
        AIAssistantService aiAssistantService,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.aiAssistantService = aiAssistantService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * REST endpoint for AI chat (synchronous)
     * POST /api/ai-assistant/chat
     * 
     * User info is extracted from UserContextHolder (set by API Gateway)
     * Each user has ONE persistent conversation with AI (conversationId = "ai-" + userId)
     */
    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<AIMessageResponse> chat(@Valid @RequestBody AIMessageRequest request) {
        CustomUserPrincipal userPrincipal = UserContextHolder.getContext();
        String userId = userPrincipal.getUserIdString();
        // Use userId as conversationId for persistent history per user
        String conversationId = "ai-" + userId;

        log.debug("REST AI chat - user: {}, conversation: {}", userId, conversationId);

        String response = aiAssistantService.chat(conversationId, userId, request.getMessage());
        
        return ResponseEntity.ok(
            AIMessageResponse.of(conversationId, request.getMessage(), response)
        );
    }

    /**
     * REST endpoint for AI chat streaming (Server-Sent Events)
     * GET /api/ai-assistant/chat/stream
     * 
     * User info is extracted from UserContextHolder (set by API Gateway)
     * Each user has ONE persistent conversation with AI (conversationId = "ai-" + userId)
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> chatStream(@RequestParam String message) {
        CustomUserPrincipal userPrincipal = UserContextHolder.getContext();
        String userId = userPrincipal.getUserIdString();
        // Use userId as conversationId for persistent history per user
        String conversationId = "ai-" + userId;

        log.debug("REST AI stream - user: {}, conversation: {}", userId, conversationId);

        return aiAssistantService.chatStream(conversationId, userId, message);
    }

    /**
     * WebSocket endpoint for AI chat
     * /app/ai-assistant/chat -> /user/queue/ai-assistant/response
     * 
     * Note: WebSocket requires authentication via JWT token in handshake
     * Each user has ONE persistent conversation with AI (conversationId = "ai-" + userId)
     */
    @MessageMapping("/ai-assistant/chat")
    public void handleAIChat(
        @Payload @Valid AIMessageRequest request,
        SimpMessageHeaderAccessor headerAccessor,
        Principal principal
    ) {
        String userId = principal.getName();
        String sessionId = headerAccessor.getSessionId();
        // Use userId as conversationId for persistent history per user
        String conversationId = "ai-" + userId;

        log.debug("WebSocket AI chat - user: {}, session: {}, conversation: {}", 
            userId, sessionId, conversationId);

        try {
            // Stream response to user via WebSocket
            aiAssistantService.chatStream(conversationId, userId, request.getMessage())
                .doOnNext(chunk -> {
                    // Send each chunk to user's queue
                    messagingTemplate.convertAndSendToUser(
                        userId,
                        "/queue/ai-assistant/response",
                        chunk
                    );
                })
                .doOnComplete(() -> {
                    // Send completion signal
                    messagingTemplate.convertAndSendToUser(
                        userId,
                        "/queue/ai-assistant/complete",
                        conversationId
                    );
                })
                .subscribe();

        } catch (Exception e) {
            log.error("Error handling AI chat via WebSocket", e);
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/ai-assistant/error",
                "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại."
            );
        }
    }

    /**
     * Clear AI conversation history
     * DELETE /api/ai-assistant/history/{conversationId}
     */
    @DeleteMapping("/history/{conversationId}")
    @ResponseBody
    public ResponseEntity<Void> clearHistory(@PathVariable String conversationId) {
        CustomUserPrincipal userPrincipal = UserContextHolder.getContext();
        log.debug("Clearing AI history - conversation: {}, user: {}", 
            conversationId, userPrincipal.getUserIdString());
        
        aiAssistantService.clearHistory(conversationId);
        return ResponseEntity.noContent().build();
    }
}
