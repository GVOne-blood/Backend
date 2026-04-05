package com.theblood.springfood.chat.service.kafka;

import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.repository.ConversationRepository;
import com.theblood.springfood.chat.repository.MessageRepository;
import com.theblood.springfood.chat.service.ConversationService;
import com.theblood.springfood.chat.service.dto.ChatMessageEvent;
import com.theblood.springfood.chat.service.mapper.MessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ChatMessagePersistenceConsumer}.
 * Tests message persistence, deduplication, metadata updates, and retry logic.
 * 
 * Requirements: 5.1, 5.4, 5.7
 */
@ExtendWith(MockitoExtension.class)
class ChatMessagePersistenceConsumerTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationParticipantRepository participantRepository;

    @Mock
    private ConversationService conversationService;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private ChatMessagePersistenceConsumer consumer;

    @Captor
    private ArgumentCaptor<List<Message>> messageListCaptor;

    @Captor
    private ArgumentCaptor<List<ConversationParticipant>> participantListCaptor;

    private Conversation testConversation;
    private ChatMessageEvent testEvent;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        // Setup test conversation
        testConversation = new Conversation();
        testConversation.setConversationId("conv-123");
        testConversation.setConversationType("DIRECT");
        testConversation.setMessageCount(0L);

        // Setup test event
        testEvent = new ChatMessageEvent();
        testEvent.setMessageId("msg-123");
        testEvent.setClientMessageId("client-456");
        testEvent.setConversationId("conv-123");
        testEvent.setSenderId("user-1");
        testEvent.setSenderName("User One");
        testEvent.setMessageType("TEXT");
        testEvent.setContent("Hello World");
        testEvent.setStatus("SENDING");
        testEvent.setCreatedAt(Instant.now());

        // Setup test message
        testMessage = new Message();
        testMessage.setMessageId("msg-123");
        testMessage.setClientMessageId("client-456");
        testMessage.setSenderId("user-1");
        testMessage.setSenderName("User One");
        testMessage.setMessageType("TEXT");
        testMessage.setContent("Hello World");
        testMessage.setStatus("SENDING");
        testMessage.setConversation(testConversation);
        testMessage.setIsDeleted(0);
        testMessage.setIsEdited(0);
        testMessage.setReactionCount(0);
        testMessage.setIsRead(false);
    }

    /**
     * Test: Messages consumed with shared group ID "chat-persistence-group"
     * Requirement: 5.1
     */
    @Test
    void testConsumerGroupId() {
        // When
        String groupId = consumer.getConsumerGroupId();

        // Then
        assertThat(groupId).isEqualTo("chat-persistence-group");
    }

    /**
     * Test: Duplicate clientMessageId skipped (no duplicate insertion)
     * Requirement: 5.4
     */
    @Test
    void testDuplicateClientMessageIdSkipped() {
        // Given
        List<ChatMessageEvent> events = List.of(testEvent);
        
        when(messageMapper.eventToEntity(testEvent)).thenReturn(testMessage);
        when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
        
        // Simulate duplicate - message already exists
        when(messageRepository.existsByClientMessageId("client-456")).thenReturn(true);

        // When
        consumer.consumeMessages(events, acknowledgment);

        // Then
        // Verify no messages were saved (duplicate filtered out)
        verify(messageRepository, never()).saveAll(anyList());
        
        // Verify acknowledgment was still called
        verify(acknowledgment).acknowledge();
    }

    /**
     * Test: Batch insert with 100 messages
     * Requirement: 5.1
     */
    @Test
    void testBatchInsertWith100Messages() {
        // Given
        List<ChatMessageEvent> events = new ArrayList<>();
        List<Message> messages = new ArrayList<>();
        
        // Create 100 test messages
        for (int i = 0; i < 100; i++) {
            ChatMessageEvent event = new ChatMessageEvent();
            event.setMessageId("msg-" + i);
            event.setClientMessageId("client-" + i);
            event.setConversationId("conv-123");
            event.setSenderId("user-1");
            event.setMessageType("TEXT");
            event.setContent("Message " + i);
            event.setStatus("SENDING");
            event.setCreatedAt(Instant.now());
            events.add(event);

            Message message = new Message();
            message.setMessageId("msg-" + i);
            message.setClientMessageId("client-" + i);
            message.setSenderId("user-1");
            message.setMessageType("TEXT");
            message.setContent("Message " + i);
            message.setStatus("SENDING");
            message.setConversation(testConversation);
            message.setIsDeleted(0);
            message.setIsEdited(0);
            message.setReactionCount(0);
            message.setIsRead(false);
            messages.add(message);
        }

        when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
        when(messageRepository.existsByClientMessageId(anyString())).thenReturn(false);
        when(messageRepository.saveAll(anyList())).thenReturn(messages);
        when(participantRepository.findByConversation_ConversationIdAndStatusAndUserIdNot(
            anyString(), anyString(), anyString())).thenReturn(List.of());

        // Mock mapper for all events
        for (int i = 0; i < 100; i++) {
            when(messageMapper.eventToEntity(events.get(i))).thenReturn(messages.get(i));
        }

        // When
        consumer.consumeMessages(events, acknowledgment);

        // Then
        verify(messageRepository).saveAll(messageListCaptor.capture());
        List<Message> savedMessages = messageListCaptor.getValue();
        
        assertThat(savedMessages).hasSize(100);
        verify(acknowledgment).acknowledge();
    }

    /**
     * Test: Database error triggers retry with exponential backoff
     * Requirement: 5.7
     */
    @Test
    void testDatabaseErrorTriggersRetry() {
        // Given
        List<ChatMessageEvent> events = List.of(testEvent);
        
        when(messageMapper.eventToEntity(testEvent)).thenReturn(testMessage);
        when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
        when(messageRepository.existsByClientMessageId("client-456")).thenReturn(false);
        
        // Simulate database error
        when(messageRepository.saveAll(anyList()))
            .thenThrow(new DataAccessException("Database connection failed") {});

        // When/Then
        assertThatThrownBy(() -> consumer.consumeMessages(events, acknowledgment))
            .isInstanceOf(DataAccessException.class);

        // Verify acknowledgment was NOT called (message will be redelivered)
        verify(acknowledgment, never()).acknowledge();
    }

    /**
     * Test: Empty batch is handled gracefully
     */
    @Test
    void testEmptyBatchHandledGracefully() {
        // Given
        List<ChatMessageEvent> events = List.of();

        // When
        consumer.consumeMessages(events, acknowledgment);

        // Then
        verify(messageRepository, never()).saveAll(anyList());
        verify(acknowledgment).acknowledge();
    }

    /**
     * Test: Null batch is handled gracefully
     */
    @Test
    void testNullBatchHandledGracefully() {
        // When
        consumer.consumeMessages(null, acknowledgment);

        // Then
        verify(messageRepository, never()).saveAll(anyList());
        verify(acknowledgment).acknowledge();
    }

    /**
     * Test: Conversation metadata is updated after message persistence
     * Requirement: 5.5
     */
    @Test
    void testConversationMetadataUpdated() {
        // Given
        List<ChatMessageEvent> events = List.of(testEvent);
        
        when(messageMapper.eventToEntity(testEvent)).thenReturn(testMessage);
        when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
        when(messageRepository.existsByClientMessageId("client-456")).thenReturn(false);
        when(messageRepository.saveAll(anyList())).thenReturn(List.of(testMessage));
        when(participantRepository.findByConversation_ConversationIdAndStatusAndUserIdNot(
            anyString(), anyString(), anyString())).thenReturn(List.of());

        // When
        consumer.consumeMessages(events, acknowledgment);

        // Then
        verify(conversationService).updateLastMessage(
            eq("conv-123"),
            eq("msg-123"),
            eq("Hello World"),
            eq("user-1"),
            any(Instant.class)
        );
        verify(acknowledgment).acknowledge();
    }

    /**
     * Test: Unread count incremented for all participants except sender
     * Requirement: 5.6
     */
    @Test
    void testUnreadCountIncrementedForParticipants() {
        // Given
        List<ChatMessageEvent> events = List.of(testEvent);
        
        ConversationParticipant participant1 = new ConversationParticipant();
        participant1.setUserId("user-2");
        participant1.setUnreadCount(5);
        
        ConversationParticipant participant2 = new ConversationParticipant();
        participant2.setUserId("user-3");
        participant2.setUnreadCount(0);
        
        List<ConversationParticipant> participants = List.of(participant1, participant2);
        
        when(messageMapper.eventToEntity(testEvent)).thenReturn(testMessage);
        when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
        when(messageRepository.existsByClientMessageId("client-456")).thenReturn(false);
        when(messageRepository.saveAll(anyList())).thenReturn(List.of(testMessage));
        when(participantRepository.findByConversation_ConversationIdAndStatusAndUserIdNot(
            "conv-123", "ACTIVE", "user-1")).thenReturn(participants);

        // When
        consumer.consumeMessages(events, acknowledgment);

        // Then
        verify(participantRepository).saveAll(participantListCaptor.capture());
        List<ConversationParticipant> updatedParticipants = participantListCaptor.getValue();
        
        assertThat(updatedParticipants).hasSize(2);
        assertThat(updatedParticipants.get(0).getUnreadCount()).isEqualTo(6); // 5 + 1
        assertThat(updatedParticipants.get(1).getUnreadCount()).isEqualTo(1); // 0 + 1
        
        verify(acknowledgment).acknowledge();
    }

    /**
     * Test: Long content is truncated to 200 characters for preview
     */
    @Test
    void testLongContentTruncatedForPreview() {
        // Given
        String longContent = "A".repeat(300);
        testEvent.setContent(longContent);
        testMessage.setContent(longContent);
        
        List<ChatMessageEvent> events = List.of(testEvent);
        
        when(messageMapper.eventToEntity(testEvent)).thenReturn(testMessage);
        when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
        when(messageRepository.existsByClientMessageId("client-456")).thenReturn(false);
        when(messageRepository.saveAll(anyList())).thenReturn(List.of(testMessage));
        when(participantRepository.findByConversation_ConversationIdAndStatusAndUserIdNot(
            anyString(), anyString(), anyString())).thenReturn(List.of());

        // When
        consumer.consumeMessages(events, acknowledgment);

        // Then
        verify(conversationService).updateLastMessage(
            eq("conv-123"),
            eq("msg-123"),
            argThat(preview -> preview != null && preview.length() == 200),
            eq("user-1"),
            any(Instant.class)
        );
    }

    /**
     * Test: Multiple messages in batch are all processed
     */
    @Test
    void testMultipleMessagesInBatchProcessed() {
        // Given
        ChatMessageEvent event1 = new ChatMessageEvent();
        event1.setMessageId("msg-1");
        event1.setClientMessageId("client-1");
        event1.setConversationId("conv-123");
        event1.setSenderId("user-1");
        event1.setMessageType("TEXT");
        event1.setContent("Message 1");
        event1.setStatus("SENDING");
        event1.setCreatedAt(Instant.now());

        ChatMessageEvent event2 = new ChatMessageEvent();
        event2.setMessageId("msg-2");
        event2.setClientMessageId("client-2");
        event2.setConversationId("conv-123");
        event2.setSenderId("user-2");
        event2.setMessageType("TEXT");
        event2.setContent("Message 2");
        event2.setStatus("SENDING");
        event2.setCreatedAt(Instant.now());

        List<ChatMessageEvent> events = List.of(event1, event2);

        Message message1 = new Message();
        message1.setMessageId("msg-1");
        message1.setClientMessageId("client-1");
        message1.setSenderId("user-1");
        message1.setContent("Message 1");
        message1.setConversation(testConversation);
        message1.setIsDeleted(0);
        message1.setIsEdited(0);
        message1.setReactionCount(0);
        message1.setIsRead(false);

        Message message2 = new Message();
        message2.setMessageId("msg-2");
        message2.setClientMessageId("client-2");
        message2.setSenderId("user-2");
        message2.setContent("Message 2");
        message2.setConversation(testConversation);
        message2.setIsDeleted(0);
        message2.setIsEdited(0);
        message2.setReactionCount(0);
        message2.setIsRead(false);

        when(messageMapper.eventToEntity(event1)).thenReturn(message1);
        when(messageMapper.eventToEntity(event2)).thenReturn(message2);
        when(conversationRepository.findById("conv-123")).thenReturn(Optional.of(testConversation));
        when(messageRepository.existsByClientMessageId(anyString())).thenReturn(false);
        when(messageRepository.saveAll(anyList())).thenReturn(List.of(message1, message2));
        when(participantRepository.findByConversation_ConversationIdAndStatusAndUserIdNot(
            anyString(), anyString(), anyString())).thenReturn(List.of());

        // When
        consumer.consumeMessages(events, acknowledgment);

        // Then
        verify(messageRepository).saveAll(messageListCaptor.capture());
        List<Message> savedMessages = messageListCaptor.getValue();
        
        assertThat(savedMessages).hasSize(2);
        verify(conversationService, times(2)).updateLastMessage(
            anyString(), anyString(), anyString(), anyString(), any(Instant.class));
        verify(acknowledgment).acknowledge();
    }
}
