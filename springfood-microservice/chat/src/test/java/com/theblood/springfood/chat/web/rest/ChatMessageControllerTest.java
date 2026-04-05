package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.repository.ConversationRepository;
import com.theblood.springfood.chat.repository.MessageRepository;
import com.theblood.springfood.chat.service.ChatMetricsService;
import com.theblood.springfood.chat.service.ConversationService;
import com.theblood.springfood.chat.service.TypingIndicatorService;
import com.theblood.springfood.chat.service.dto.ChatMessageEvent;
import com.theblood.springfood.chat.service.dto.ReadReceiptEvent;
import com.theblood.springfood.chat.service.dto.ReadReceiptRequest;
import com.theblood.springfood.chat.service.dto.SendMessageRequest;
import com.theblood.springfood.chat.service.dto.TypingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ChatMessageController}.
 * <p>
 * Tests specific examples and edge cases for message resources operations.
 * Validates authorization, validation, and error handling.
 * <p>
 * Requirements: 3.1, 3.6, 7.2, 9.2
 */
@ExtendWith(MockitoExtension.class)
class ChatMessageControllerTest {

    private static final String TEST_USER_ID = "test-user-123";
    private static final String TEST_CONVERSATION_ID = "conv-123";
    private static final String NON_PARTICIPANT_USER_ID = "non-participant-456";
    @Mock
    private KafkaTemplate<String, ChatMessageEvent> chatMessageKafkaTemplate;
    @Mock
    private KafkaTemplate<String, ReadReceiptEvent> readReceiptKafkaTemplate;
    @Mock
    private ConversationService conversationService;
    @Mock
    private TypingIndicatorService typingIndicatorService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ChatMetricsService metricsService;
    private ChatMessageController chatMessageController;

    @BeforeEach
    void setUp() {
        chatMessageController = new ChatMessageController(
            chatMessageKafkaTemplate,
            readReceiptKafkaTemplate,
            conversationService,
            typingIndicatorService,
            messagingTemplate,
            messageRepository,
            metricsService
        );
    }

    /**
     * Test: sendMessage for non-participant returns authorization error
     * <p>
     * Validates: Requirement 3.1 - Validate user is participant before processing
     * <p>
     * Given: A user who is NOT a participant of a conversation
     * When: The user attempts to send a message to that conversation
     * Then: The system should reject the message and send an authorization error
     */
    @Test
    void sendMessage_whenUserIsNotParticipant_shouldReturnAuthorizationError() {
        // Given: User is not a participant
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(TEST_CONVERSATION_ID);
        request.setContent("Hello, this should fail");
        request.setClientMessageId(UUID.randomUUID().toString());

        Principal principal = new UsernamePasswordAuthenticationToken(NON_PARTICIPANT_USER_ID, null);

        // Mock: User is not a participant
        when(conversationService.isUserParticipant(TEST_CONVERSATION_ID, NON_PARTICIPANT_USER_ID))
            .thenReturn(false);

        // When: User attempts to send message
        chatMessageController.sendMessage(request, principal);

        // Then: Message should NOT be published to Kafka
        verify(chatMessageKafkaTemplate, never()).send(anyString(), anyString(), any(ChatMessageEvent.class));

        // And: Error should be sent to user
        ArgumentCaptor<Object> errorCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(
            eq(NON_PARTICIPANT_USER_ID),
            eq("/queue/errors"),
            errorCaptor.capture()
        );

        // Verify error message contains "FORBIDDEN"
        Object errorResponse = errorCaptor.getValue();
        assertThat(errorResponse.toString()).contains("FORBIDDEN");
        assertThat(errorResponse.toString()).contains("not a participant");
    }

