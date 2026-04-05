package com.theblood.springfood.chat.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.chat.IntegrationTest;
import com.theblood.springfood.chat.domain.Conversation;
import com.theblood.springfood.chat.domain.ConversationParticipant;
import com.theblood.springfood.chat.repository.ConversationParticipantRepository;
import com.theblood.springfood.chat.repository.ConversationRepository;
import com.theblood.springfood.chat.service.dto.ChatMessageEvent;
import com.theblood.springfood.chat.service.dto.SendMessageRequest;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
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
 * WebSocket Integration Test for Chat Realtime Core
 * 
 * Task 12: Checkpoint - Ensure WebSocket layer works
 * 
 * This test verifies:
 * - WebSocket connection establishment with JWT authentication
 * - Message sending via WebSocket/STOMP
 * - Messages published to Kafka topic
 * - Complete end-to-end flow from client to Kafka
 * 
 * Requirements: 1.2, 1.3, 3.1, 3.2, 3.4, 15.1
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@EmbeddedKafka(
    partitions = 1,
    topics = {"chat-messages", "chat-read-receipts"},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    }
)
class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.kafka.test.EmbeddedKafkaBroker embeddedKafkaBroker;

    private WebSocketStompClient stompClient;
    private Consumer<String, ChatMessageEvent> kafkaConsumer;
    private String websocketUrl;
    private static final String TEST_USER_ID = "test-user-websocket-123";
    private static final String TEST_USERNAME = "testuser";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @BeforeEach
    void setUp() {
        websocketUrl = "ws://localhost:" + port + "/ws";
        
        // Setup WebSocket STOMP client with SockJS
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        
        // Setup Kafka consumer for verification
        setupKafkaConsumer();
    }

    @AfterEach
    void tearDown() {
        if (kafkaConsumer != null) {
            kafkaConsumer.close();
        }
    }

    /**
     * Test: WebSocket connection with valid JWT authentication
     * 
     * Validates: Requirements 1.2, 1.3, 15.1
     * 
     * Given: A valid JWT token
     * When: Client connects to WebSocket endpoint with JWT in Authorization header
     * Then: Connection should be established successfully
     */
    @Test
    void testWebSocketConnectionWithValidJWT() throws Exception {
        // Given: Valid JWT token
        String jwtToken = generateValidJwtToken(TEST_USER_ID, TEST_USERNAME);
        
        // When: Connect to WebSocket with JWT
        BlockingQueue<String> connectionStatus = new LinkedBlockingQueue<>();
        
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        
        StompSession session = stompClient.connectAsync(
            websocketUrl,
            headers,
            new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    connectionStatus.offer("CONNECTED");
                }
                
                @Override
                public void handleException(StompSession session, StompCommand command, 
                                           StompHeaders headers, byte[] payload, Throwable exception) {
                    connectionStatus.offer("ERROR: " + exception.getMessage());
                }
                
                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    connectionStatus.offer("TRANSPORT_ERROR: " + exception.getMessage());
                }
            }
        ).get(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        
        // Then: Connection should be established
        String status = connectionStatus.poll(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        assertThat(status).isEqualTo("CONNECTED");
        assertThat(session.isConnected()).isTrue();
        
        // Cleanup
        session.disconnect();
    }

    /**
     * Test: WebSocket connection with invalid JWT is rejected
     * 
     * Validates: Requirements 1.4
     * 
     * Given: An invalid JWT token
     * When: Client attempts to connect to WebSocket endpoint
     * Then: Connection should be rejected
     */
    @Test
    void testWebSocketConnectionWithInvalidJWT() {
        // Given: Invalid JWT token
        String invalidToken = "invalid.jwt.token";
        
        // When: Attempt to connect with invalid JWT
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Bearer " + invalidToken);
        
        // Then: Connection should fail
        assertThatThrownBy(() -> {
            stompClient.connectAsync(
                websocketUrl,
                headers,
                new StompSessionHandlerAdapter() {}
            ).get(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        }).hasMessageContaining("Authentication");
    }

    /**
     * Test: Send message via WebSocket and verify Kafka publishing
     * 
     * Validates: Requirements 3.1, 3.2, 3.4
     * 
     * Given: An authenticated WebSocket connection and a conversation with participant
     * When: User sends a message via WebSocket
     * Then: Message should be published to Kafka topic "chat-messages" with conversationId as key
     */
    @Test
    void testSendMessageViaWebSocketAndVerifyKafka() throws Exception {
        // Given: Create conversation and add user as participant
        Conversation conversation = createTestConversation();
        addParticipant(conversation, TEST_USER_ID);
        
        // Given: Valid JWT and WebSocket connection
        String jwtToken = generateValidJwtToken(TEST_USER_ID, TEST_USERNAME);
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        
        BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
        
        StompSession session = stompClient.connectAsync(
            websocketUrl,
            headers,
            new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    messageQueue.offer("CONNECTED");
                }
            }
        ).get(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        
        // Wait for connection
        String connectionStatus = messageQueue.poll(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        assertThat(connectionStatus).isEqualTo("CONNECTED");
        
        // When: Send message via WebSocket
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(conversation.getConversationId());
        request.setContent("Hello from WebSocket integration test!");
        request.setClientMessageId(UUID.randomUUID().toString());
        
        session.send("/app/chat.send", request);
        
        // Then: Verify message published to Kafka
        ConsumerRecords<String, ChatMessageEvent> records = KafkaTestUtils.getRecords(
            kafkaConsumer,
            TIMEOUT
        );
        
        assertThat(records.count()).isGreaterThan(0);
        
        boolean messageFound = false;
        for (ConsumerRecord<String, ChatMessageEvent> record : records) {
            ChatMessageEvent event = record.value();
            
            if (event.getClientMessageId().equals(request.getClientMessageId())) {
                messageFound = true;
                
                // Verify Kafka partition key is conversationId (Requirement 3.4)
                assertThat(record.key()).isEqualTo(conversation.getConversationId());
                
                // Verify message data (Requirement 3.2, 3.5)
                assertThat(event.getConversationId()).isEqualTo(conversation.getConversationId());
                assertThat(event.getSenderId()).isEqualTo(TEST_USER_ID);
                assertThat(event.getContent()).isEqualTo("Hello from WebSocket integration test!");
                assertThat(event.getMessageId()).isNotNull();
                assertThat(event.getStatus()).isEqualTo("SENDING");
                assertThat(event.getMessageType()).isEqualTo("TEXT");
                assertThat(event.getCreatedAt()).isNotNull();
                
                break;
            }
        }
        
        assertThat(messageFound)
            .as("Message should be published to Kafka topic 'chat-messages'")
            .isTrue();
        
        // Cleanup
        session.disconnect();
    }

    /**
     * Test: Non-participant cannot send message
     * 
     * Validates: Requirements 3.1, 2.6
     * 
     * Given: An authenticated user who is NOT a participant of a conversation
     * When: User attempts to send a message to that conversation
     * Then: Message should be rejected and error sent to user
     */
    @Test
    void testNonParticipantCannotSendMessage() throws Exception {
        // Given: Create conversation WITHOUT adding user as participant
        Conversation conversation = createTestConversation();
        
        // Given: Valid JWT but user is not a participant
        String jwtToken = generateValidJwtToken(TEST_USER_ID, TEST_USERNAME);
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        
        BlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();
        
        StompSession session = stompClient.connectAsync(
            websocketUrl,
            headers,
            new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    messageQueue.offer("CONNECTED");
                }
            }
        ).get(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        
        // Wait for connection
        messageQueue.poll(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        
        // Subscribe to error queue
        session.subscribe("/user/queue/errors", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }
            
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer(payload);
            }
        });
        
        // When: Attempt to send message as non-participant
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(conversation.getConversationId());
        request.setContent("This should fail");
        request.setClientMessageId(UUID.randomUUID().toString());
        
        session.send("/app/chat.send", request);
        
        // Then: Error should be received
        Object errorResponse = messageQueue.poll(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.toString()).contains("FORBIDDEN");
        
        // Cleanup
        session.disconnect();
    }

    /**
     * Test: Multiple messages maintain order via Kafka partitioning
     * 
     * Validates: Requirements 12.1, 12.2
     * 
     * Given: An authenticated participant
     * When: User sends multiple messages to the same conversation
     * Then: All messages should use the same Kafka partition key (conversationId)
     */
    @Test
    void testMultipleMessagesUseSamePartitionKey() throws Exception {
        // Given: Create conversation and add user as participant
        Conversation conversation = createTestConversation();
        addParticipant(conversation, TEST_USER_ID);
        
        // Given: Valid JWT and WebSocket connection
        String jwtToken = generateValidJwtToken(TEST_USER_ID, TEST_USERNAME);
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Bearer " + jwtToken);
        
        StompSession session = stompClient.connectAsync(
            websocketUrl,
            headers,
            new StompSessionHandlerAdapter() {}
        ).get(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        
        // When: Send multiple messages
        List<String> clientMessageIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            SendMessageRequest request = new SendMessageRequest();
            request.setConversationId(conversation.getConversationId());
            request.setContent("Message " + (i + 1));
            request.setClientMessageId(UUID.randomUUID().toString());
            clientMessageIds.add(request.getClientMessageId());
            
            session.send("/app/chat.send", request);
            Thread.sleep(100); // Small delay between messages
        }
        
        // Then: Verify all messages use same partition key
        ConsumerRecords<String, ChatMessageEvent> records = KafkaTestUtils.getRecords(
            kafkaConsumer,
            TIMEOUT
        );
        
        int messagesFound = 0;
        for (ConsumerRecord<String, ChatMessageEvent> record : records) {
            ChatMessageEvent event = record.value();
            
            if (clientMessageIds.contains(event.getClientMessageId())) {
                // Verify partition key is conversationId
                assertThat(record.key())
                    .as("All messages to same conversation should use conversationId as partition key")
                    .isEqualTo(conversation.getConversationId());
                
                messagesFound++;
            }
        }
        
        assertThat(messagesFound)
            .as("All sent messages should be published to Kafka")
            .isEqualTo(3);
        
        // Cleanup
        session.disconnect();
    }

    // ========== Helper Methods ==========

    /**
     * Generate a valid JWT token for testing
     */
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

    /**
     * Create a test conversation
     */
    private Conversation createTestConversation() {
        Conversation conversation = new Conversation();
        conversation.setConversationType("GROUP");
        conversation.setName("Test Conversation " + UUID.randomUUID());
        conversation.setMessageCount(0L);
        conversation.setIsArchived(0);
        conversation.setIsPinned(0);
        return conversationRepository.save(conversation);
    }

    /**
     * Add a participant to a conversation
     */
    private void addParticipant(Conversation conversation, String userId) {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setConversation(conversation);
        participant.setUserId(userId);
        participant.setStatus("ACTIVE");
        participant.setRole("MEMBER");
        participant.setUnreadCount(0);
        participant.setIsMuted(0);
        participant.setIsPinned(0);
        participant.setJoinedAt(Instant.now());
        participant.setAddedBy("system");
        participantRepository.save(participant);
    }

    /**
     * Setup Kafka consumer for test verification
     */
    private void setupKafkaConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
            "test-consumer-" + UUID.randomUUID(),
            "false",
            embeddedKafkaBroker
        );
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ChatMessageEvent.class.getName());
        
        DefaultKafkaConsumerFactory<String, ChatMessageEvent> consumerFactory = 
            new DefaultKafkaConsumerFactory<>(consumerProps);
        
        kafkaConsumer = consumerFactory.createConsumer();
        kafkaConsumer.subscribe(Collections.singletonList("chat-messages"));
    }
}
