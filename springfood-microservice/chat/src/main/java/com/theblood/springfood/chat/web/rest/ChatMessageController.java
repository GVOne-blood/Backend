package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.config.LoggingMDCUtil;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.repository.MessageRepository;
import com.theblood.springfood.chat.service.ChatMetricsService;
import com.theblood.springfood.chat.service.ConversationService;
import com.theblood.springfood.chat.service.TypingIndicatorService;
import com.theblood.springfood.chat.service.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

/**
 * WebSocket Controller for handling real-time chat messages.
 * Handles message sending, typing indicators, and read receipts via STOMP protocol.
 * <p>
 * Architecture:
 * - Receives messages from WebSocket clients via @MessageMapping
 * - Validates user is participant before processing
 * - Publishes messages to Kafka for distribution and persistence
 * - Uses conversationId as Kafka partition key for ordering guarantees
 * <p>
 * Requirements: 3.1, 3.2, 3.4, 7.1, 9.1
 */
@RestController
@RequiredArgsConstructor
public class ChatMessageController {

    private static final Logger LOG = LoggerFactory.getLogger(ChatMessageController.class);

    private final KafkaTemplate<String, ChatMessageEvent> chatMessageKafkaTemplate;
    private final KafkaTemplate<String, ReadReceiptEvent> readReceiptKafkaTemplate;
    private final ConversationService conversationService;
    private final TypingIndicatorService typingIndicatorService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final ChatMetricsService metricsService;

