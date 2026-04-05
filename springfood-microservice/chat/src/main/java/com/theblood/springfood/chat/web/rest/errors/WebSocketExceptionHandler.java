package com.theblood.springfood.chat.web.rest.errors;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

/**
 * WebSocket exception handler for STOMP message exceptions.
 * Handles exceptions thrown during @MessageMapping processing and sends
 * structured error responses to the client via /user/queue/errors.
 */
@Controller
public class WebSocketExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketExceptionHandler.class);

    /**
     * Handle AuthenticationException in WebSocket context.
     * Sends ERROR frame to client.
     */
    @MessageExceptionHandler(AuthenticationException.class)
    @SendToUser("/queue/errors")
    public Map<String, Object> handleAuthenticationException(AuthenticationException ex) {
        LOG.error("WebSocket authentication failed: {}", ex.getMessage());
        return createErrorResponse("AUTHENTICATION_FAILED", ex.getMessage(), 401);
    }

    /**
     * Handle AuthorizationException in WebSocket context.
     * Sends ERROR frame to client.
     */
    @MessageExceptionHandler(AuthorizationException.class)
    @SendToUser("/queue/errors")
    public Map<String, Object> handleAuthorizationException(AuthorizationException ex) {
        LOG.warn("WebSocket authorization failed: {} for conversation: {}", ex.getMessage(), ex.getConversationId());
        
        Map<String, Object> error = createErrorResponse("AUTHORIZATION_FAILED", ex.getMessage(), 403);
        if (ex.getConversationId() != null) {
            error.put("conversationId", ex.getConversationId());
        }
        return error;
    }

    /**
     * Handle ValidationException in WebSocket context.
     * Sends ERROR frame to client.
     */
    @MessageExceptionHandler(ValidationException.class)
    @SendToUser("/queue/errors")
    public Map<String, Object> handleValidationException(ValidationException ex) {
        LOG.warn("WebSocket validation failed: {} for field: {}", ex.getMessage(), ex.getField());
        
        Map<String, Object> error = createErrorResponse("VALIDATION_FAILED", ex.getMessage(), 400);
        if (ex.getField() != null) {
            error.put("field", ex.getField());
        }
        if (ex.getRejectedValue() != null) {
            error.put("rejectedValue", ex.getRejectedValue());
        }
        return error;
    }

    /**
     * Handle Kafka exceptions in WebSocket context.
     * Sends ERROR frame to client indicating message send failure.
     */
    @MessageExceptionHandler(org.springframework.kafka.KafkaException.class)
    @SendToUser("/queue/errors")
    public Map<String, Object> handleKafkaException(org.springframework.kafka.KafkaException ex) {
        LOG.error("WebSocket Kafka error: {}", ex.getMessage(), ex);
        return createErrorResponse(
            "MESSAGE_SEND_FAILED",
            "Failed to send message after retries. Please try again.",
            500
        );
    }

    /**
     * Handle generic exceptions in WebSocket context.
     * Sends ERROR frame to client.
     */
    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public Map<String, Object> handleGenericException(Exception ex) {
        LOG.error("WebSocket error: {}", ex.getMessage(), ex);
        return createErrorResponse("INTERNAL_ERROR", "An unexpected error occurred. Please try again.", 500);
    }

    /**
     * Create structured error response for WebSocket clients.
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message, int status) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", errorCode);
        error.put("message", message);
        error.put("status", status);
        error.put("timestamp", Instant.now().toString());
        return error;
    }
}
