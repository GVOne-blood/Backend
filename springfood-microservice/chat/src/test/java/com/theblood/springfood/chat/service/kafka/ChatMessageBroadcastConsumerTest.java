package com.theblood.springfood.chat.service.kafka;

import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.domain.enumeration.ParticipantStatus;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.service.ChatMetricsService;
import com.theblood.springfood.chat.service.dto.ChatMessageEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ChatMessageBroadcastConsumer}.
 * 
 * Tests verify:
 * - Unique consumer group ID generation per instance
 * - ACTIVE participant filtering
 * - Instance-specific broadcasting via SimpUserRegistry
 * - Manual Kafka offset acknowledgment
 * - Error handling without acknowledgment
 */
@ExtendWith(MockitoExtension.class)
class ChatMessageBroadcastConsumerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private SimpUserRegistry userRegistry;

    @Mock
    private ConversationParticipantRepository participantRepository;

    @Mock
    private ChatMetricsService metricsService;

    @Mock
    private Acknowledgment acknowledgment;

    @Mock
    private SimpUser simpUser;

    @Mock
    private SimpSession simpSession;

    private ChatMessageBroadcastConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ChatMessageBroadcastConsumer(messagingTemplate, userRegistry, participantRepository, metricsService);
    }

    /**
     * Test: Consumer group ID should be unique per instance.
     * Requirement 13.1: Each instance generates unique group ID
     */
    @Test
    void shouldGenerateUniqueConsumerGroupId() {
        // Given: Two consumer instances
        ChatMessageBroadcastConsumer consumer1 = new ChatMessageBroadcastConsumer(
            messagingTemplate, userRegistry, participantRepository, metricsService
        );
        ChatMessageBroadcastConsumer consumer2 = new ChatMessageBroadcastConsumer(
            messagingTemplate, userRegistry, participantRepository, metricsService
        );

        // When: Getting consumer group IDs
        String groupId1 = consumer1.getConsumerGroupId();
        String groupId2 = consumer2.getConsumerGroupId();

        // Then: Group IDs should be different and follow format
        assertThat(groupId1).isNotEqualTo(groupId2);
        assertThat(groupId1).startsWith("chat-broadcast-");
        assertThat(groupId2).startsWith("chat-broadcast-");
    }

    /**
     * Test: Should only broadcast to ACTIVE participants.
     * Requirement 4.2: Query only ACTIVE participants
     */
    @Test
    void shouldOnlyBroadcastToActiveParticipants() {
        // Given: Message event
        ChatMessageEvent event = createTestEvent("conv-1", "msg-1", "user-1");

        // And: Participants with different statuses
        ConversationParticipant activeParticipant = createParticipant("user-2", "ACTIVE");
        ConversationParticipant leftParticipant = createParticipant("user-3", "LEFT");
        ConversationParticipant removedParticipant = createParticipant("user-4", "REMOVED");

        when(participantRepository.findByConversation_ConversationIdAndStatus("conv-1", "ACTIVE"))
            .thenReturn(List.of(activeParticipant));

        // And: User is connected
        when(userRegistry.getUser("user-2")).thenReturn(simpUser);
        when(simpUser.getSessions()).thenReturn(Set.of(simpSession));

        // When: Consuming message
        consumer.consumeMessage(event, acknowledgment);

        // Then: Should only query ACTIVE participants
        verify(participantRepository).findByConversation_ConversationIdAndStatus("conv-1", "ACTIVE");
        
        // And: Should send to active participant only
        verify(messagingTemplate).convertAndSendToUser(eq("user-2"), eq("/queue/messages"), eq(event));
        verify(messagingTemplate, never()).convertAndSendToUser(eq("user-3"), any(), any());
        verify(messagingTemplate, never()).convertAndSendToUser(eq("user-4"), any(), any());
    }

    /**
     * Test: Should only send to users connected to THIS instance.
     * Requirement 4.4: Send only to users connected to this instance
     */
    @Test
    void shouldOnlySendToUsersConnectedToThisInstance() {
        // Given: Message event
        ChatMessageEvent event = createTestEvent("conv-1", "msg-1", "user-1");

        // And: Two ACTIVE participants
        ConversationParticipant participant1 = createParticipant("user-2", "ACTIVE");
        ConversationParticipant participant2 = createParticipant("user-3", "ACTIVE");

        when(participantRepository.findByConversation_ConversationIdAndStatus("conv-1", "ACTIVE"))
            .thenReturn(Arrays.asList(participant1, participant2));

        // And: Only user-2 is connected to THIS instance
        when(userRegistry.getUser("user-2")).thenReturn(simpUser);
        when(simpUser.getSessions()).thenReturn(Set.of(simpSession));
        when(userRegistry.getUser("user-3")).thenReturn(null); // Not connected to this instance

        // When: Consuming message
        consumer.consumeMessage(event, acknowledgment);

        // Then: Should only send to user-2 (connected to this instance)
        verify(messagingTemplate).convertAndSendToUser(eq("user-2"), eq("/queue/messages"), eq(event));
        verify(messagingTemplate, never()).convertAndSendToUser(eq("user-3"), any(), any());
    }

    /**
     * Test: Should acknowledge Kafka offset after successful delivery.
     * Requirement 13.6: Manual offset commit for at-least-once delivery
     */
    @Test
    void shouldAcknowledgeKafkaOffsetAfterSuccessfulDelivery() {
        // Given: Message event
        ChatMessageEvent event = createTestEvent("conv-1", "msg-1", "user-1");

        // And: One ACTIVE participant connected
        ConversationParticipant participant = createParticipant("user-2", "ACTIVE");
        when(participantRepository.findByConversation_ConversationIdAndStatus("conv-1", "ACTIVE"))
            .thenReturn(List.of(participant));
        when(userRegistry.getUser("user-2")).thenReturn(simpUser);
        when(simpUser.getSessions()).thenReturn(Set.of(simpSession));

        // When: Consuming message
        consumer.consumeMessage(event, acknowledgment);

        // Then: Should acknowledge Kafka offset
        verify(acknowledgment).acknowledge();
    }

    /**
     * Test: Should not acknowledge Kafka offset on error.
     * Ensures at-least-once delivery by allowing message redelivery on failure.
     */
    @Test
    void shouldNotAcknowledgeKafkaOffsetOnError() {
        // Given: Message event
        ChatMessageEvent event = createTestEvent("conv-1", "msg-1", "user-1");

        // And: Repository throws exception
        when(participantRepository.findByConversation_ConversationIdAndStatus(any(), any()))
            .thenThrow(new RuntimeException("Database error"));

        // When/Then: Consuming message should throw exception
        try {
            consumer.consumeMessage(event, acknowledgment);
        } catch (RuntimeException e) {
            // Expected
        }

        // Then: Should NOT acknowledge Kafka offset (message will be redelivered)
        verify(acknowledgment, never()).acknowledge();
    }

    /**
     * Test: Should handle no connected users gracefully.
     * Requirement 4.4: Skip broadcasting when no users connected to this instance
     */
    @Test
    void shouldHandleNoConnectedUsersGracefully() {
        // Given: Message event
        ChatMessageEvent event = createTestEvent("conv-1", "msg-1", "user-1");

        // And: ACTIVE participants but none connected to this instance
        ConversationParticipant participant = createParticipant("user-2", "ACTIVE");
        when(participantRepository.findByConversation_ConversationIdAndStatus("conv-1", "ACTIVE"))
            .thenReturn(List.of(participant));
        when(userRegistry.getUser("user-2")).thenReturn(null);

        // When: Consuming message
        consumer.consumeMessage(event, acknowledgment);

        // Then: Should not send any messages
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());

        // But: Should still acknowledge (message was processed, just no local recipients)
        verify(acknowledgment).acknowledge();
    }

    /**
     * Test: Should use correct WebSocket destination.
     * Requirement 4.4: Send to /user/{userId}/queue/messages
     */
    @Test
    void shouldUseCorrectWebSocketDestination() {
        // Given: Message event
        ChatMessageEvent event = createTestEvent("conv-1", "msg-1", "user-1");

        // And: One ACTIVE participant connected
        ConversationParticipant participant = createParticipant("user-2", "ACTIVE");
        when(participantRepository.findByConversation_ConversationIdAndStatus("conv-1", "ACTIVE"))
            .thenReturn(List.of(participant));
        when(userRegistry.getUser("user-2")).thenReturn(simpUser);
        when(simpUser.getSessions()).thenReturn(Set.of(simpSession));

        // When: Consuming message
        consumer.consumeMessage(event, acknowledgment);

        // Then: Should send to correct destination
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate).convertAndSendToUser(
            eq("user-2"),
            destinationCaptor.capture(),
            eq(event)
        );
        assertThat(destinationCaptor.getValue()).isEqualTo("/queue/messages");
    }

    // Helper methods

    private ChatMessageEvent createTestEvent(String conversationId, String messageId, String senderId) {
        ChatMessageEvent event = new ChatMessageEvent();
        event.setConversationId(conversationId);
        event.setMessageId(messageId);
        event.setSenderId(senderId);
        event.setSenderName("Test User");
        event.setContent("Test message");
        event.setMessageType("TEXT");
        event.setStatus("SENT");
        event.setCreatedAt(Instant.now());
        return event;
    }

    private ConversationParticipant createParticipant(String userId, String status) {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setUserId(userId);
        participant.setStatus(status);
        participant.setDisplayName("Test User");
        
        // Create a minimal conversation
        Conversation conversation = new Conversation();
        conversation.setConversationId("conv-1");
        participant.setConversation(conversation);
        
        return participant;
    }
}
