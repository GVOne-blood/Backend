package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.IntegrationTest;
import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.repository.ConversationRepository;
import com.theblood.springfood.chat.repository.MessageRepository;
import com.theblood.springfood.chat.service.ChatMetricsService;
import com.theblood.springfood.chat.service.ConversationService;
import com.theblood.springfood.chat.service.MessageService;
import com.theblood.springfood.chat.service.TypingIndicatorService;
import com.theblood.springfood.chat.service.dto.ReadReceiptRequest;
import com.theblood.springfood.chat.service.dto.SendMessageRequest;
import com.theblood.springfood.chat.service.dto.TypingRequest;
import com.theblood.springfood.chat.web.rest.ChatMessageController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for {@link ChatMessageController}.
 * 
 * Tests universal correctness properties across randomized inputs.
 * Simulates property-based testing with 100 iterations using randomized data.
 */
@IntegrationTest
@Transactional
class ChatMessageControllerPropertyTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private TypingIndicatorService typingIndicatorService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMetricsService metricsService;

    private ChatMessageController chatMessageController;

    @BeforeEach
    void setUp() {
        chatMessageController = new ChatMessageController(
            (KafkaTemplate) kafkaTemplate,
            (KafkaTemplate) kafkaTemplate,
            conversationService,
            typingIndicatorService,
            messagingTemplate,
            messageRepository,
            metricsService
        );
    }

    /**
     * Property 8: Participant Authorization
     * 
     * For any operation requiring conversation access (sending messages, retrieving history,
     * adding reactions, sending typing indicators), the system should allow the operation
     * only if the user is an ACTIVE participant of the conversation, otherwise return
     * authorization error.
     * 
     * Validates: Requirements 2.6, 3.1, 6.1, 7.2, 8.1, 9.2
     * 
     * This test simulates property-based testing by running 100 iterations with randomized data.
     */
    @Test
    @Tag("Feature: chat-realtime-core, Property 8: Participant Authorization")
    void participantAuthorizationProperty() {
        Random random = new Random();
        String[] conversationTypes = {"DIRECT", "GROUP", "ORDER_SUPPORT", "SHOP_SUPPORT"};
        String[] participantStatuses = {"ACTIVE", "LEFT", "REMOVED", "MUTED"};
        OperationType[] operationTypes = OperationType.values();
        
        // Run 100 iterations with randomized data (simulating property-based testing)
        for (int iteration = 0; iteration < 100; iteration++) {
            // Generate random test data
            String conversationType = conversationTypes[random.nextInt(conversationTypes.length)];
            String participantStatus = participantStatuses[random.nextInt(participantStatuses.length)];
            OperationType operationType = operationTypes[random.nextInt(operationTypes.length)];
            String userId = "user-" + UUID.randomUUID().toString().substring(0, 8);
            String content = "Test message " + random.nextInt(1000);
            
            // Execute the property test
            testParticipantAuthorization(conversationType, participantStatus, operationType, userId, content);
        }
    }
    
    /**
     * Test a single iteration of the participant authorization property.
     */
    private void testParticipantAuthorization(
        String conversationType,
        String participantStatus,
        OperationType operationType,
        String userId,
        String content
    ) {
        // Given: Create a conversation with participants
        Conversation conversation = createTestConversation(conversationType);
        
        // Create a participant with the given status
        ConversationParticipant participant = createTestParticipant(conversation, userId, participantStatus);
        
        // Create a non-participant user for testing
        String nonParticipantUserId = "non-participant-" + UUID.randomUUID().toString();
        
        // When: User attempts to perform an operation
        boolean isActiveParticipant = "ACTIVE".equals(participantStatus);
        
        // Test with participant
        boolean participantOperationSucceeded = performOperation(
            operationType, 
            conversation.getConversationId(), 
            userId, 
            content
        );
        
        // Test with non-participant
        boolean nonParticipantOperationSucceeded = performOperation(
            operationType,
            conversation.getConversationId(),
            nonParticipantUserId,
            content
        );
        
        // Then: Operation should succeed only for ACTIVE participants
        if (isActiveParticipant) {
            assertThat(participantOperationSucceeded)
                .as("ACTIVE participant should be able to perform %s operation", operationType)
                .isTrue();
        } else {
            assertThat(participantOperationSucceeded)
                .as("Non-ACTIVE participant (status=%s) should NOT be able to perform %s operation", 
                    participantStatus, operationType)
                .isFalse();
        }
        
        // Non-participants should always be rejected
        assertThat(nonParticipantOperationSucceeded)
            .as("Non-participant should NOT be able to perform %s operation", operationType)
            .isFalse();
        
        // Cleanup
        cleanupTestData(conversation);
    }

    /**
     * Perform the specified operation and return whether it succeeded.
     * Returns true if operation completed without throwing an exception.
     */
    private boolean performOperation(
        OperationType operationType,
        String conversationId,
        String userId,
        String content
    ) {
        try {
            Principal principal = new UsernamePasswordAuthenticationToken(userId, null);
            
            switch (operationType) {
                case SEND_MESSAGE:
                    SendMessageRequest sendRequest = new SendMessageRequest();
                    sendRequest.setConversationId(conversationId);
                    sendRequest.setContent(content);
                    sendRequest.setClientMessageId(UUID.randomUUID().toString());
                    chatMessageController.sendMessage(sendRequest, principal);
                    return true;
                    
                case TYPING_INDICATOR:
                    TypingRequest typingRequest = new TypingRequest();
                    typingRequest.setConversationId(conversationId);
                    typingRequest.setIsTyping(true);
                    chatMessageController.handleTyping(typingRequest, principal);
                    return true;
                    
                case READ_RECEIPT:
                    // Create a message first for read receipt
                    Message message = createTestMessage(conversationId, "test-sender");
                    ReadReceiptRequest readRequest = new ReadReceiptRequest();
                    readRequest.setConversationId(conversationId);
                    readRequest.setLastReadMessageId(message.getMessageId());
                    chatMessageController.markAsRead(readRequest, principal);
                    return true;
                    
                case GET_MESSAGE_HISTORY:
                    messageService.getMessageHistory(conversationId, userId, PageRequest.of(0, 20));
                    return true;
                    
                case ADD_REACTION:
                    // Create a message first for reaction
                    Message msgForReaction = createTestMessage(conversationId, "test-sender");
                    messageService.addReaction(msgForReaction.getMessageId(), userId, "thumbs_up");
                    return true;
                    
                case GET_CONVERSATION_DETAILS:
                    conversationService.getConversationById(conversationId, userId);
                    return true;
                    
                default:
                    throw new IllegalArgumentException("Unknown operation type: " + operationType);
            }
        } catch (IllegalArgumentException e) {
            // Authorization/validation errors indicate operation was rejected
            return false;
        } catch (Exception e) {
            // Other exceptions (e.g., Kafka errors) should not be treated as authorization failures
            // For this test, we'll treat them as failures to be conservative
            return false;
        }
    }

    /**
     * Create a test conversation with the given type.
     */
    private Conversation createTestConversation(String conversationType) {
        Conversation conversation = new Conversation();
        conversation.setConversationType(conversationType);
        conversation.setName("Test Conversation " + UUID.randomUUID());
        conversation.setMessageCount(0L);
        conversation.setIsArchived(0);
        conversation.setIsPinned(0);
        return conversationRepository.save(conversation);
    }

    /**
     * Create a test participant with the given status.
     */
    private ConversationParticipant createTestParticipant(
        Conversation conversation,
        String userId,
        String status
    ) {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setConversation(conversation);
        participant.setUserId(userId);
        participant.setStatus(status);
        participant.setRole("MEMBER");
        participant.setUnreadCount(0);
        participant.setIsMuted(0);
        participant.setIsPinned(0);
        participant.setJoinedAt(Instant.now());
        participant.setAddedBy("system");
        return participantRepository.save(participant);
    }

    /**
     * Create a test message for read receipt and reaction tests.
     */
    private Message createTestMessage(String conversationId, String senderId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        
        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderId(senderId);
        message.setContent("Test message");
        message.setMessageType("TEXT");
        message.setStatus("SENT");
        message.setIsDeleted(0);
        message.setIsEdited(0);
        message.setReactionCount(0);
        return messageRepository.save(message);
    }

    /**
     * Cleanup test data after each property test.
     */
    private void cleanupTestData(Conversation conversation) {
        try {
            // Delete messages first (foreign key constraint)
            messageRepository.deleteAll(
                messageRepository.findAll().stream()
                    .filter(m -> m.getConversation().getConversationId().equals(conversation.getConversationId()))
                    .toList()
            );
            
            // Delete participants
            participantRepository.deleteAll(
                participantRepository.findAll().stream()
                    .filter(p -> p.getConversation().getConversationId().equals(conversation.getConversationId()))
                    .toList()
            );
            
            // Delete conversation
            conversationRepository.delete(conversation);
        } catch (Exception e) {
            // Ignore cleanup errors in tests
        }
    }

    /**
     * Enum representing different operation types that require participant authorization.
     */
    enum OperationType {
        SEND_MESSAGE,           // Requirement 3.1
        TYPING_INDICATOR,       // Requirement 9.2
        READ_RECEIPT,           // Requirement 7.2
        GET_MESSAGE_HISTORY,    // Requirement 6.1
        ADD_REACTION,           // Requirement 8.1
        GET_CONVERSATION_DETAILS // Requirement 2.6
    }
}
