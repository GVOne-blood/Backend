package com.theblood.springfood.chat.service.kafka;

import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.domain.MessageReadReceipt;
import com.theblood.springfood.chat.domain.enumeration.ParticipantStatus;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.repository.MessageReadReceiptRepository;
import com.theblood.springfood.chat.repository.MessageRepository;
import com.theblood.springfood.chat.service.ChatMetricsService;
import com.theblood.springfood.chat.service.dto.ReadReceiptEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReadReceiptConsumer}.
 * 
 * Tests verify:
 * - Read receipt processing for valid participants
 * - Error handling for non-participants
 * - Error handling for non-existent messages
 * - last_read_at timestamp updates
 * - Redis cache clearing
 * - Broadcast to all ACTIVE participants
 * - Manual Kafka offset acknowledgment
 */
@ExtendWith(MockitoExtension.class)
class ReadReceiptConsumerTest {

    @Mock
    private ConversationParticipantRepository participantRepository;

    @Mock
    private MessageReadReceiptRepository readReceiptRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatMetricsService metricsService;

    @Mock
    private Acknowledgment acknowledgment;

    private ReadReceiptConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ReadReceiptConsumer(
            participantRepository,
            readReceiptRepository,
            messageRepository,
            redisTemplate,
            messagingTemplate,
            metricsService
        );
    }

    /**
     * Test: Should process read receipt for valid participant.
     * Requirements: 7.4, 7.5
     */
    @Test
    void shouldProcessReadReceiptForValidParticipant() {
        // Given: Valid read receipt event
        ReadReceiptEvent event = createTestEvent("conv-1", "user-1", "msg-1");
        
        ConversationParticipant participant = createTestParticipant("user-1", "conv-1");
        participant.setUnreadCount(5);
        
        Message message = createTestMessage("msg-1", "conv-1");
        
        when(participantRepository.findByConversation_ConversationIdAndUserId("conv-1", "user-1"))
            .thenReturn(Optional.of(participant));
        when(messageRepository.findById("msg-1")).thenReturn(Optional.of(message));
        when(readReceiptRepository.findByMessage_MessageIdAndUserId("msg-1", "user-1"))
            .thenReturn(Optional.empty());
        when(participantRepository.findByConversation_ConversationIdAndStatus("conv-1", ParticipantStatus.ACTIVE.getCode()))
            .thenReturn(Arrays.asList(participant));

        // When: Processing read receipt
        consumer.consumeReadReceipt(event, acknowledgment);

        // Then: Participant should be updated
        ArgumentCaptor<ConversationParticipant> participantCaptor = ArgumentCaptor.forClass(ConversationParticipant.class);
        verify(participantRepository).save(participantCaptor.capture());
        
        ConversationParticipant savedParticipant = participantCaptor.getValue();
        assertThat(savedParticipant.getLastReadMessageId()).isEqualTo("msg-1");
        assertThat(savedParticipant.getLastReadAt()).isEqualTo(event.getReadAt());
        assertThat(savedParticipant.getUnreadCount()).isEqualTo(0); // Reset to 0
        
        // And: Read receipt should be created
        verify(readReceiptRepository).save(any(MessageReadReceipt.class));
        
        // And: Redis cache should be cleared
        verify(redisTemplate).delete("unread:user-1:conv-1");
        
        // And: Broadcast should be sent
        verify(messagingTemplate).convertAndSend(eq("/topic/conversation.conv-1"), eq(event));
        
        // And: Kafka offset should be acknowledged
        verify(acknowledgment).acknowledge();
    }

    /**
     * Test: Should handle non-participant gracefully.
     * Requirement 7.2: Validate user is participant
     */
    @Test
    void shouldHandleNonParticipantGracefully() {
        // Given: Read receipt event for non-participant
        ReadReceiptEvent event = createTestEvent("conv-1", "user-999", "msg-1");
        
        when(participantRepository.findByConversation_ConversationIdAndUserId("conv-1", "user-999"))
            .thenReturn(Optional.empty());

        // When: Processing read receipt
        consumer.consumeReadReceipt(event, acknowledgment);

        // Then: Should not update participant (doesn't exist)
        verify(participantRepository, never()).save(any());
        
        // But: Should still acknowledge (graceful handling)
        verify(acknowledgment).acknowledge();
    }

    /**
     * Test: Should handle non-existent message gracefully.
     * Requirement 7.4: Validate message exists
     */
    @Test
    void shouldHandleNonExistentMessageGracefully() {
        // Given: Read receipt event for non-existent message
        ReadReceiptEvent event = createTestEvent("conv-1", "user-1", "msg-999");
        
        ConversationParticipant participant = createTestParticipant("user-1", "conv-1");
        
        when(participantRepository.findByConversation_ConversationIdAndUserId("conv-1", "user-1"))
            .thenReturn(Optional.of(participant));
        when(messageRepository.findById("msg-999")).thenReturn(Optional.empty());
        when(participantRepository.findByConversation_ConversationIdAndStatus("conv-1", ParticipantStatus.ACTIVE.getCode()))
            .thenReturn(Arrays.asList(participant));

        // When: Processing read receipt
        consumer.consumeReadReceipt(event, acknowledgment);

        // Then: Participant should still be updated (last_read_message_id can be set)
        verify(participantRepository).save(any(ConversationParticipant.class));
        
        // But: Read receipt should not be created (message doesn't exist)
        verify(readReceiptRepository, never()).save(any());
        
        // And: Should still acknowledge
        verify(acknowledgment).acknowledge();
    }

    /**
     * Test: Should update last_read_at timestamp correctly.
     * Requirement 7.5: Update last_read_at timestamp
     */
    @Test
    void shouldUpdateLastReadAtTimestamp() {
        // Given: Read receipt event with specific timestamp
        Instant readTime = Instant.parse("2024-01-15T10:30:00Z");
        ReadReceiptEvent event = new ReadReceiptEvent("conv-1", "user-1", "msg-1", readTime);
        
        ConversationParticipant participant = createTestParticipant("user-1", "conv-1");
        Message message = createTestMessage("msg-1", "conv-1");
        
        when(participantRepository.findByConversation_ConversationIdAndUserId("conv-1", "user-1"))
            .thenReturn(Optional.of(participant));
        when(messageRepository.findById("msg-1")).thenReturn(Optional.of(message));
        when(readReceiptRepository.findByMessage_MessageIdAndUserId("msg-1", "user-1"))
            .thenReturn(Optional.empty());
        when(participantRepository.findByConversation_ConversationIdAndStatus("conv-1", ParticipantStatus.ACTIVE.getCode()))
            .thenReturn(Arrays.asList(participant));

        // When: Processing read receipt
        consumer.consumeReadReceipt(event, acknowledgment);

        // Then: last_read_at should be set to event timestamp
        ArgumentCaptor<ConversationParticipant> participantCaptor = ArgumentCaptor.forClass(ConversationParticipant.class);
        verify(participantRepository).save(participantCaptor.capture());
        
        assertThat(participantCaptor.getValue().getLastReadAt()).isEqualTo(readTime);
    }

    /**
     * Test: Should clear Redis cache after processing.
     * Requirement 11.5: Clear Redis cache when unread_count is updated
     */
    @Test
    void shouldClearRedisCacheAfterProcessing() {
        // Given: Valid read receipt event
        ReadReceiptEvent event = createTestEvent("conv-1", "user-1", "msg-1");
        
        ConversationParticipant participant = createTestParticipant("user-1", "conv-1");
        Message message = createTestMessage("msg-1", "conv-1");
        
        when(participantRepository.findByConversation_ConversationIdAndUserId("conv-1", "user-1"))
            .thenReturn(Optional.of(participant));
        when(messageRepository.findById("msg-1")).thenReturn(Optional.of(message));
        when(readReceiptRepository.findByMessage_MessageIdAndUserId("msg-1", "user-1"))
            .thenReturn(Optional.empty());
        when(participantRepository.findByConversation_ConversationIdAndStatus("conv-1", ParticipantStatus.ACTIVE.getCode()))
            .thenReturn(Arrays.asList(participant));

        // When: Processing read receipt
        consumer.consumeReadReceipt(event, acknowledgment);

        // Then: Redis cache should be cleared with correct key format
        verify(redisTemplate).delete("unread:user-1:conv-1");
    }

    /**
     * Test: Should update existing read receipt if already exists.
     * Requirement 7.4: Insert/update message_read_receipt records
     */
    @Test
    void shouldUpdateExistingReadReceipt() {
        // Given: Read receipt event and existing receipt
        ReadReceiptEvent event = createTestEvent("conv-1", "user-1", "msg-1");
        
        ConversationParticipant participant = createTestParticipant("user-1", "conv-1");
        Message message = createTestMessage("msg-1", "conv-1");
        MessageReadReceipt existingReceipt = new MessageReadReceipt();
        existingReceipt.setReceiptId("receipt-1");
        existingReceipt.setMessage(message);
        existingReceipt.setUserId("user-1");
        existingReceipt.setReadAt(Instant.parse("2024-01-15T09:00:00Z"));
        
        when(participantRepository.findByConversation_ConversationIdAndUserId("conv-1", "user-1"))
            .thenReturn(Optional.of(participant));
        when(messageRepository.findById("msg-1")).thenReturn(Optional.of(message));
        when(readReceiptRepository.findByMessage_MessageIdAndUserId("msg-1", "user-1"))
            .thenReturn(Optional.of(existingReceipt));
        when(participantRepository.findByConversation_ConversationIdAndStatus("conv-1", ParticipantStatus.ACTIVE.getCode()))
            .thenReturn(Arrays.asList(participant));

        // When: Processing read receipt
        consumer.consumeReadReceipt(event, acknowledgment);

        // Then: Existing receipt should be updated (not created new)
        ArgumentCaptor<MessageReadReceipt> receiptCaptor = ArgumentCaptor.forClass(MessageReadReceipt.class);
        verify(readReceiptRepository).save(receiptCaptor.capture());
        
        MessageReadReceipt savedReceipt = receiptCaptor.getValue();
        assertThat(savedReceipt.getReceiptId()).isEqualTo("receipt-1"); // Same receipt
        assertThat(savedReceipt.getReadAt()).isEqualTo(event.getReadAt()); // Updated timestamp
    }

    /**
     * Test: Should broadcast to all ACTIVE participants.
     * Requirement 7.6: Broadcast read status to all participants
     */
    @Test
    void shouldBroadcastToAllActiveParticipants() {
        // Given: Read receipt event and multiple ACTIVE participants
        ReadReceiptEvent event = createTestEvent("conv-1", "user-1", "msg-1");
        
        ConversationParticipant participant1 = createTestParticipant("user-1", "conv-1");
        ConversationParticipant participant2 = createTestParticipant("user-2", "conv-1");
        ConversationParticipant participant3 = createTestParticipant("user-3", "conv-1");
        List<ConversationParticipant> participants = Arrays.asList(participant1, participant2, participant3);
        
        Message message = createTestMessage("msg-1", "conv-1");
        
        when(participantRepository.findByConversation_ConversationIdAndUserId("conv-1", "user-1"))
            .thenReturn(Optional.of(participant1));
        when(messageRepository.findById("msg-1")).thenReturn(Optional.of(message));
        when(readReceiptRepository.findByMessage_MessageIdAndUserId("msg-1", "user-1"))
            .thenReturn(Optional.empty());
        when(participantRepository.findByConversation_ConversationIdAndStatus("conv-1", ParticipantStatus.ACTIVE.getCode()))
            .thenReturn(participants);

        // When: Processing read receipt
        consumer.consumeReadReceipt(event, acknowledgment);

        // Then: Should broadcast to conversation topic (all participants will receive)
        verify(messagingTemplate).convertAndSend(eq("/topic/conversation.conv-1"), eq(event));
    }

    /**
     * Test: Should not acknowledge on error.
     * Ensures at-least-once delivery guarantee
     */
    @Test
    void shouldNotAcknowledgeOnError() {
        // Given: Read receipt event that will cause error
        ReadReceiptEvent event = createTestEvent("conv-1", "user-1", "msg-1");
        
        when(participantRepository.findByConversation_ConversationIdAndUserId("conv-1", "user-1"))
            .thenThrow(new RuntimeException("Database error"));

        // When/Then: Processing should throw exception
        assertThatThrownBy(() -> consumer.consumeReadReceipt(event, acknowledgment))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Database error");

        // And: Should not acknowledge (message will be redelivered)
        verify(acknowledgment, never()).acknowledge();
    }

    /**
     * Test: Should handle Redis errors gracefully.
     * Redis errors should not fail the entire operation
     */
    @Test
    void shouldHandleRedisErrorsGracefully() {
        // Given: Valid read receipt event but Redis fails
        ReadReceiptEvent event = createTestEvent("conv-1", "user-1", "msg-1");
        
        ConversationParticipant participant = createTestParticipant("user-1", "conv-1");
        Message message = createTestMessage("msg-1", "conv-1");
        
        when(participantRepository.findByConversation_ConversationIdAndUserId("conv-1", "user-1"))
            .thenReturn(Optional.of(participant));
        when(messageRepository.findById("msg-1")).thenReturn(Optional.of(message));
        when(readReceiptRepository.findByMessage_MessageIdAndUserId("msg-1", "user-1"))
            .thenReturn(Optional.empty());
        when(participantRepository.findByConversation_ConversationIdAndStatus("conv-1", ParticipantStatus.ACTIVE.getCode()))
            .thenReturn(Arrays.asList(participant));
        
        // Redis throws exception
        when(redisTemplate.delete(anyString())).thenThrow(new RuntimeException("Redis connection failed"));

        // When: Processing read receipt (should not throw)
        consumer.consumeReadReceipt(event, acknowledgment);

        // Then: Should still complete successfully
        verify(participantRepository).save(any(ConversationParticipant.class));
        verify(readReceiptRepository).save(any(MessageReadReceipt.class));
        verify(messagingTemplate).convertAndSend(anyString(), any(ReadReceiptEvent.class));
        verify(acknowledgment).acknowledge();
    }

    // Helper methods

    private ReadReceiptEvent createTestEvent(String conversationId, String userId, String messageId) {
        return new ReadReceiptEvent(
            conversationId,
            userId,
            messageId,
            Instant.now()
        );
    }

    private ConversationParticipant createTestParticipant(String userId, String conversationId) {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setParticipantId("participant-" + userId);
        participant.setUserId(userId);
        participant.setStatus(ParticipantStatus.ACTIVE.getCode());
        participant.setRole("MEMBER");
        participant.setUnreadCount(0);
        
        Conversation conversation = new Conversation();
        conversation.setConversationId(conversationId);
        participant.setConversation(conversation);
        
        return participant;
    }

    private Message createTestMessage(String messageId, String conversationId) {
        Message message = new Message();
        message.setMessageId(messageId);
        
        Conversation conversation = new Conversation();
        conversation.setConversationId(conversationId);
        message.setConversation(conversation);
        
        return message;
    }
}