    /**
     * Handle incoming chat messages from WebSocket clients.
     * <p>
     * Flow:
     * 1. Validate user is participant of the conversation
     * 2. Generate unique messageId and preserve clientMessageId
     * 3. Validate replyToMessageId if present (exists and in same conversation)
     * 4. Cache reply_to_preview (first 200 chars of referenced message)
     * 5. Create ChatMessageEvent with complete message data
     * 6. Publish to Kafka topic "chat-messages" with conversationId as partition key
     * 7. Clear typing indicator for sender
     * <p>
     * Requirements:
     * - 3.1: Validate user is participant before processing
     * - 3.2: Generate messageId and preserve clientMessageId
     * - 3.4: Publish to Kafka with conversationId as partition key
     * - 9.6: Clear typing indicator when message sent
     * - 10.1: Validate referenced message is in same conversation
     * - 10.2: Store replyToMessageId in message record
     * - 10.3: Cache reply_to_preview (first 200 characters)
     * - 10.5: Reject messages with invalid replyToMessageId
     *
     * @param request   The message request containing conversationId, content, etc.
     * @param principal The authenticated user principal (from JWT)
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Valid @Payload SendMessageRequest request, Principal principal) {
        String userId = principal.getName();
        String conversationId = request.getConversationId();

        // Set MDC context for structured logging
        LoggingMDCUtil.setConversationId(conversationId);
        LoggingMDCUtil.setUserId(userId);
        if (request.getClientMessageId() != null) {
            LoggingMDCUtil.setClientMessageId(request.getClientMessageId());
        }

        try {
            // DEBUG level: Message flow milestone - received
            LOG.debug("Message received from WebSocket client");

            // Requirement 3.1: Validate user is participant
            if (!conversationService.isUserParticipant(conversationId, userId)) {
                // WARN level: Authorization failures with userId and conversationId
                LOG.warn("Authorization failed - user is not a participant of conversation");
                sendErrorToUser(userId, "FORBIDDEN", "You are not a participant of this conversation");
                return;
            }

            // Requirement 3.2: Generate unique messageId and preserve clientMessageId
            String messageId = UUID.randomUUID().toString();
            String clientMessageId = request.getClientMessageId() != null
                ? request.getClientMessageId()
                : UUID.randomUUID().toString();

            // Update MDC with generated messageId
            LoggingMDCUtil.setMessageId(messageId);

            // Requirement 10.1, 10.2, 10.3, 10.5: Validate and cache reply message preview
            String replyToPreview = null;
            if (request.getReplyToMessageId() != null && !request.getReplyToMessageId().isEmpty()) {
                Message replyToMessage = messageRepository.findById(request.getReplyToMessageId())
                    .orElse(null);

                // Requirement 10.5: Reject if referenced message doesn't exist
                if (replyToMessage == null) {
                    LOG.warn("Validation failed - reply to non-existent message: {}",
                        request.getReplyToMessageId());
                    sendErrorToUser(userId, "VALIDATION_ERROR",
                        "Referenced message does not exist");
                    return;
                }

                // Requirement 10.1: Validate referenced message is in the same conversation
                if (!conversationId.equals(replyToMessage.getConversation().getConversationId())) {
                    LOG.warn("Validation failed - reply to message in different conversation: {}",
                        request.getReplyToMessageId());
                    sendErrorToUser(userId, "VALIDATION_ERROR",
                        "Cannot reply to message from a different conversation");
                    return;
                }

                // Requirement 10.3: Cache reply_to_preview (first 200 characters)
                if (replyToMessage.getContent() != null) {
                    replyToPreview = replyToMessage.getContent().length() > 200
                        ? replyToMessage.getContent().substring(0, 200)
                        : replyToMessage.getContent();
                }
            }

            // Create ChatMessageEvent with complete message data
            ChatMessageEvent event = new ChatMessageEvent();
            event.setMessageId(messageId);
            event.setClientMessageId(clientMessageId);
            event.setConversationId(conversationId);
            event.setSenderId(userId);
            // Note: senderName and senderAvatar should be fetched from user service or passed from client
            // For now, we'll leave them null and they can be enriched later
            event.setSenderName(null); // TODO: Fetch from user service
            event.setSenderAvatar(null); // TODO: Fetch from user service
            event.setSenderType("USER");
            event.setMessageType(request.getMessageType() != null ? request.getMessageType() : "TEXT");
            event.setContent(request.getContent());
            event.setMetadata(request.getMetadata());
            // Requirement 10.2, 10.4: Include replyToMessageId and reply_to_preview in event
            event.setReplyToMessageId(request.getReplyToMessageId());
            event.setReplyToPreview(replyToPreview);
            event.setReferenceType(request.getReferenceType());
            event.setReferenceId(request.getReferenceId());
            event.setStatus("SENDING");
            event.setCreatedAt(Instant.now());

            // Requirement 3.4: Publish to Kafka with conversationId as partition key
            // This ensures all messages in the same conversation go to the same partition
            // for ordering guarantees (Requirement 12.1)

            // DEBUG level: Message flow milestone - publishing to Kafka
            LOG.debug("Publishing message to Kafka topic: chat-messages");

            chatMessageKafkaTemplate.send("chat-messages", conversationId, event)
                .whenComplete((result, ex) -> {
                    LoggingMDCUtil.setMessageContext(conversationId, messageId, userId);
                    try {
                        if (ex != null) {
                            // ERROR level: Kafka producer errors with messageId and conversationId
                            LOG.error("Kafka producer error - failed to publish message", ex);
                            metricsService.incrementKafkaErrors();
                            sendErrorToUser(userId, "MESSAGE_SEND_FAILED",
                                "Failed to send message: " + ex.getMessage());
                        } else {
                            // DEBUG level: Message flow milestone - published
                            LOG.debug("Message published to Kafka successfully");
                            // Increment messages sent counter
                            metricsService.incrementMessagesSent();
                        }
                    } finally {
                        LoggingMDCUtil.clear();
                    }
                });

            // Requirement 9.6: Clear typing indicator when message sent
            typingIndicatorService.stopTyping(conversationId, userId);

        } catch (IllegalArgumentException e) {
            LOG.warn("Validation error: {}", e.getMessage());
            sendErrorToUser(userId, "VALIDATION_ERROR", e.getMessage());
        } catch (Exception e) {
            LOG.error("Unexpected error processing message", e);
            sendErrorToUser(userId, "INTERNAL_ERROR", "An unexpected error occurred");
        } finally {
            LoggingMDCUtil.clear();
        }
    }

    /**
     * Handle typing indicator events from WebSocket clients.
     * <p>
     * Flow:
     * 1. Validate user is participant of the conversation
     * 2. Start or stop typing indicator based on isTyping flag
     * 3. Update Redis with typing state (TTL 5 seconds)
     * 4. Broadcast typing indicators to all participants
     * <p>
     * Requirements:
     * - 9.1: Handle typing events from clients
     * - 9.2: Validate user is participant
     * - 9.3: Update Redis with TTL
     *
     * @param request   The typing request containing conversationId and isTyping flag
     * @param principal The authenticated user principal (from JWT)
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Valid @Payload TypingRequest request, Principal principal) {
        String userId = principal.getName();
        String conversationId = request.getConversationId();

        // Set MDC context
        LoggingMDCUtil.setConversationId(conversationId);
        LoggingMDCUtil.setUserId(userId);

        try {
            LOG.debug("Typing indicator received - isTyping: {}", request.getIsTyping());

            // Requirement 9.2: Validate user is participant
            if (!conversationService.isUserParticipant(conversationId, userId)) {
                // WARN level: Authorization failures
                LOG.warn("Authorization failed - user is not a participant of conversation");
                sendErrorToUser(userId, "FORBIDDEN", "You are not a participant of this conversation");
                return;
            }

            // Start or stop typing based on flag
            if (Boolean.TRUE.equals(request.getIsTyping())) {
                // Requirement 9.3: Add to Redis with TTL 5 seconds
                typingIndicatorService.startTyping(conversationId, userId, null);
            } else {
                typingIndicatorService.stopTyping(conversationId, userId);
            }

        } catch (IllegalArgumentException e) {
            LOG.warn("Validation error for typing indicator: {}", e.getMessage());
            sendErrorToUser(userId, "VALIDATION_ERROR", e.getMessage());
        } catch (Exception e) {
            LOG.error("Unexpected error processing typing indicator", e);
            // Don't send error to user - typing indicators are non-critical
        } finally {
            LoggingMDCUtil.clear();
        }
    }

    /**
     * Handle read receipt events from WebSocket clients.
     * <p>
     * Flow:
     * 1. Validate user is participant of the conversation
     * 2. Create ReadReceiptEvent
     * 3. Publish to Kafka topic "chat-read-receipts"
     * 4. Kafka consumer will update database and broadcast to participants
     * <p>
     * Requirements:
     * - 7.1: Handle read receipt events from clients
     * - 7.2: Validate user is participant
     * - 7.3: Publish to Kafka for processing
     *
     * @param request   The read receipt request containing conversationId and lastReadMessageId
     * @param principal The authenticated user principal (from JWT)
     */
    @MessageMapping("/chat.read")
    public void markAsRead(@Valid @Payload ReadReceiptRequest request, Principal principal) {
        String userId = principal.getName();
        String conversationId = request.getConversationId();

        // Set MDC context
        LoggingMDCUtil.setConversationId(conversationId);
        LoggingMDCUtil.setUserId(userId);
        LoggingMDCUtil.setMessageId(request.getLastReadMessageId());

        try {
            LOG.debug("Read receipt received - lastReadMessageId: {}", request.getLastReadMessageId());

            // Requirement 7.2: Validate user is participant
            if (!conversationService.isUserParticipant(conversationId, userId)) {
                // WARN level: Authorization failures
                LOG.warn("Authorization failed - user is not a participant of conversation");
                sendErrorToUser(userId, "FORBIDDEN", "You are not a participant of this conversation");
                return;
            }

            // Create ReadReceiptEvent
            ReadReceiptEvent event = new ReadReceiptEvent();
            event.setConversationId(conversationId);
            event.setUserId(userId);
            event.setLastReadMessageId(request.getLastReadMessageId());
            event.setReadAt(Instant.now());

            // Requirement 7.3: Publish to Kafka topic "chat-read-receipts"
            readReceiptKafkaTemplate.send("chat-read-receipts", conversationId, event)
                .whenComplete((result, ex) -> {
                    LoggingMDCUtil.setConversationId(conversationId);
                    LoggingMDCUtil.setUserId(userId);
                    try {
                        if (ex != null) {
                            // ERROR level: Kafka producer errors
                            LOG.error("Kafka producer error - failed to publish read receipt", ex);
                            metricsService.incrementKafkaErrors();
                            sendErrorToUser(userId, "READ_RECEIPT_FAILED",
                                "Failed to process read receipt: " + ex.getMessage());
                        } else {
                            LOG.debug("Read receipt published to Kafka successfully");
                        }
                    } finally {
                        LoggingMDCUtil.clear();
                    }
                });

        } catch (IllegalArgumentException e) {
            LOG.warn("Validation error for read receipt: {}", e.getMessage());
            sendErrorToUser(userId, "VALIDATION_ERROR", e.getMessage());
        } catch (Exception e) {
            LOG.error("Unexpected error processing read receipt", e);
            sendErrorToUser(userId, "INTERNAL_ERROR", "An unexpected error occurred");
        } finally {
            LoggingMDCUtil.clear();
        }
    }

    /**
     * Send error message to a specific user via their personal queue.
     *
     * @param userId    The user ID to send the error to
     * @param errorCode The error code (e.g., "FORBIDDEN", "VALIDATION_ERROR")
     * @param message   The error message
     */
    private void sendErrorToUser(String userId, String errorCode, String message) {
        try {
            var errorPayload = new ErrorResponse(errorCode, message, Instant.now());
            messagingTemplate.convertAndSendToUser(userId, "/queue/errors", errorPayload);
            LOG.debug("Sent error to user {}: {} - {}", userId, errorCode, message);
        } catch (Exception e) {
            LOG.error("Failed to send error message to user {}", userId, e);
        }
    }

    /**
     * Simple error response DTO for WebSocket errors.
     */
    private record ErrorResponse(String error, String message, Instant timestamp) {
    }
}
