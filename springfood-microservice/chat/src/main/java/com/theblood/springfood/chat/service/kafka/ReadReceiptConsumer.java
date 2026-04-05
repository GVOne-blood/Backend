package com.theblood.springfood.chat.service.kafka;

import com.theblood.springfood.chat.config.LoggingMDCUtil;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.domain.MessageReadReceipt;
import com.theblood.springfood.chat.domain.enumeration.ParticipantStatus;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.repository.MessageReadReceiptRepository;
import com.theblood.springfood.chat.repository.MessageRepository;
import com.theblood.springfood.chat.service.ChatMetricsService;
import com.theblood.springfood.chat.service.dto.ReadReceiptEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Kafka consumer for processing read receipts.
 * 
 * Responsibilities:
 * - Update conversation_participant.last_read_message_id and last_read_at
 * - Reset conversation_participant.unread_count to 0
 * - Insert/update message_read_receipt records
 * - Clear Redis cache "unread:{userId}:{conversationId}"
 * - Broadcast read status to all participants via /topic/conversation.{conversationId}
 * 
 * Message Flow:
 * 1. Consume ReadReceiptEvent from Kafka topic "chat-read-receipts"
 * 2. Update participant's read status in database
 * 3. Insert/update message read receipt record
 * 4. Clear Redis cache for unread count
 * 5. Broadcast read status to all conversation participants
 * 6. Manually commit Kafka offset after successful processing
 * 
 * Requirements: 7.3, 7.4, 7.5, 7.6
 */
