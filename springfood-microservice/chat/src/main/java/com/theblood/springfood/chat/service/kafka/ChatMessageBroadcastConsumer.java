package com.theblood.springfood.chat.service.kafka;

import com.theblood.springfood.chat.config.LoggingMDCUtil;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.domain.enumeration.ParticipantStatus;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.service.ChatMetricsService;
import com.theblood.springfood.chat.service.dto.ChatMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Kafka consumer for broadcasting chat messages to connected WebSocket clients.
 * 
 * Architecture:
 * - Each service instance has a UNIQUE consumer group ID (chat-broadcast-{UUID})
 * - This ensures ALL instances receive ALL messages from Kafka (fan-out pattern)
 * - Each instance then broadcasts only to users connected to THAT instance
 * - This enables horizontal scaling with multiple service instances
 * 
 * Message Flow:
 * 1. Consume message from Kafka topic "chat-messages"
 * 2. Query all ACTIVE participants from database
 * 3. Check which participants are connected to THIS instance (via SimpUserRegistry)
 * 4. Send message via WebSocket to connected users only
 * 5. Manually commit Kafka offset after successful delivery
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 13.6
 */
@Component
public class ChatMessageBroadcastConsumer {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageBroadcastConsumer.class);
    
    /**
     * Unique consumer group ID per instance for fan-out broadcasting.
     * Format: "chat-broadcast-{UUID}" ensures each instance receives all messages.
     * 
     * Requirement 13.1: Each instance generates unique group ID
     * Static and public to be accessible by @KafkaListener SpEL evaluation
     */
    public static final String CONSUMER_GROUP_ID = "chat-broadcast-" + UUID.randomUUID();

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;
    private final ConversationParticipantRepository participantRepository;
    private final ChatMetricsService metricsService;

    public ChatMessageBroadcastConsumer(
            SimpMessagingTemplate messagingTemplate,
            SimpUserRegistry userRegistry,
            ConversationParticipantRepository participantRepository,
            ChatMetricsService metricsService) {
        this.messagingTemplate = messagingTemplate;
        this.userRegistry = userRegistry;
        this.participantRepository = participantRepository;
        this.metricsService = metricsService;
        
        log.info("ChatMessageBroadcastConsumer initialized with group ID: {}", CONSUMER_GROUP_ID);
    }

    /**
     * Consume messages from Kafka and broadcast to connected WebSocket clients.
     * 
     * Kafka Configuration:
     * - Topic: "chat-messages"
     * - Group ID: Unique per instance (chat-broadcast-{UUID})
     * - Container Factory: kafkaBroadcastListenerContainerFactory (single message mode)
     * - Acknowledgment: Manual (after successful WebSocket delivery)
     * 
     * Processing Steps:
     * 1. Receive ChatMessageEvent from Kafka
     * 2. Query ACTIVE participants for the conversation
     * 3. Filter participants connected to THIS instance
     * 4. Send message to each connected user via WebSocket
     * 5. Acknowledge Kafka offset
     * 
     * Requirements:
     * - 4.1: Consume with unique group ID per instance
     * - 4.2: Query only ACTIVE participants
     * - 4.3: Identify participants
     * - 4.4: Send only to users connected to this instance
     * - 13.6: Manual offset commit for at-least-once delivery
     * 
     * @param event The chat message event from Kafka
     * @param acknowledgment Kafka acknowledgment for manual commit
     */
    @KafkaListener(
        topics = "chat-messages",
        groupId = "#{T(com.theblood.springfood.chat.service.kafka.ChatMessageBroadcastConsumer).CONSUMER_GROUP_ID}",
        containerFactory = "kafkaBroadcastListenerContainerFactory"
    )
    public void consumeMessage(ChatMessageEvent event, Acknowledgment acknowledgment) {
        // Set MDC context for structured logging
        LoggingMDCUtil.setMessageContext(
            event.getConversationId(),
            event.getMessageId(),
            event.getSenderId()
        );
        
        try {
            // DEBUG level: Message flow milestone - received from Kafka for broadcasting
            log.debug("Message received from Kafka for broadcasting");

            // Requirement 4.2: Query all ACTIVE participants from conversation_participant table
            List<ConversationParticipant> participants = participantRepository
                .findByConversation_ConversationIdAndStatus(
                    event.getConversationId(), 
                    ParticipantStatus.ACTIVE.getCode()
                );

            log.debug("Found {} ACTIVE participants for conversation", participants.size());

            int deliveredCount = 0;

            // Requirement 4.3, 4.4: Check which participants are connected to THIS instance
            for (ConversationParticipant participant : participants) {
                String userId = participant.getUserId();
                
                // Check if user is connected to THIS instance via SimpUserRegistry
                SimpUser user = userRegistry.getUser(userId);
                
                if (user != null && !user.getSessions().isEmpty()) {
                    // User is connected to this instance - send message via WebSocket
                    // Requirement 4.4: Use SimpMessagingTemplate.convertAndSendToUser()
                    messagingTemplate.convertAndSendToUser(
                        userId,
                        "/queue/messages",
                        event
                    );
                    
                    deliveredCount++;
                    
                    // Increment messages delivered counter
                    metricsService.incrementMessagesDelivered();
                    
                    // Record message latency (time from send to receive)
                    if (event.getCreatedAt() != null) {
                        metricsService.recordMessageLatency(event.getCreatedAt());
                    }
                    
                    // DEBUG level: Message flow milestone - delivered to client
                    log.debug("Message delivered to user {} on this instance", userId);
                } else {
                    log.trace("User {} not connected to this instance, skipping", userId);
                }
            }

            log.info("Broadcast complete: delivered to {}/{} participants on this instance",
                deliveredCount, participants.size());

            // Requirement 13.6: Manually commit Kafka offset after successful delivery
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.trace("Kafka offset acknowledged");
            }

        } catch (Exception e) {
            // ERROR level: Kafka consumer errors
            log.error("Kafka consumer error - failed to broadcast message", e);
            
            // Increment Kafka errors counter
            metricsService.incrementKafkaErrors();
            
            // Don't acknowledge on error - message will be redelivered
            // This ensures at-least-once delivery guarantee
            throw e;
        } finally {
            LoggingMDCUtil.clear();
        }
    }

    /**
     * Getter for consumer group ID (used for logging and monitoring).
     * 
     * @return The unique consumer group ID for this instance
     */
    public static String getConsumerGroupId() {
        return CONSUMER_GROUP_ID;
    }
}