    /**
     * Test: sendMessage with empty content returns validation error
     * <p>
     * Validates: Requirement 3.6 - Validate message content before processing
     * <p>
     * Given: A valid participant
     * When: The user attempts to send a message with null or empty content
     * Then: The system should reject the message due to validation constraints
     * <p>
     * Note: The @Valid annotation on SendMessageRequest triggers Bean Validation.
     * In a real WebSocket environment, Spring would reject this before reaching the resources.
     * This test verifies the DTO validation constraints are properly defined.
     */
    @Test
    void sendMessage_whenContentIsEmpty_shouldFailValidation() {
        // Given: User is a valid participant
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(TEST_CONVERSATION_ID);
        request.setContent(null); // Invalid: content is required
        request.setClientMessageId(UUID.randomUUID().toString());

        // When/Then: Verify validation would fail
        // In integration tests, Spring would reject this request before it reaches the resources
        // For unit tests, we verify the validation logic using Jakarta Validation API
        var violations = jakarta.validation.Validation.buildDefaultValidatorFactory()
            .getValidator()
            .validate(request);

        assertThat(violations)
            .as("SendMessageRequest should have validation errors for null content")
            .isNotEmpty()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content"));
    }

    /**
     * Test: sendMessage with valid data publishes to Kafka
     * <p>
     * Validates: Requirement 3.2, 3.4 - Generate messageId and publish to Kafka
     * <p>
     * Given: A valid participant with valid message content
     * When: The user sends a message
     * Then: The system should publish the message to Kafka with conversationId as partition key
     */
    @Test
    void sendMessage_whenValidRequest_shouldPublishToKafka() {
        // Given: Valid participant and message
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(TEST_CONVERSATION_ID);
        request.setContent("Hello, World!");
        request.setClientMessageId(UUID.randomUUID().toString());

        Principal principal = new UsernamePasswordAuthenticationToken(TEST_USER_ID, null);

        when(conversationService.isUserParticipant(TEST_CONVERSATION_ID, TEST_USER_ID))
            .thenReturn(true);

        // Mock Kafka send success
        CompletableFuture<SendResult<String, ChatMessageEvent>> future = CompletableFuture.completedFuture(null);
        when(chatMessageKafkaTemplate.send(anyString(), anyString(), any(ChatMessageEvent.class)))
            .thenReturn(future);

        // When: User sends message
        chatMessageController.sendMessage(request, principal);

        // Then: Message should be published to Kafka
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ChatMessageEvent> eventCaptor = ArgumentCaptor.forClass(ChatMessageEvent.class);

        verify(chatMessageKafkaTemplate).send(
            topicCaptor.capture(),
            keyCaptor.capture(),
            eventCaptor.capture()
        );

        // Verify Kafka topic
        assertThat(topicCaptor.getValue()).isEqualTo("chat-messages");

        // Verify partition key is conversationId (Requirement 3.4)
        assertThat(keyCaptor.getValue()).isEqualTo(TEST_CONVERSATION_ID);

        // Verify event data
        ChatMessageEvent event = eventCaptor.getValue();
        assertThat(event.getConversationId()).isEqualTo(TEST_CONVERSATION_ID);
        assertThat(event.getSenderId()).isEqualTo(TEST_USER_ID);
        assertThat(event.getContent()).isEqualTo("Hello, World!");
        assertThat(event.getClientMessageId()).isEqualTo(request.getClientMessageId());
        assertThat(event.getMessageId()).isNotNull();
        assertThat(event.getStatus()).isEqualTo("SENDING");

        // Verify typing indicator is cleared (Requirement 9.6)
        verify(typingIndicatorService).stopTyping(TEST_CONVERSATION_ID, TEST_USER_ID);
    }

    /**
     * Test: typing indicator for non-participant returns authorization error
     * <p>
     * Validates: Requirement 9.2 - Validate user is participant before processing typing indicator
     * <p>
     * Given: A user who is NOT a participant of a conversation
     * When: The user attempts to send a typing indicator for that conversation
     * Then: The system should reject the request and send an authorization error
     */
    @Test
    void handleTyping_whenUserIsNotParticipant_shouldReturnAuthorizationError() {
        // Given: User is not a participant
        TypingRequest request = new TypingRequest();
        request.setConversationId(TEST_CONVERSATION_ID);
        request.setIsTyping(true);

        Principal principal = new UsernamePasswordAuthenticationToken(NON_PARTICIPANT_USER_ID, null);

        // Mock: User is not a participant
        when(conversationService.isUserParticipant(TEST_CONVERSATION_ID, NON_PARTICIPANT_USER_ID))
            .thenReturn(false);

        // When: User attempts to send typing indicator
        chatMessageController.handleTyping(request, principal);

        // Then: Typing indicator should NOT be processed
        verify(typingIndicatorService, never()).startTyping(anyString(), anyString(), anyString());
        verify(typingIndicatorService, never()).stopTyping(anyString(), anyString());

        // And: Error should be sent to user
        ArgumentCaptor<Object> errorCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(
            eq(NON_PARTICIPANT_USER_ID),
            eq("/queue/errors"),
            errorCaptor.capture()
        );

        // Verify error message contains "FORBIDDEN"
        Object errorResponse = errorCaptor.getValue();
        assertThat(errorResponse.toString()).contains("FORBIDDEN");
        assertThat(errorResponse.toString()).contains("not a participant");
    }

