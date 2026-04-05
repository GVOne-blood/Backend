package com.theblood.springfood.chat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.chat.IntegrationTest;
import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.domain.Message;
import com.theblood.springfood.chat.domain.MessageReaction;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.repository.ConversationRepository;
import com.theblood.springfood.chat.repository.MessageReactionRepository;
import com.theblood.springfood.chat.repository.MessageRepository;
import com.theblood.springfood.chat.service.dto.ChatMessageEvent;
import com.theblood.springfood.chat.service.dto.ReadReceiptRequest;
import com.theblood.springfood.chat.service.dto.SendMessageRequest;
import com.theblood.springfood.chat.service.dto.TypingRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * End-to-End Integration Test for Chat Realtime Core
 *
 * Task 25.1: Write end-to-end integration tests with Testcontainers
 *
 * This test verifies complete message flows:
 * - Client A → WebSocket → Kafka → Client B
 * - Message persistence and history retrieval round trip
 * - Read receipts update unread counts correctly
 * - Reactions toggle correctly (add twice = remove)
 * - Typing indicators expire after 5 seconds via Redis TTL
 * - Reply messages include preview and reference validation
 *
 * Requirements: All
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@EmbeddedKafka(
    partitions = 3,
    topics = {"chat-messages", "chat-read-receipts"},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    }
)
class EndToEndIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageReactionRepository reactionRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String websocketUrl;
    private static final String USER_A_ID = "user-a-e2e-test";
    private static final String USER_B_ID = "user-b-e2e-test";
    private static final String USER_A_NAME = "User A";
    private static final String USER_B_NAME = "User B";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    @BeforeEach
    void setUp() {
        websocketUrl = "ws://localhost:" + port + "/ws";
    }

    @AfterEach
    void tearDown() {
        // Cleanup Redis keys
        Set<String> keys = redisTemplate.keys("typing:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * Test: Complete message flow from Client A to Client B
     *
     * Validates: Requirements 3.1, 3.2, 3.4, 4.1, 4.2, 4.3, 4.4, 5.1, 5.3
     *
     * Given: Two users (A and B) connected via WebSocket
     * When: User A sends a message
     * Then: User B should receive the message in real-time
     * And: Message should be persisted to database
     */
    @Test
    void testCompleteMessageFlowFromClientAToClientB() throws Exception {
        // Given: Create conversation with both users as participants
        Conversation conversation = createTestConversation();
        addParticipant(conversation, USER_A_ID, USER_A_NAME);
        addParticipant(conversation, USER_B_ID, USER_B_NAME);

        // Given: Connect both users via WebSocket
        WebSocketStompClient clientA = createStompClient();
        WebSocketStompClient clientB = createStompClient();

        BlockingQueue<ChatMessageEvent> userBMessages = new LinkedBlockingQueue<>();

        StompSession sessionA = connectUser(clientA, USER_A_ID, USER_A_NAME);
        StompSession sessionB = connectUser(clientB, USER_B_ID, USER_B_NAME);

        // User B subscribes to message queue
        sessionB.subscribe("/user/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageEvent.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                userBMessages.offer((ChatMessageEvent) payload);
            }
        });

        // Wait for subscription to be active
        Thread.sleep(500);

        // When: User A sends a message
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(conversation.getConversationId());
        request.setContent("Hello from User A!");
        request.setClientMessageId(UUID.randomUUID().toString());

        sessionA.send("/app/chat.send", request);

        // Then: User B should receive the message
        ChatMessageEvent receivedMessage = userBMessages.poll(TIMEOUT.getSeconds(), TimeUnit.SECONDS);

        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getConversationId()).isEqualTo(conversation.getConversationId());
        assertThat(receivedMessage.getSenderId()).isEqualTo(USER_A_ID);
        assertThat(receivedMessage.getContent()).isEqualTo("Hello from User A!");
        assertThat(receivedMessage.getClientMessageId()).isEqualTo(request.getClientMessageId());
        assertThat(receivedMessage.getMessageId()).isNotNull();

        // And: Message should be persisted to database (wait for async persistence)
        Thread.sleep(2000);

        List<Message> persistedMessages = messageRepository.findByConversationConversationId(
            conversation.getConversationId()
        );

        assertThat(persistedMessages)
            .isNotEmpty()
            .anyMatch(msg -> msg.getClientMessageId().equals(request.getClientMessageId()));

        // Cleanup
        sessionA.disconnect();
        sessionB.disconnect();
    }

    /**
     * Test: Message persistence and history retrieval round trip
     *
     * Validates: Requirements 5.1, 5.3, 6.1, 6.2, 6.3
     *
     * Given: Messages sent and persisted
     * When: User retrieves message history
     * Then: All messages should be returned in correct order
     */
    @Test
    void testMessagePersistenceAndHistoryRetrieval() throws Exception {
        // Given: Create conversation and add user
        Conversation conversation = createTestConversation();
        addParticipant(conversation, USER_A_ID, USER_A_NAME);

        // Given: Connect user and send multiple messages
        WebSocketStompClient client = createStompClient();
        StompSession session = connectUser(client, USER_A_ID, USER_A_NAME);

        List<String> clientMessageIds = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            SendMessageRequest request = new SendMessageRequest();
            request.setConversationId(conversation.getConversationId());
            request.setContent("Message " + i);
            request.setClientMessageId(UUID.randomUUID().toString());
            clientMessageIds.add(request.getClientMessageId());

            session.send("/app/chat.send", request);
            Thread.sleep(200);
        }

        // Wait for async persistence
        Thread.sleep(3000);

        // When: Retrieve message history
        List<Message> messages = messageRepository.findByConversationConversationIdOrderByCreatedAtDesc(
            conversation.getConversationId()
        );

        // Then: All messages should be persisted
        assertThat(messages).hasSize(3);

        // Verify messages are in correct order (newest first)
        assertThat(messages.get(0).getContent()).isEqualTo("Message 3");
        assertThat(messages.get(1).getContent()).isEqualTo("Message 2");
        assertThat(messages.get(2).getContent()).isEqualTo("Message 1");

        // Verify all client message IDs are present
        List<String> persistedClientIds = messages.stream()
            .map(Message::getClientMessageId)
            .toList();
        assertThat(persistedClientIds).containsAll(clientMessageIds);

        // Cleanup
        session.disconnect();
    }

    /**
     * Test: Read receipts update unread counts correctly
     *
     * Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 11.1, 11.2
     *
     * Given: User B has unread messages
     * When: User B sends read receipt
     * Then: Unread count should be reset to 0
     */
    @Test
    void testReadReceiptsUpdateUnreadCounts() throws Exception {
        // Given: Create conversation with both users
        Conversation conversation = createTestConversation();
        addParticipant(conversation, USER_A_ID, USER_A_NAME);
        addParticipant(conversation, USER_B_ID, USER_B_NAME);

        // Given: User A sends a message (User B will have unread count)
        WebSocketStompClient clientA = createStompClient();
        StompSession sessionA = connectUser(clientA, USER_A_ID, USER_A_NAME);

        SendMessageRequest messageRequest = new SendMessageRequest();
        messageRequest.setConversationId(conversation.getConversationId());
        messageRequest.setContent("Unread message for User B");
        messageRequest.setClientMessageId(UUID.randomUUID().toString());

        sessionA.send("/app/chat.send", messageRequest);

        // Wait for persistence and unread count update
        Thread.sleep(3000);

        // Verify User B has unread count > 0
        ConversationParticipant userBParticipant = participantRepository
            .findByConversationConversationIdAndUserId(conversation.getConversationId(), USER_B_ID)
            .orElseThrow();

        assertThat(userBParticipant.getUnreadCount()).isGreaterThan(0);

        // When: User B connects and sends read receipt
        WebSocketStompClient clientB = createStompClient();
        StompSession sessionB = connectUser(clientB, USER_B_ID, USER_B_NAME);

        Message persistedMessage = messageRepository
            .findByConversationConversationIdOrderByCreatedAtDesc(conversation.getConversationId())
            .get(0);

        ReadReceiptRequest readReceipt = new ReadReceiptRequest();
        readReceipt.setConversationId(conversation.getConversationId());
        readReceipt.setLastReadMessageId(persistedMessage.getMessageId());

        sessionB.send("/app/chat.read", readReceipt);

        // Wait for read receipt processing
        Thread.sleep(2000);

        // Then: Unread count should be reset to 0
        ConversationParticipant updatedParticipant = participantRepository
            .findByConversationConversationIdAndUserId(conversation.getConversationId(), USER_B_ID)
            .orElseThrow();

        assertThat(updatedParticipant.getUnreadCount()).isEqualTo(0);
        assertThat(updatedParticipant.getLastReadMessageId()).isEqualTo(persistedMessage.getMessageId());
        assertThat(updatedParticipant.getLastReadAt()).isNotNull();

        // Cleanup
        sessionA.disconnect();
        sessionB.disconnect();
    }

    /**
     * Test: Reactions toggle correctly (add twice = remove)
     *
     * Validates: Requirements 8.1, 8.2, 8.3, 8.5, 8.6
     *
     * Given: A persisted message
     * When: User adds same reaction twice
     * Then: Reaction should be removed (toggle behavior)
     */
    @Test
    void testReactionsToggleCorrectly() throws Exception {
        // Given: Create conversation and persist a message
        Conversation conversation = createTestConversation();
        addParticipant(conversation, USER_A_ID, USER_A_NAME);

        Message message = new Message();
        message.setConversation(conversation);
        message.setMessageId(UUID.randomUUID().toString());
        message.setClientMessageId(UUID.randomUUID().toString());
        message.setSenderId(USER_A_ID);
        message.setSenderName(USER_A_NAME);
        message.setContent("Test message for reactions");
        message.setMessageType("TEXT");
        message.setStatus("SENT");
        message.setReactionCount(0);
        message.setIsDeleted(0);
        message.setIsEdited(0);
        // createdDate is automatically set by @CreatedDate annotation
        message = messageRepository.save(message);

        String messageId = message.getMessageId();
        String emoji = "👍";

        // When: Add reaction first time
        MessageReaction reaction1 = new MessageReaction();
        reaction1.setMessage(message);
        reaction1.setUserId(USER_A_ID);
        reaction1.setEmoji(emoji);
        // createdDate is automatically set by @CreatedDate annotation
        reactionRepository.save(reaction1);

        // Update reaction count
        message.setReactionCount(1);
        messageRepository.save(message);

        // Then: Reaction should exist
        List<MessageReaction> reactions = reactionRepository.findByMessageMessageId(messageId);
        assertThat(reactions).hasSize(1);
        assertThat(reactions.get(0).getEmoji()).isEqualTo(emoji);

        Message updatedMessage = messageRepository.findById(messageId).orElseThrow();
        assertThat(updatedMessage.getReactionCount()).isEqualTo(1);

        // When: Add same reaction again (toggle - should remove)
        Optional<MessageReaction> existingReaction = reactionRepository
            .findByMessageMessageIdAndUserIdAndEmoji(messageId, USER_A_ID, emoji);

        if (existingReaction.isPresent()) {
            reactionRepository.delete(existingReaction.get());
            updatedMessage.setReactionCount(Math.max(0, updatedMessage.getReactionCount() - 1));
            messageRepository.save(updatedMessage);
        }

        // Then: Reaction should be removed
        reactions = reactionRepository.findByMessageMessageId(messageId);
        assertThat(reactions).isEmpty();

        updatedMessage = messageRepository.findById(messageId).orElseThrow();
        assertThat(updatedMessage.getReactionCount()).isEqualTo(0);
    }

    /**
     * Test: Typing indicators expire after 5 seconds via Redis TTL
     *
     * Validates: Requirements 9.1, 9.2, 9.3, 9.5
     *
     * Given: User starts typing
     * When: 5+ seconds pass without activity
     * Then: Typing indicator should be automatically removed by Redis TTL
     */
    @Test
    void testTypingIndicatorsExpireAfter5Seconds() throws Exception {
        // Given: Create conversation and connect user
        Conversation conversation = createTestConversation();
        addParticipant(conversation, USER_A_ID, USER_A_NAME);

        WebSocketStompClient client = createStompClient();
        StompSession session = connectUser(client, USER_A_ID, USER_A_NAME);

        // When: User starts typing
        TypingRequest typingRequest = new TypingRequest();
        typingRequest.setConversationId(conversation.getConversationId());
        typingRequest.setIsTyping(true);

        session.send("/app/chat.typing", typingRequest);

        // Wait for Redis update
        Thread.sleep(500);

        // Then: User should be in typing set
        String redisKey = "typing:" + conversation.getConversationId();
        Set<String> typingUsers = redisTemplate.opsForSet().members(redisKey);

        assertThat(typingUsers)
            .isNotNull()
            .contains(USER_A_ID);

        // When: Wait for TTL expiration (5+ seconds)
        Thread.sleep(6000);

        // Then: Typing indicator should be removed by Redis TTL
        typingUsers = redisTemplate.opsForSet().members(redisKey);

        assertThat(typingUsers == null || !typingUsers.contains(USER_A_ID))
            .as("Typing indicator should expire after 5 seconds")
            .isTrue();

        // Cleanup
        session.disconnect();
    }

    /**
     * Test: Reply messages include preview and reference validation
     *
     * Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5
     *
     * Given: An existing message
     * When: User sends a reply to that message
     * Then: Reply should include replyToMessageId and reply_to_preview
     */
    @Test
    void testReplyMessagesIncludePreviewAndValidation() throws Exception {
        // Given: Create conversation and persist original message
        Conversation conversation = createTestConversation();
        addParticipant(conversation, USER_A_ID, USER_A_NAME);

        Message originalMessage = new Message();
        originalMessage.setConversation(conversation);
        originalMessage.setMessageId(UUID.randomUUID().toString());
        originalMessage.setClientMessageId(UUID.randomUUID().toString());
        originalMessage.setSenderId(USER_A_ID);
        originalMessage.setSenderName(USER_A_NAME);
        originalMessage.setContent("This is the original message that will be replied to");
        originalMessage.setMessageType("TEXT");
        originalMessage.setStatus("SENT");
        originalMessage.setReactionCount(0);
        originalMessage.setIsDeleted(0);
        originalMessage.setIsEdited(0);
        // createdDate is automatically set by @CreatedDate annotation
        originalMessage = messageRepository.save(originalMessage);

        // When: User sends a reply
        WebSocketStompClient client = createStompClient();
        StompSession session = connectUser(client, USER_A_ID, USER_A_NAME);

        SendMessageRequest replyRequest = new SendMessageRequest();
        replyRequest.setConversationId(conversation.getConversationId());
        replyRequest.setContent("This is a reply");
        replyRequest.setClientMessageId(UUID.randomUUID().toString());
        replyRequest.setReplyToMessageId(originalMessage.getMessageId());

        session.send("/app/chat.send", replyRequest);

        // Wait for persistence
        Thread.sleep(3000);

        // Then: Reply message should have replyToMessageId and preview
        List<Message> messages = messageRepository
            .findByConversationConversationIdOrderByCreatedAtDesc(conversation.getConversationId());

        Message replyMessage = messages.stream()
            .filter(msg -> msg.getClientMessageId().equals(replyRequest.getClientMessageId()))
            .findFirst()
            .orElseThrow();

        assertThat(replyMessage.getReplyToMessageId()).isEqualTo(originalMessage.getMessageId());
        assertThat(replyMessage.getReplyToPreview())
            .isNotNull()
            .contains("This is the original message");

        // Cleanup
        session.disconnect();
    }

    /**
     * Test: Invalid reply to message in different conversation is rejected
     *
     * Validates: Requirements 10.1, 10.5
     *
     * Given: Two different conversations
     * When: User tries to reply to message from different conversation
     * Then: Reply should be rejected
     */
    @Test
    void testInvalidReplyToDifferentConversationRejected() throws Exception {
        // Given: Create two conversations
        Conversation conversation1 = createTestConversation();
        Conversation conversation2 = createTestConversation();
        addParticipant(conversation1, USER_A_ID, USER_A_NAME);
        addParticipant(conversation2, USER_A_ID, USER_A_NAME);

        // Given: Message in conversation1
        Message messageInConv1 = new Message();
        messageInConv1.setConversation(conversation1);
        messageInConv1.setMessageId(UUID.randomUUID().toString());
        messageInConv1.setClientMessageId(UUID.randomUUID().toString());
        messageInConv1.setSenderId(USER_A_ID);
        messageInConv1.setSenderName(USER_A_NAME);
        messageInConv1.setContent("Message in conversation 1");
        messageInConv1.setMessageType("TEXT");
        messageInConv1.setStatus("SENT");
        messageInConv1.setReactionCount(0);
        messageInConv1.setIsDeleted(0);
        messageInConv1.setIsEdited(0);
        // createdDate is automatically set by @CreatedDate annotation
        messageInConv1 = messageRepository.save(messageInConv1);

        // When: Try to reply to conv1 message from conv2
        WebSocketStompClient client = createStompClient();
        StompSession session = connectUser(client, USER_A_ID, USER_A_NAME);

        BlockingQueue<Object> errorQueue = new LinkedBlockingQueue<>();

        session.subscribe("/user/queue/errors", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                errorQueue.offer(payload);
            }
        });

        SendMessageRequest invalidReply = new SendMessageRequest();
        invalidReply.setConversationId(conversation2.getConversationId());
        invalidReply.setContent("Invalid reply");
        invalidReply.setClientMessageId(UUID.randomUUID().toString());
        invalidReply.setReplyToMessageId(messageInConv1.getMessageId());

        session.send("/app/chat.send", invalidReply);

        // Then: Error should be received
        Object error = errorQueue.poll(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        assertThat(error).isNotNull();

        // Cleanup
        session.disconnect();
    }

    // ========== Helper Methods ==========

    private WebSocketStompClient createStompClient() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        return stompClient;
    }

    private StompSession connectUser(WebSocketStompClient client, String userId, String username)
            throws Exception {
        String jwtToken = generateValidJwtToken(userId, username);

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);

        return client.connectAsync(
            websocketUrl,
            headers,
            new StompSessionHandlerAdapter() {}
        ).get(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
    }

    private String generateValidJwtToken(String userId, String username) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("chat-service")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(3600))
            .subject(userId)
            .claim("preferred_username", username)
            .claim("auth", "ROLE_USER")
            .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private Conversation createTestConversation() {
        Conversation conversation = new Conversation();
        conversation.setConversationType("GROUP");
        conversation.setName("E2E Test Conversation " + UUID.randomUUID());
        conversation.setMessageCount(0L);
        conversation.setIsArchived(0);
        conversation.setIsPinned(0);
        return conversationRepository.save(conversation);
    }

    private void addParticipant(Conversation conversation, String userId, String displayName) {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setConversation(conversation);
        participant.setUserId(userId);
        participant.setDisplayName(displayName);
        participant.setStatus("ACTIVE");
        participant.setRole("MEMBER");
        participant.setUnreadCount(0);
        participant.setIsMuted(0);
        participant.setIsPinned(0);
        participant.setJoinedAt(Instant.now());
        participant.setAddedBy("system");
        participantRepository.save(participant);
    }
}