@Component
public class ReadReceiptConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReadReceiptConsumer.class);

    private final ConversationParticipantRepository participantRepository;
    private final MessageReadReceiptRepository readReceiptRepository;
    private final MessageRepository messageRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMetricsService metricsService;

    public ReadReceiptConsumer(
            ConversationParticipantRepository participantRepository,
            MessageReadReceiptRepository readReceiptRepository,
            MessageRepository messageRepository,
            RedisTemplate<String, String> redisTemplate,
            SimpMessagingTemplate messagingTemplate,
            ChatMetricsService metricsService) {
        this.participantRepository = participantRepository;
        this.readReceiptRepository = readReceiptRepository;
        this.messageRepository = messageRepository;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.metricsService = metricsService;
        
        log.info("ReadReceiptConsumer initialized");
    }

    /**
     * Consume read receipt events from Kafka and process them.
     * 
     * Kafka Configuration:
     * - Topic: "chat-read-receipts"
     * - Group ID: "chat-read-receipt-group" (shared across instances)
     * - Container Factory: kafkaReadReceiptListenerContainerFactory (single message mode)
     * - Acknowledgment: Manual (after successful processing)
     * 
     * Processing Steps:
     * 1. Receive ReadReceiptEvent from Kafka
     * 2. Update conversation_participant (last_read_message_id, last_read_at, unread_count=0)
     * 3. Insert/update message_read_receipt record
     * 4. Clear Redis cache for unread count
     * 5. Broadcast read status to all ACTIVE participants
     * 6. Acknowledge Kafka offset
     * 
     * Requirements:
     * - 7.3: Publish read receipt to Kafka
     * - 7.4: Insert/update message_read_receipt record
     * - 7.5: Update participant last_read_message_id and reset unread_count
     * - 7.6: Broadcast read status to all participants
     * 
     * @param event The read receipt event from Kafka
     * @param acknowledgment Kafka acknowledgment for manual commit
     */
    @KafkaListener(
        topics = "chat-read-receipts",
        groupId = "chat-read-receipt-group",
        containerFactory = "kafkaReadReceiptListenerContainerFactory"
    )
    @Transactional
    public void consumeReadReceipt(ReadReceiptEvent event, Acknowledgment acknowledgment) {
        // Set MDC context for structured logging
        LoggingMDCUtil.setMessageContext(
            event.getConversationId(),
            event.getLastReadMessageId(),
            event.getUserId()
        );
        
        try {
            log.debug("Read receipt received from Kafka for processing");

            // Requirement 7.5: Update conversation_participant
            updateParticipantReadStatus(event);

            // Requirement 7.4: Insert/update message_read_receipt record
            insertOrUpdateReadReceipt(event);

            // Clear Redis cache for unread count
            clearUnreadCache(event.getUserId(), event.getConversationId());

            // Requirement 7.6: Broadcast read status to all participants
            broadcastReadStatus(event);

            log.info("Read receipt processed successfully");

            // Manually commit Kafka offset after successful processing
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.trace("Kafka offset acknowledged");
            }

        } catch (Exception e) {
            // ERROR level: Kafka consumer errors
            log.error("Kafka consumer error - failed to process read receipt", e);
            
            // Don't acknowledge on error - message will be redelivered
            throw e;
        } finally {
            LoggingMDCUtil.clear();
        }
    }

    /**
     * Update conversation_participant with read status.
     * Sets last_read_message_id, last_read_at, and resets unread_count to 0.
     * 
     * Requirement 7.5: Update participant last_read_message_id and reset unread_count to zero
     * 
     * @param event The read receipt event
     */
    private void updateParticipantReadStatus(ReadReceiptEvent event) {
        Optional<ConversationParticipant> participantOpt = participantRepository
            .findByConversation_ConversationIdAndUserId(
                event.getConversationId(), 
                event.getUserId()
            );

        if (participantOpt.isEmpty()) {
            log.warn("Participant not found for user {} in conversation {}", 
                event.getUserId(), event.getConversationId());
            return;
        }

        ConversationParticipant participant = participantOpt.get();
        
        // Update read status
        participant.setLastReadMessageId(event.getLastReadMessageId());
        participant.setLastReadAt(event.getReadAt());
        participant.setUnreadCount(0); // Reset unread count to 0
        
        participantRepository.save(participant);
        
        log.debug("Updated participant read status: user={}, conversation={}, lastReadMessageId={}, unreadCount=0",
            event.getUserId(), event.getConversationId(), event.getLastReadMessageId());
    }

    /**
     * Insert or update message_read_receipt record.
     * If a receipt already exists for this message and user, update the read_at timestamp.
     * 
     * Requirement 7.4: Insert/update message_read_receipt records in PostgreSQL
     * 
     * @param event The read receipt event
     */
    private void insertOrUpdateReadReceipt(ReadReceiptEvent event) {
        // Find the message
        Optional<Message> messageOpt = messageRepository.findById(event.getLastReadMessageId());
        
        if (messageOpt.isEmpty()) {
            log.warn("Message not found: {}", event.getLastReadMessageId());
            return;
        }

        Message message = messageOpt.get();

        // Check if read receipt already exists
        Optional<MessageReadReceipt> existingReceiptOpt = readReceiptRepository
            .findByMessage_MessageIdAndUserId(event.getLastReadMessageId(), event.getUserId());

        MessageReadReceipt receipt;
        if (existingReceiptOpt.isPresent()) {
            // Update existing receipt
            receipt = existingReceiptOpt.get();
            receipt.setReadAt(event.getReadAt());
            log.debug("Updating existing read receipt for message {} and user {}", 
                event.getLastReadMessageId(), event.getUserId());
        } else {
            // Create new receipt
            receipt = new MessageReadReceipt();
            receipt.setMessage(message);
            receipt.setUserId(event.getUserId());
            receipt.setReadAt(event.getReadAt());
            log.debug("Creating new read receipt for message {} and user {}", 
                event.getLastReadMessageId(), event.getUserId());
        }

        readReceiptRepository.save(receipt);
    }

    /**
     * Clear Redis cache for unread count.
     * Cache key format: "unread:{userId}:{conversationId}"
     * 
     * Requirement 11.5: Clear Redis cache when unread_count is updated
     * 
     * @param userId The user ID
     * @param conversationId The conversation ID
     */
    private void clearUnreadCache(String userId, String conversationId) {
        try {
            String cacheKey = "unread:" + userId + ":" + conversationId;
            redisTemplate.delete(cacheKey);
            log.debug("Cleared Redis cache for key: {}", cacheKey);
        } catch (Exception e) {
            // WARN level: Redis connection failures (graceful degradation)
            log.warn("Redis connection failure - graceful degradation: {}", e.getMessage());
            
            // Increment Redis errors counter
            metricsService.incrementRedisErrors();
        }
    }

    /**
     * Broadcast read status to all ACTIVE participants in the conversation.
     * Sends notification to /topic/conversation.{conversationId} destination.
     * 
     * Requirement 7.6: Broadcast read status to all participants via /topic/conversation.{conversationId}
     * 
     * @param event The read receipt event
     */
    private void broadcastReadStatus(ReadReceiptEvent event) {
        try {
            // Query all ACTIVE participants
            List<ConversationParticipant> participants = participantRepository
                .findByConversation_ConversationIdAndStatus(
                    event.getConversationId(), 
                    ParticipantStatus.ACTIVE.getCode()
                );

            if (participants.isEmpty()) {
                log.warn("No ACTIVE participants found for conversation {}", event.getConversationId());
                return;
            }

            // Broadcast to conversation topic
            String destination = "/topic/conversation." + event.getConversationId();
            messagingTemplate.convertAndSend(destination, event);
            
            log.debug("Broadcast read status to {} ACTIVE participants in conversation {} via {}",
                participants.size(), event.getConversationId(), destination);

        } catch (Exception e) {
            // Broadcasting errors should not fail the entire operation
            log.error("Failed to broadcast read status for conversation {}: {}", 
                event.getConversationId(), e.getMessage(), e);
        }
    }
}