    /**
     * Test: typing indicator for valid participant updates Redis
     * <p>
     * Validates: Requirement 9.3 - Update Redis with typing state
     * <p>
     * Given: A valid participant
     * When: The user sends a typing indicator
     * Then: The system should update Redis with the typing state
     */
    @Test
    void handleTyping_whenValidParticipant_shouldUpdateRedis() {
        // Given: Valid participant
        TypingRequest request = new TypingRequest();
        request.setConversationId(TEST_CONVERSATION_ID);
        request.setIsTyping(true);

        Principal principal = new UsernamePasswordAuthenticationToken(TEST_USER_ID, null);

        when(conversationService.isUserParticipant(TEST_CONVERSATION_ID, TEST_USER_ID))
            .thenReturn(true);

        // When: User sends typing indicator
        chatMessageController.handleTyping(request, principal);

        // Then: Typing indicator should be started
        verify(typingIndicatorService).startTyping(TEST_CONVERSATION_ID, TEST_USER_ID, null);

        // And: No error should be sent
        verify(messagingTemplate, never()).convertAndSendToUser(
            eq(TEST_USER_ID),
            eq("/queue/errors"),
            any()
        );
    }

    /**
     * Test: read receipt for non-existent message returns validation error
     * <p>
     * Validates: Requirement 7.2 - Validate message exists before processing read receipt
     * <p>
     * Given: A valid participant
     * When: The user attempts to mark a non-existent message as read
     * Then: The system should reject the request
     * <p>
     * Note: The actual validation of message existence happens in the Kafka consumer
     * (ReadReceiptConsumer), not in the resources. The resources only validates
     * that the user is a participant. This test verifies the participant check.
     */
    @Test
    void markAsRead_whenUserIsNotParticipant_shouldReturnAuthorizationError() {
        // Given: User is not a participant
        ReadReceiptRequest request = new ReadReceiptRequest();
        request.setConversationId(TEST_CONVERSATION_ID);
        request.setLastReadMessageId("non-existent-message-id");

        Principal principal = new UsernamePasswordAuthenticationToken(NON_PARTICIPANT_USER_ID, null);

        // Mock: User is not a participant
        when(conversationService.isUserParticipant(TEST_CONVERSATION_ID, NON_PARTICIPANT_USER_ID))
            .thenReturn(false);

        // When: User attempts to mark message as read
        chatMessageController.markAsRead(request, principal);

        // Then: Read receipt should NOT be published to Kafka
        verify(readReceiptKafkaTemplate, never()).send(anyString(), anyString(), any(ReadReceiptEvent.class));

        // And: Error should be sent to user
        ArgumentCaptor<Object> errorCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(
            eq(NON_PARTICIPANT_USER_ID),
            eq("/queue/errors"),
            errorCaptor.capture()
        );

        // Verify error message contains "FORBIDDEN"
        Object errorResponse = errorCaptor.getValue();
        assertThat(errorResponse.toString()).contains("FORBIDDEN");
        assertThat(errorResponse.toString()).contains("not a participant");
    }

