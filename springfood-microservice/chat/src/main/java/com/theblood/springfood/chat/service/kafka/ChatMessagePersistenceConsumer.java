package com.theblood.springfood.chat.service.kafka;

import com.theblood.springfood.chat.config.LoggingMDCUtil;
import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.repository.ConversationRepository;
import com.theblood.springfood.chat.repository.MessageRepository;
import com.theblood.springfood.chat.service.ChatMetricsService;
import com.theblood.springfood.chat.service.ConversationService;
import com.theblood.springfood.chat.service.dto.ChatMessageEvent;
import com.theblood.springfood.chat.service.mapper.MessageMapper;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka consumer for persisting chat messages to the database.
 * Uses shared consumer group "chat-persistence-group" for load-balanced processing.
 * Implements batch processing with max 100 messages or 5 seconds timeout.
 * 
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6
 */
@Component
public class ChatMessagePersistenceConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ChatMessagePersistenceConsumer.class);
    private static final String CONSUMER_GROUP_ID = "chat-persistence-group";
    private static final String PARTICIPANT_STATUS_ACTIVE = "ACTIVE";
    private static final int MAX_PREVIEW_LENGTH = 200;

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final ConversationService conversationService;
    private final MessageMapper messageMapper;
    private final ChatMetricsService metricsService;

    public ChatMessagePersistenceConsumer(
        MessageRepository messageRepository,
        ConversationRepository conversationRepository,
        ConversationParticipantRepository participantRepository,
        ConversationService conversationService,
        MessageMapper messageMapper,
        ChatMetricsService metricsService
    ) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.conversationService = conversationService;
        this.messageMapper = messageMapper;
        this.metricsService = metricsService;
    }

    /**
     * Consume messages from Kafka topic "chat-messages" in batch mode.
     * Batch size is configured in KafkaConsumerConfig (max 100 messages or 5s timeout).
     * 
     * @param events List of chat message events from Kafka
     * @param acknowledgment Kafka acknowledgment for manual commit
     */
    @KafkaListener(
        topics = "chat-messages",
        groupId = CONSUMER_GROUP_ID,
        containerFactory = "kafkaBatchPersistenceListenerContainerFactory"
    )
    @Transactional
    public void consumeMessages(List<ChatMessageEvent> events, Acknowledgment acknowledgment) {
        if (events == null || events.isEmpty()) {
            LOG.debug("Received empty batch, skipping processing");
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            return;
        }

        // Set MDC context with batch size
        LoggingMDCUtil.setBatchSize(events.size());
        if (!events.isEmpty()) {
            LoggingMDCUtil.setConversationId(events.get(0).getConversationId());
        }
        
        try {
            // DEBUG level: Message flow milestone - received batch from Kafka for persistence
            LOG.debug("Message batch received from Kafka for persistence");

            try {
                // Process the batch with retry logic
                persistMessageBatch(events);
                
                // Manually commit offset after successful processing
                if (acknowledgment != null) {
                    acknowledgment.acknowledge();
                }
                
                // DEBUG level: Message flow milestone - persisted
                LOG.info("Message batch persisted successfully");
            } catch (Exception e) {
                // ERROR level: Database errors with batch size
                LOG.error("Database error - failed to persist message batch after retries", e);
                
                // Increment Kafka errors counter
                metricsService.incrementKafkaErrors();
                
                // Don't acknowledge - Kafka will redeliver
                throw e;
            }
        } finally {
            LoggingMDCUtil.clear();
        }
    }

    /**
     * Persist a batch of messages to the database with retry logic.
     * Filters duplicates, batch inserts messages, updates conversation metadata,
     * and increments unread counts.
     * 
     * @param events List of chat message events to persist
     * @throws DataAccessException if database operation fails after retries
     */
    @Retryable(
        value = { DataAccessException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void persistMessageBatch(List<ChatMessageEvent> events) {
        // Note: Retry attempt number is automatically tracked by Spring Retry
        // We log it in the recover method if all retries fail
        
        // 1. Convert events to Message entities
        List<Message> messages = events.stream()
            .map(this::eventToMessage)
            .collect(Collectors.toList());

        // 2. Filter out duplicates by clientMessageId
        List<Message> uniqueMessages = messages.stream()
            .filter(msg -> !messageRepository.existsByClientMessageId(msg.getClientMessageId()))
            .collect(Collectors.toList());

        if (uniqueMessages.isEmpty()) {
            LOG.debug("All messages in batch are duplicates, skipping persistence");
            return;
        }

        LOG.debug("Persisting {} unique messages (filtered {} duplicates)", 
            uniqueMessages.size(), messages.size() - uniqueMessages.size());

        // 3. Batch insert messages
        List<Message> savedMessages = messageRepository.saveAll(uniqueMessages);

        // Increment messages persisted counter
        metricsService.incrementMessagesPersisted(savedMessages.size());
        
        // Record persistence latency for the first message in batch
        if (!savedMessages.isEmpty() && savedMessages.get(0).getCreatedDate() != null) {
            metricsService.recordPersistenceLatency(savedMessages.get(0).getCreatedDate());
        }

        // 4. Update conversation metadata and unread counts for each message
        for (Message message : savedMessages) {
            try {
                // Update conversation metadata
                updateConversationMetadata(message);
                
                // Increment unread count for all participants except sender
                incrementUnreadCounts(message);
            } catch (Exception e) {
                // ERROR level: Database errors
                LOG.error("Database error - failed to update metadata for message", e);
                // Continue processing other messages
            }
        }
    }

    /**
     * Convert ChatMessageEvent to Message entity.
     * Sets up the conversation relationship and initializes default values.
     * 
     * @param event The Kafka event
     * @return Message entity ready for persistence
     */
    private Message eventToMessage(ChatMessageEvent event) {
        Message message = messageMapper.eventToEntity(event);
        
        // Set conversation relationship
        Conversation conversation = conversationRepository.findById(event.getConversationId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Conversation not found: " + event.getConversationId()));
        message.setConversation(conversation);
        
        // Initialize default values if not set
        if (message.getIsDeleted() == null) {
            message.setIsDeleted(0);
        }
        if (message.getIsEdited() == null) {
            message.setIsEdited(0);
        }
        if (message.getReactionCount() == null) {
            message.setReactionCount(0);
        }
        if (message.getIsRead() == null) {
            message.setIsRead(false);
        }
        
        return message;
    }

    /**
     * Update conversation metadata when a new message is persisted.
     * Updates last_message_at, last_message_preview, last_message_sender_id,
     * last_message_id, and increments message_count.
     * 
     * @param message The persisted message
     */
    private void updateConversationMetadata(Message message) {
        String conversationId = message.getConversation().getConversationId();
        String preview = message.getContent();
        
        // Truncate preview to 200 characters
        if (preview != null && preview.length() > MAX_PREVIEW_LENGTH) {
            preview = preview.substring(0, MAX_PREVIEW_LENGTH);
        }
        
        conversationService.updateLastMessage(
            conversationId,
            message.getMessageId(),
            preview,
            message.getSenderId(),
            message.getCreatedDate()
        );
        
        LOG.debug("Updated conversation metadata for conversation {}", conversationId);
    }

    /**
     * Increment unread_count for all ACTIVE participants except the sender.
     * 
     * @param message The persisted message
     */
    private void incrementUnreadCounts(Message message) {
        String conversationId = message.getConversation().getConversationId();
        String senderId = message.getSenderId();
        
        // Get all ACTIVE participants except sender
        List<ConversationParticipant> participants = participantRepository
            .findByConversation_ConversationIdAndStatusAndUserIdNot(
                conversationId,
                PARTICIPANT_STATUS_ACTIVE,
                senderId
            );
        
        // Increment unread count for each participant
        for (ConversationParticipant participant : participants) {
            Integer currentCount = participant.getUnreadCount();
            participant.setUnreadCount(currentCount != null ? currentCount + 1 : 1);
        }
        
        // Batch save all updated participants
        if (!participants.isEmpty()) {
            participantRepository.saveAll(participants);
            LOG.debug("Incremented unread count for {} participants in conversation {}", 
                participants.size(), conversationId);
        }
    }

    /**
     * Get the consumer group ID used by this consumer.
     * Exposed for testing purposes.
     * 
     * @return The consumer group ID
     */
    public String getConsumerGroupId() {
        return CONSUMER_GROUP_ID;
    }
}