    /**
     * Test: read receipt for valid participant publishes to Kafka
     * <p>
     * Validates: Requirement 7.3 - Publish read receipt to Kafka
     * <p>
     * Given: A valid participant with a valid message ID
     * When: The user marks messages as read
     * Then: The system should publish the read receipt to Kafka
     */
    @Test
    void markAsRead_whenValidRequest_shouldPublishToKafka() {
        // Given: Valid participant and message
        String messageId = UUID.randomUUID().toString();
        ReadReceiptRequest request = new ReadReceiptRequest();
        request.setConversationId(TEST_CONVERSATION_ID);
        request.setLastReadMessageId(messageId);

        Principal principal = new UsernamePasswordAuthenticationToken(TEST_USER_ID, null);

        when(conversationService.isUserParticipant(TEST_CONVERSATION_ID, TEST_USER_ID))
            .thenReturn(true);

        // Mock Kafka send success
        CompletableFuture<SendResult<String, ReadReceiptEvent>> future = CompletableFuture.completedFuture(null);
        when(readReceiptKafkaTemplate.send(anyString(), anyString(), any(ReadReceiptEvent.class)))
            .thenReturn(future);

        // When: User marks message as read
        chatMessageController.markAsRead(request, principal);

        // Then: Read receipt should be published to Kafka
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ReadReceiptEvent> eventCaptor = ArgumentCaptor.forClass(ReadReceiptEvent.class);

        verify(readReceiptKafkaTemplate).send(
            topicCaptor.capture(),
            keyCaptor.capture(),
            eventCaptor.capture()
        );

        // Verify Kafka topic
        assertThat(topicCaptor.getValue()).isEqualTo("chat-read-receipts");

        // Verify partition key is conversationId
        assertThat(keyCaptor.getValue()).isEqualTo(TEST_CONVERSATION_ID);

        // Verify event data
        ReadReceiptEvent event = eventCaptor.getValue();
        assertThat(event.getConversationId()).isEqualTo(TEST_CONVERSATION_ID);
        assertThat(event.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(event.getLastReadMessageId()).isEqualTo(messageId);
        assertThat(event.getReadAt()).isNotNull();
    }


    /**
     * Test: reply to message in different conversation rejected
     * <p>
     * Validates: Requirement 10.1 - Validate referenced message is in same conversation
     * <p>
     * Given: A valid participant attempting to reply to a message from a different conversation
     * When: The user sends a message with replyToMessageId from another conversation
     * Then: The system should reject the message with a validation error
     */
    @Test
    void sendMessage_whenReplyToDifferentConversation_shouldReturnValidationError() {
        // Given: User is a participant of conversation A
        String conversationA = "conv-A";
        String conversationB = "conv-B";
        String messageInConversationB = "msg-in-conv-B";

        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(conversationA);
        request.setContent("Reply to message from different conversation");
        request.setClientMessageId(UUID.randomUUID().toString());
        request.setReplyToMessageId(messageInConversationB);

        Principal principal = new UsernamePasswordAuthenticationToken(TEST_USER_ID, null);

        // Mock: User is a participant of conversation A
        when(conversationService.isUserParticipant(conversationA, TEST_USER_ID))
            .thenReturn(true);

        // Mock: Reply message exists but in different conversation
        Message replyMessage = new Message();
        replyMessage.setMessageId(messageInConversationB);
        replyMessage.setContent("Original message");
        Conversation conversationBEntity = new Conversation();
        conversationBEntity.setConversationId(conversationB);
        replyMessage.setConversation(conversationBEntity);

        when(messageRepository.findById(messageInConversationB))
            .thenReturn(Optional.of(replyMessage));

        // When: User attempts to send reply
        chatMessageController.sendMessage(request, principal);

        // Then: Message should NOT be published to Kafka
        verify(chatMessageKafkaTemplate, never()).send(anyString(), anyString(), any(ChatMessageEvent.class));

        // And: Error should be sent to user
        ArgumentCaptor<Object> errorCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(
            eq(TEST_USER_ID),
            eq("/queue/errors"),
            errorCaptor.capture()
        );

        // Verify error message
        Object errorResponse = errorCaptor.getValue();
        assertThat(errorResponse.toString()).contains("VALIDATION_ERROR");
        assertThat(errorResponse.toString()).contains("different conversation");
    }

    /**
     * Test: reply to non-existent message rejected
     * <p>
     * Validates: Requirement 10.5 - Reject messages with invalid replyToMessageId
     * <p>
     * Given: A valid participant attempting to reply to a non-existent message
     * When: The user sends a message with replyToMessageId that doesn't exist
     * Then: The system should reject the message with a validation error
     */
    @Test
    void sendMessage_whenReplyToNonExistentMessage_shouldReturnValidationError() {
        // Given: User is a participant
        String nonExistentMessageId = "non-existent-msg-id";

        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(TEST_CONVERSATION_ID);
        request.setContent("Reply to non-existent message");
        request.setClientMessageId(UUID.randomUUID().toString());
        request.setReplyToMessageId(nonExistentMessageId);

        Principal principal = new UsernamePasswordAuthenticationToken(TEST_USER_ID, null);

        // Mock: User is a participant
        when(conversationService.isUserParticipant(TEST_CONVERSATION_ID, TEST_USER_ID))
            .thenReturn(true);

        // Mock: Reply message does not exist
        when(messageRepository.findById(nonExistentMessageId))
            .thenReturn(Optional.empty());

        // When: User attempts to send reply
        chatMessageController.sendMessage(request, principal);

        // Then: Message should NOT be published to Kafka
        verify(chatMessageKafkaTemplate, never()).send(anyString(), anyString(), any(ChatMessageEvent.class));

        // And: Error should be sent to user
        ArgumentCaptor<Object> errorCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(
            eq(TEST_USER_ID),
            eq("/queue/errors"),
            errorCaptor.capture()
        );

        // Verify error message
        Object errorResponse = errorCaptor.getValue();
        assertThat(errorResponse.toString()).contains("VALIDATION_ERROR");
        assertThat(errorResponse.toString()).contains("does not exist");
    }

    /**
     * Test: reply_to_preview truncated to 200 characters
     * <p>
     * Validates: Requirement 10.3 - Cache reply_to_preview (first 200 characters)
     * <p>
     * Given: A valid participant replying to a message with content longer than 200 characters
     * When: The user sends a reply message
     * Then: The system should truncate the reply_to_preview to 200 characters
     */
    @Test
    void sendMessage_whenReplyToLongMessage_shouldTruncatePreview() {
        // Given: User is a participant
        String replyToMessageId = "msg-with-long-content";
        String longContent = "A".repeat(300); // 300 characters

        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(TEST_CONVERSATION_ID);
        request.setContent("Reply to long message");
        request.setClientMessageId(UUID.randomUUID().toString());
        request.setReplyToMessageId(replyToMessageId);

        Principal principal = new UsernamePasswordAuthenticationToken(TEST_USER_ID, null);

        // Mock: User is a participant
        when(conversationService.isUserParticipant(TEST_CONVERSATION_ID, TEST_USER_ID))
            .thenReturn(true);

        // Mock: Reply message exists with long content
        Message replyMessage = new Message();
        replyMessage.setMessageId(replyToMessageId);
        replyMessage.setContent(longContent);
        Conversation conversation = new Conversation();
        conversation.setConversationId(TEST_CONVERSATION_ID);
        replyMessage.setConversation(conversation);

        when(messageRepository.findById(replyToMessageId))
            .thenReturn(Optional.of(replyMessage));

        // Mock Kafka send success
        CompletableFuture<SendResult<String, ChatMessageEvent>> future = CompletableFuture.completedFuture(null);
        when(chatMessageKafkaTemplate.send(anyString(), anyString(), any(ChatMessageEvent.class)))
            .thenReturn(future);

        // When: User sends reply
        chatMessageController.sendMessage(request, principal);

        // Then: Message should be published to Kafka with truncated preview
        ArgumentCaptor<ChatMessageEvent> eventCaptor = ArgumentCaptor.forClass(ChatMessageEvent.class);
        verify(chatMessageKafkaTemplate).send(
            eq("chat-messages"),
            eq(TEST_CONVERSATION_ID),
            eventCaptor.capture()
        );

        // Verify reply_to_preview is truncated to 200 characters
        ChatMessageEvent event = eventCaptor.getValue();
        assertThat(event.getReplyToMessageId()).isEqualTo(replyToMessageId);
        assertThat(event.getReplyToPreview()).isNotNull();
        assertThat(event.getReplyToPreview()).hasSize(200);
        assertThat(event.getReplyToPreview()).isEqualTo(longContent.substring(0, 200));
    }

}
