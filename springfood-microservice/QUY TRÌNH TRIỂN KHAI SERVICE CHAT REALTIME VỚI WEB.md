<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

# QUY TRÌNH TRIỂN KHAI SERVICE CHAT REALTIME VỚI WEBSOCKET, KAFKA VÀ SPRING BOOT MICROSERVICE

## TỔNG QUAN KIẾN TRÚC

### Kiến trúc tổng thể

```
Client (Browser/Mobile)
    ↓ WebSocket Connection
WebSocket Service (Spring Boot)
    ↓ Kafka Producer/Consumer
Kafka Cluster (Message Broker)
    ↓
Chat Service (Message Processing)
    ↓
Database (MongoDB/PostgreSQL)
```


### Các thành phần chính

1. **WebSocket Layer**: Xử lý kết nối realtime với client
2. **Kafka Layer**: Message broker phân phối message giữa các service
3. **Chat Service**: Xử lý logic nghiệp vụ chat
4. **Database**: Lưu trữ lịch sử chat

***

## PHẦN 1: CẤU HÌNH WEBSOCKET CƠ BẢN

### Bước 1.1: Thêm Dependencies

**Maven (pom.xml)**:

```xml
<dependencies>
    <!-- Spring Boot WebSocket -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    
    <!-- Spring Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    
    <!-- STOMP -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-reactor-netty</artifactId>
    </dependency>
    
    <!-- Security (cho JWT) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- MongoDB (lưu chat history) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
    </dependency>
    
    <!-- JSON -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
</dependencies>
```


### Bước 1.2: Cấu hình WebSocket với STOMP

**WebSocketConfig.java**:

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint cho client kết nối WebSocket
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")  // Cho phép CORS
                .withSockJS();  // Fallback cho browser không support WebSocket
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho message từ client gửi lên
        registry.setApplicationDestinationPrefixes("/app");
        
        // Prefix cho message server broadcast xuống client
        // Dùng Simple Broker cho development
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Prefix cho user-specific messages
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Thêm interceptor để xác thực JWT
        registration.interceptors(new JwtChannelInterceptor());
    }
}
```

**Giải thích:**

- `/ws-chat`: Endpoint cho client kết nối ban đầu
- `/app`: Prefix cho message từ client (vd: `/app/chat.send`)
- `/topic`: Broadcast cho nhiều user (vd: group chat)
- `/queue`: Gửi cho 1 user cụ thể (vd: private message)

***

## PHẦN 2: TÍCH HỢP KAFKA

### Bước 2.1: Cấu hình Kafka Producer

**application.yml**:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all  # Đảm bảo message được ghi vào tất cả replica
      retries: 3
    consumer:
      group-id: chat-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
      auto-offset-reset: earliest
```

**KafkaProducerConfig.java**:

```java
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, ChatMessage> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);  // Tránh duplicate
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ChatMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```


### Bước 2.2: Cấu hình Kafka Consumer

**KafkaConsumerConfig.java**:

```java
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, ChatMessage> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-service-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);  // Manual commit
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessage> 
            kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessage> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);  // 3 consumer threads
        factory.getContainerProperties()
               .setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
```


### Bước 2.3: Kafka Topics Configuration

**KafkaTopicConfig.java**:

```java
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic chatMessageTopic() {
        return TopicBuilder.name("chat.message")
                .partitions(3)  // 3 partitions cho scalability
                .replicas(2)    // 2 replicas cho high availability
                .config("retention.ms", "604800000")  // 7 days
                .build();
    }

    @Bean
    public NewTopic chatNotificationTopic() {
        return TopicBuilder.name("chat.notification")
                .partitions(3)
                .replicas(2)
                .build();
    }

    @Bean
    public NewTopic userStatusTopic() {
        return TopicBuilder.name("user.status")
                .partitions(2)
                .replicas(2)
                .build();
    }
}
```


***

## PHẦN 3: MODELS VÀ ENTITIES

### Bước 3.1: Message Models

**ChatMessage.java**:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String id;
    private String chatRoomId;
    private String senderId;
    private String senderName;
    private String content;
    private MessageType type;  // TEXT, IMAGE, FILE, SYSTEM
    private LocalDateTime timestamp;
    private MessageStatus status;  // SENT, DELIVERED, READ
    private List<String> readBy;
}
```

**ChatRoom.java**:

```java
@Document(collection = "chat_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    @Id
    private String id;
    private String name;
    private ChatRoomType type;  // PRIVATE, GROUP
    private List<String> participants;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createdAt;
    private Map<String, Integer> unreadCount;  // userId -> count
}
```

**UserStatus.java**:

```java
@Data
@Builder
public class UserStatus {
    private String userId;
    private String username;
    private OnlineStatus status;  // ONLINE, OFFLINE, AWAY
    private LocalDateTime lastSeen;
    private String sessionId;
}
```


### Bước 3.2: Enums

```java
public enum MessageType {
    TEXT, IMAGE, FILE, SYSTEM, JOIN, LEAVE
}

public enum MessageStatus {
    SENT, DELIVERED, READ
}

public enum ChatRoomType {
    PRIVATE, GROUP
}

public enum OnlineStatus {
    ONLINE, OFFLINE, AWAY
}
```


***

## PHẦN 4: AUTHENTICATION \& SECURITY

### Bước 4.1: JWT Authentication Interceptor

**JwtChannelInterceptor.java**:

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class JwtChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = 
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Lấy JWT token từ STOMP header
            String token = accessor.getFirstNativeHeader("Authorization");
            
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                
                try {
                    // Validate và extract user từ JWT
                    if (jwtTokenProvider.validateToken(token)) {
                        String userId = jwtTokenProvider.getUserIdFromToken(token);
                        String username = jwtTokenProvider.getUsernameFromToken(token);
                        
                        // Tạo Principal và set vào accessor
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                userId, null, Collections.emptyList()
                            );
                        
                        accessor.setUser(authentication);
                        
                        // Lưu thông tin user vào session attributes
                        accessor.getSessionAttributes().put("userId", userId);
                        accessor.getSessionAttributes().put("username", username);
                    } else {
                        throw new IllegalArgumentException("Invalid JWT token");
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Authentication failed: " + e.getMessage());
                }
            } else {
                throw new IllegalArgumentException("Missing Authorization header");
            }
        }

        return message;
    }
}
```

**JwtTokenProvider.java**:

```java
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
        return claims.get("username", String.class);
    }
}
```


***

## PHẦN 5: WEBSOCKET CONTROLLER

### Bước 5.1: Chat Controller

**ChatController.java**:

```java
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Client gửi message tới /app/chat.send
     * Controller nhận và forward tới Kafka
     */
    @MessageMapping("/chat.send")
    public void sendMessage(
            @Payload ChatMessage message,
            @Header("simpSessionId") String sessionId,
            Principal principal
    ) {
        try {
            // Lấy thông tin user từ Principal
            String userId = principal.getName();
            message.setSenderId(userId);
            message.setTimestamp(LocalDateTime.now());
            message.setStatus(MessageStatus.SENT);
            
            log.info("Received message from user {}: {}", userId, message.getContent());
            
            // Publish message tới Kafka
            // Key = chatRoomId để đảm bảo message cùng room đi vào cùng partition
            kafkaTemplate.send("chat.message", message.getChatRoomId(), message)
                .addCallback(
                    result -> log.info("Message sent to Kafka: {}", message.getId()),
                    ex -> log.error("Failed to send message to Kafka", ex)
                );
                
        } catch (Exception e) {
            log.error("Error processing message", e);
            // Gửi error message về client
            messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                "Failed to send message: " + e.getMessage()
            );
        }
    }

    /**
     * Client gửi typing indicator
     */
    @MessageMapping("/chat.typing")
    public void userTyping(
            @Payload Map<String, String> payload,
            Principal principal
    ) {
        String chatRoomId = payload.get("chatRoomId");
        String username = payload.get("username");
        
        // Broadcast typing indicator tới room
        messagingTemplate.convertAndSend(
            "/topic/chat.room." + chatRoomId + ".typing",
            Map.of("username", username, "typing", true)
        );
    }

    /**
     * Mark message as read
     */
    @MessageMapping("/chat.read")
    public void markAsRead(
            @Payload Map<String, String> payload,
            Principal principal
    ) {
        String messageId = payload.get("messageId");
        String userId = principal.getName();
        
        chatService.markMessageAsRead(messageId, userId);
    }
}
```


### Bước 5.2: User Status Controller

**UserStatusController.java**:

```java
@Controller
@RequiredArgsConstructor
@Slf4j
public class UserStatusController {

    private final KafkaTemplate<String, UserStatus> kafkaTemplate;
    private final UserSessionService userSessionService;

    @MessageMapping("/user.online")
    public void userOnline(Principal principal) {
        String userId = principal.getName();
        
        UserStatus status = UserStatus.builder()
            .userId(userId)
            .status(OnlineStatus.ONLINE)
            .lastSeen(LocalDateTime.now())
            .build();
            
        kafkaTemplate.send("user.status", userId, status);
        log.info("User {} is now online", userId);
    }
}
```


***

## PHẦN 6: KAFKA PRODUCER SERVICE

**KafkaMessageProducer.java**:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageProducer {

    private final KafkaTemplate<String, ChatMessage> chatKafkaTemplate;
    private final KafkaTemplate<String, UserStatus> statusKafkaTemplate;

    /**
     * Gửi chat message tới Kafka
     */
    public void sendChatMessage(ChatMessage message) {
        try {
            // Key = chatRoomId để message cùng room vào cùng partition
            // => đảm bảo thứ tự message trong 1 room
            ListenableFuture<SendResult<String, ChatMessage>> future = 
                chatKafkaTemplate.send("chat.message", message.getChatRoomId(), message);
            
            future.addCallback(
                result -> {
                    log.info("Message sent successfully: messageId={}, offset={}", 
                        message.getId(), 
                        result.getRecordMetadata().offset());
                },
                ex -> {
                    log.error("Failed to send message: messageId={}", message.getId(), ex);
                    // TODO: Implement retry logic hoặc DLQ
                }
            );
        } catch (Exception e) {
            log.error("Error sending message to Kafka", e);
            throw new RuntimeException("Failed to send message", e);
        }
    }

    /**
     * Gửi user status update
     */
    public void sendUserStatus(UserStatus status) {
        statusKafkaTemplate.send("user.status", status.getUserId(), status);
    }

    /**
     * Gửi notification
     */
    public void sendNotification(String userId, String message) {
        Map<String, Object> notification = Map.of(
            "userId", userId,
            "message", message,
            "timestamp", LocalDateTime.now()
        );
        
        chatKafkaTemplate.send("chat.notification", userId, 
            ChatMessage.builder()
                .type(MessageType.SYSTEM)
                .content(message)
                .build()
        );
    }
}
```


***

## PHẦN 7: KAFKA CONSUMER SERVICE

**KafkaMessageConsumer.java**:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;

    /**
     * Consumer message từ Kafka và broadcast tới WebSocket clients
     */
    @KafkaListener(
        topics = "chat.message",
        groupId = "chat-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeChatMessage(
            @Payload ChatMessage message,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("Consumed message: id={}, partition={}, offset={}", 
                message.getId(), partition, offset);
            
            // 1. Lưu message vào database
            ChatMessage savedMessage = chatService.saveMessage(message);
            
            // 2. Update chat room last message
            chatRoomService.updateLastMessage(
                message.getChatRoomId(), 
                message.getContent(),
                message.getTimestamp()
            );
            
            // 3. Broadcast message tới tất cả user trong room
            // Topic pattern: /topic/chat.room.{roomId}
            messagingTemplate.convertAndSend(
                "/topic/chat.room." + message.getChatRoomId(),
                savedMessage
            );
            
            // 4. Gửi notification cho từng user offline
            List<String> offlineUsers = chatRoomService
                .getOfflineParticipants(message.getChatRoomId());
            
            for (String userId : offlineUsers) {
                if (!userId.equals(message.getSenderId())) {
                    // Gửi notification tới queue riêng của user
                    messagingTemplate.convertAndSendToUser(
                        userId,
                        "/queue/notifications",
                        Map.of(
                            "type", "NEW_MESSAGE",
                            "chatRoomId", message.getChatRoomId(),
                            "message", message.getContent()
                        )
                    );
                }
            }
            
            // 5. Manual commit offset sau khi xử lý thành công
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error consuming message", e);
            // TODO: Send to DLQ (Dead Letter Queue)
            acknowledgment.acknowledge();  // Vẫn commit để không block
        }
    }

    /**
     * Consumer user status updates
     */
    @KafkaListener(
        topics = "user.status",
        groupId = "chat-service-group"
    )
    public void consumeUserStatus(
            @Payload UserStatus status,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("User status update: userId={}, status={}", 
                status.getUserId(), status.getStatus());
            
            // Broadcast status tới tất cả user
            messagingTemplate.convertAndSend(
                "/topic/user.status",
                status
            );
            
            // Update status trong cache/database
            chatService.updateUserStatus(status);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing user status", e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Consumer notifications
     */
    @KafkaListener(
        topics = "chat.notification",
        groupId = "notification-group"
    )
    public void consumeNotification(
            @Payload ChatMessage notification,
            Acknowledgment acknowledgment
    ) {
        try {
            // Gửi notification tới user cụ thể
            messagingTemplate.convertAndSend(
                "/topic/notifications",
                notification
            );
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing notification", e);
            acknowledgment.acknowledge();
        }
    }
}
```


***

## PHẦN 8: BUSINESS LOGIC SERVICES

### Bước 8.1: Chat Service

**ChatService.java**:

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final KafkaMessageProducer kafkaProducer;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Lưu message vào database
     */
    public ChatMessage saveMessage(ChatMessage message) {
        // Generate ID nếu chưa có
        if (message.getId() == null) {
            message.setId(UUID.randomUUID().toString());
        }
        
        // Validate chat room exists
        ChatRoom chatRoom = chatRoomRepository.findById(message.getChatRoomId())
            .orElseThrow(() -> new NotFoundException("Chat room not found"));
        
        // Initialize readBy list
        if (message.getReadBy() == null) {
            message.setReadBy(new ArrayList<>());
        }
        message.getReadBy().add(message.getSenderId());  // Sender auto-read
        
        // Save to MongoDB
        ChatMessage saved = chatMessageRepository.save(message);
        
        // Update unread count cho các participants khác
        updateUnreadCount(chatRoom, message.getSenderId());
        
        return saved;
    }

    /**
     * Get chat history với pagination
     */
    public Page<ChatMessage> getChatHistory(
            String chatRoomId, 
            Pageable pageable
    ) {
        return chatMessageRepository.findByChatRoomIdOrderByTimestampDesc(
            chatRoomId, 
            pageable
        );
    }

    /**
     * Mark message as read
     */
    public void markMessageAsRead(String messageId, String userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new NotFoundException("Message not found"));
        
        if (!message.getReadBy().contains(userId)) {
            message.getReadBy().add(userId);
            message.setStatus(MessageStatus.READ);
            chatMessageRepository.save(message);
            
            // Notify sender về read receipt
            messagingTemplate.convertAndSendToUser(
                message.getSenderId(),
                "/queue/read-receipts",
                Map.of("messageId", messageId, "readBy", userId)
            );
        }
    }

    /**
     * Update unread count
     */
    private void updateUnreadCount(ChatRoom chatRoom, String senderId) {
        for (String participantId : chatRoom.getParticipants()) {
            if (!participantId.equals(senderId)) {
                int currentCount = chatRoom.getUnreadCount()
                    .getOrDefault(participantId, 0);
                chatRoom.getUnreadCount().put(participantId, currentCount + 1);
            }
        }
        chatRoomRepository.save(chatRoom);
    }

    /**
     * Clear unread count khi user vào room
     */
    public void clearUnreadCount(String chatRoomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new NotFoundException("Chat room not found"));
        
        chatRoom.getUnreadCount().put(userId, 0);
        chatRoomRepository.save(chatRoom);
    }

    /**
     * Update user status
     */
    public void updateUserStatus(UserStatus status) {
        // Update trong cache hoặc database
        // TODO: Implement caching với Redis
        log.info("Updated user status: {}", status);
    }

    /**
     * Search messages
     */
    public List<ChatMessage> searchMessages(
            String chatRoomId, 
            String keyword
    ) {
        return chatMessageRepository.findByChatRoomIdAndContentContaining(
            chatRoomId, 
            keyword
        );
    }
}
```


### Bước 8.2: Chat Room Service

**ChatRoomService.java**:

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final UserSessionService userSessionService;

    /**
     * Tạo private chat room (1-1)
     */
    public ChatRoom createPrivateChatRoom(String userId1, String userId2) {
        // Check nếu room đã tồn tại
        Optional<ChatRoom> existingRoom = chatRoomRepository
            .findPrivateRoomByParticipants(userId1, userId2);
        
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }
        
        // Tạo room mới
        ChatRoom chatRoom = ChatRoom.builder()
            .id(UUID.randomUUID().toString())
            .type(ChatRoomType.PRIVATE)
            .participants(Arrays.asList(userId1, userId2))
            .createdAt(LocalDateTime.now())
            .unreadCount(new HashMap<>())
            .build();
        
        return chatRoomRepository.save(chatRoom);
    }

    /**
     * Tạo group chat room
     */
    public ChatRoom createGroupChatRoom(
            String name, 
            List<String> participants
    ) {
        if (participants.size() < 2) {
            throw new IllegalArgumentException("Group must have at least 2 participants");
        }
        
        ChatRoom chatRoom = ChatRoom.builder()
            .id(UUID.randomUUID().toString())
            .name(name)
            .type(ChatRoomType.GROUP)
            .participants(participants)
            .createdAt(LocalDateTime.now())
            .unreadCount(new HashMap<>())
            .build();
        
        return chatRoomRepository.save(chatRoom);
    }

    /**
     * Get user's chat rooms
     */
    public List<ChatRoom> getUserChatRooms(String userId) {
        return chatRoomRepository.findByParticipantsContaining(userId);
    }

    /**
     * Add participant to group
     */
    public void addParticipant(String chatRoomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new NotFoundException("Chat room not found"));
        
        if (chatRoom.getType() != ChatRoomType.GROUP) {
            throw new IllegalArgumentException("Can only add participants to group chat");
        }
        
        if (!chatRoom.getParticipants().contains(userId)) {
            chatRoom.getParticipants().add(userId);
            chatRoomRepository.save(chatRoom);
            
            // Send system message
            sendSystemMessage(chatRoomId, userId + " joined the group");
        }
    }

    /**
     * Remove participant from group
     */
    public void removeParticipant(String chatRoomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new NotFoundException("Chat room not found"));
        
        chatRoom.getParticipants().remove(userId);
        chatRoomRepository.save(chatRoom);
        
        sendSystemMessage(chatRoomId, userId + " left the group");
    }

    /**
     * Update last message
     */
    public void updateLastMessage(
            String chatRoomId, 
            String message, 
            LocalDateTime timestamp
    ) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new NotFoundException("Chat room not found"));
        
        chatRoom.setLastMessage(message);
        chatRoom.setLastMessageTime(timestamp);
        chatRoomRepository.save(chatRoom);
    }

    /**
     * Get offline participants
     */
    public List<String> getOfflineParticipants(String chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow(() -> new NotFoundException("Chat room not found"));
        
        return chatRoom.getParticipants().stream()
            .filter(userId -> !userSessionService.isUserOnline(userId))
            .collect(Collectors.toList());
    }

    /**
     * Send system message
     */
    private void sendSystemMessage(String chatRoomId, String content) {
        ChatMessage systemMessage = ChatMessage.builder()
            .id(UUID.randomUUID().toString())
            .chatRoomId(chatRoomId)
            .type(MessageType.SYSTEM)
            .content(content)
            .timestamp(LocalDateTime.now())
            .build();
        
        // Send to Kafka để broadcast
        // kafkaProducer.sendChatMessage(systemMessage);
    }
}
```


***

## PHẦN 9: WEBSOCKET EVENT LISTENERS

**WebSocketEventListener.java**:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final KafkaMessageProducer kafkaProducer;
    private final UserSessionService userSessionService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Khi user connect
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // Get user info từ session attributes
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        
        if (userId != null) {
            log.info("User connected: userId={}, sessionId={}", userId, sessionId);
            
            // Lưu session
            userSessionService.addSession(userId, sessionId);
            
            // Publish user online status
            UserStatus status = UserStatus.builder()
                .userId(userId)
                .username(username)
                .status(OnlineStatus.ONLINE)
                .sessionId(sessionId)
                .lastSeen(LocalDateTime.now())
                .build();
            
            kafkaProducer.sendUserStatus(status);
        }
    }

    /**
     * Khi user disconnect
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        
        if (userId != null) {
            log.info("User disconnected: userId={}, sessionId={}", userId, sessionId);
            
            // Remove session
            userSessionService.removeSession(userId, sessionId);
            
            // Check nếu user không còn session nào => OFFLINE
            if (!userSessionService.isUserOnline(userId)) {
                UserStatus status = UserStatus.builder()
                    .userId(userId)
                    .username(username)
                    .status(OnlineStatus.OFFLINE)
                    .lastSeen(LocalDateTime.now())
                    .build();
                
                kafkaProducer.sendUserStatus(status);
            }
        }
    }

    /**
     * Khi có subscribe event
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        
        log.info("User subscribed: userId={}, destination={}", userId, destination);
        
        // Nếu subscribe vào chat room => clear unread count
        if (destination != null && destination.startsWith("/topic/chat.room.")) {
            String chatRoomId = destination.replace("/topic/chat.room.", "");
            // chatService.clearUnreadCount(chatRoomId, userId);
        }
    }
}
```


***

## PHẦN 10: USER SESSION MANAGEMENT

**UserSessionService.java**:

```java
@Service
@Slf4j
public class UserSessionService {

    // Map userId -> Set of sessionIds
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    /**
     * Add session cho user
     */
    public void addSession(String userId, String sessionId) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                    .add(sessionId);
        log.info("Added session: userId={}, sessionId={}, totalSessions={}", 
            userId, sessionId, userSessions.get(userId).size());
    }

    /**
     * Remove session
     */
    public void removeSession(String userId, String sessionId) {
        Set<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
            log.info("Removed session: userId={}, sessionId={}, remainingSessions={}", 
                userId, sessionId, sessions.size());
        }
    }

    /**
     * Check user online (có ít nhất 1 session)
     */
    public boolean isUserOnline(String userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * Get all sessions của user
     */
    public Set<String> getUserSessions(String userId) {
        return userSessions.getOrDefault(userId, Collections.emptySet());
    }

    /**
     * Get all online users
     */
    public Set<String> getOnlineUsers() {
        return userSessions.keySet();
    }

    /**
     * Get session count của user
     */
    public int getSessionCount(String userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null ? sessions.size() : 0;
    }
}
```

**Lưu ý về Session Management:**

Trong production, nên dùng **Redis** để lưu session thay vì in-memory Map vì:

- Scale multiple instances
- Session persistence khi restart
- Shared state giữa các server

***

## PHẦN 11: REPOSITORIES

### MongoDB Repositories

**ChatMessageRepository.java**:

```java
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    
    Page<ChatMessage> findByChatRoomIdOrderByTimestampDesc(
        String chatRoomId, 
        Pageable pageable
    );
    
    List<ChatMessage> findByChatRoomIdAndContentContaining(
        String chatRoomId, 
        String keyword
    );
    
    List<ChatMessage> findByChatRoomIdAndTimestampBetween(
        String chatRoomId,
        LocalDateTime start,
        LocalDateTime end
    );
    
    Long countByChatRoomIdAndReadByNotContaining(
        String chatRoomId, 
        String userId
    );
}
```

**ChatRoomRepository.java**:

```java
@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    
    List<ChatRoom> findByParticipantsContaining(String userId);
    
    @Query("{ 'type': 'PRIVATE', 'participants': { $all: [?0, ?1] } }")
    Optional<ChatRoom> findPrivateRoomByParticipants(String userId1, String userId2);
    
    List<ChatRoom> findByTypeAndParticipantsContaining(
        ChatRoomType type, 
        String userId
    );
}
```


***

## PHẦN 12: ERROR HANDLING

**WebSocketExceptionHandler.java**:

```java
@ControllerAdvice
@Slf4j
public class WebSocketExceptionHandler {

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public ErrorMessage handleException(Exception exception) {
        log.error("WebSocket error occurred", exception);
        
        return ErrorMessage.builder()
            .message(exception.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    }

    @MessageExceptionHandler(NotFoundException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleNotFoundException(NotFoundException exception) {
        log.error("Resource not found", exception);
        
        return ErrorMessage.builder()
            .message("Resource not found: " + exception.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

**ErrorMessage.java**:

```java
@Data
@Builder
public class ErrorMessage {
    private String message;
    private LocalDateTime timestamp;
    private String code;
}
```


***

## PHẦN 13: APPLICATION PROPERTIES

**application.yml**:

```yaml
spring:
  application:
    name: chat-service
    
  # MongoDB Configuration
  data:
    mongodb:
      uri: mongodb://localhost:27017/chat_db
      auto-index-creation: true
      
  # Kafka Configuration
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
        linger.ms: 10
        batch.size: 16384
        compression.type: snappy
    consumer:
      group-id: chat-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      enable-auto-commit: false
      properties:
        spring.json.trusted.packages: "*"
        max.poll.records: 500
        max.poll.interval.ms: 300000
        
  # Security
  security:
    jwt:
      secret: ${JWT_SECRET:your-secret-key-change-this-in-production}
      expiration: 86400000  # 24 hours

# Server Configuration
server:
  port: 8080
  
# WebSocket Configuration
websocket:
  allowed-origins: 
    - http://localhost:3000
    - http://localhost:4200
  heartbeat:
    send-interval: 20000
    receive-interval: 20000
    
# Logging
logging:
  level:
    root: INFO
    com.yourcompany.chat: DEBUG
    org.springframework.kafka: INFO
    org.springframework.web.socket: DEBUG
```


***

## PHẦN 14: LUỒNG DỮ LIỆU CHI TIẾT

### Luồng gửi message

```
1. Client gửi message qua WebSocket
   ↓
   SEND /app/chat.send
   {
     "chatRoomId": "room123",
     "content": "Hello",
     "type": "TEXT"
   }

2. ChatController.sendMessage() nhận message
   ↓
   - Validate user từ Principal
   - Set metadata (senderId, timestamp, status)
   
3. Gửi message tới Kafka topic "chat.message"
   ↓
   Key: chatRoomId
   Value: ChatMessage object
   Partition: Dựa vào hash(chatRoomId)

4. KafkaMessageConsumer nhận message từ Kafka
   ↓
   - Save message vào MongoDB
   - Update chat room last message
   - Update unread count
   
5. Broadcast message tới WebSocket clients
   ↓
   messagingTemplate.convertAndSend(
     "/topic/chat.room." + chatRoomId,
     message
   )

6. Tất cả clients subscribe "/topic/chat.room.{roomId}" nhận message
   ↓
   - Client update UI
   - Send read receipt (optional)
```


### Luồng user connect/disconnect

```
1. Client connect tới /ws-chat với JWT token
   ↓
   CONNECT
   Headers: {
     Authorization: Bearer <token>
   }

2. JwtChannelInterceptor.preSend()
   ↓
   - Validate JWT token
   - Extract userId, username
   - Set Principal vào accessor
   
3. SessionConnectedEvent được trigger
   ↓
   WebSocketEventListener.handleWebSocketConnectListener()
   - Add session vào UserSessionService
   - Publish ONLINE status tới Kafka "user.status"

4. KafkaMessageConsumer nhận user status
   ↓
   - Broadcast tới "/topic/user.status"
   - Update cache/database

5. All clients nhận status update
   ↓
   - Update online user list
   - Show online indicator
```


### Luồng user typing indicator

```
1. Client gửi typing event
   ↓
   SEND /app/chat.typing
   { "chatRoomId": "room123", "username": "John" }

2. ChatController.userTyping()
   ↓
   Broadcast ngay lập tức (không qua Kafka)
   
3. messagingTemplate.convertAndSend(
     "/topic/chat.room.{roomId}.typing",
     { "username": "John", "typing": true }
   )

4. Clients trong room nhận typing indicator
   ↓
   Show "John is typing..."
```


***

## PHẦN 15: SCALABILITY \& BEST PRACTICES

### 15.1: Horizontal Scaling với Redis Pub/Sub

Khi scale multiple instances, cần dùng **External Message Broker**:

**RedisConfig.java**:

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("chat.*"));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

**WebSocketConfig với Redis**:

```java
@Override
public void configureMessageBroker(MessageBrokerRegistry registry) {
    // Thay SimpleBroker bằng External Broker
    registry.enableStompBrokerRelay("/topic", "/queue")
        .setRelayHost("localhost")
        .setRelayPort(61613)  // STOMP port
        .setClientLogin("guest")
        .setClientPasscode("guest");
        
    registry.setApplicationDestinationPrefixes("/app");
}
```


### 15.2: Kafka Partitioning Strategy

```java
// Custom Partitioner để đảm bảo message cùng room vào cùng partition
public class ChatRoomPartitioner implements Partitioner {
    
    @Override
    public int partition(String topic, Object key, byte[] keyBytes,
                        Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();
        
        // Hash chatRoomId để có partition cố định
        String chatRoomId = (String) key;
        return Math.abs(chatRoomId.hashCode()) % numPartitions;
    }
}
```


### 15.3: Message Delivery Guarantees

**At-least-once delivery**:

```java
// Producer config
props.put(ProducerConfig.ACKS_CONFIG, "all");
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

// Consumer config
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
// Manual commit sau khi xử lý thành công
acknowledgment.acknowledge();
```


### 15.4: Performance Optimization

**Batch Processing**:

```java
@KafkaListener(
    topics = "chat.message",
    batch = "true"
)
public void consumeBatch(
        List<ChatMessage> messages,
        Acknowledgment acknowledgment
) {
    try {
        // Batch save to database
        chatMessageRepository.saveAll(messages);
        
        // Batch broadcast
        messages.forEach(msg -> 
            messagingTemplate.convertAndSend(
                "/topic/chat.room." + msg.getChatRoomId(),
                msg
            )
        );
        
        acknowledgment.acknowledge();
    } catch (Exception e) {
        log.error("Error processing batch", e);
    }
}
```


### 15.5: Monitoring \& Metrics

**KafkaMetricsConfig.java**:

```java
@Configuration
public class KafkaMetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags("application", "chat-service");
    }
}
```

**Expose metrics**:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```


***

## PHẦN 16: TESTING

### Integration Test cho WebSocket

**ChatWebSocketIT.java**:

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
class ChatWebSocketIT {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private final BlockingQueue<ChatMessage> messageQueue = 
        new LinkedBlockingQueue<>();

    @BeforeEach
    void setup() {
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        this.stompClient = new WebSocketStompClient(webSocketClient);
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void shouldSendAndReceiveMessage() throws Exception {
        // Connect
        String url = "ws://localhost:" + port + "/ws-chat";
        StompSession session = stompClient.connect(url, 
            new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);

        // Subscribe
        session.subscribe("/topic/chat.room.test123", 
            new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return ChatMessage.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    messageQueue.add((ChatMessage) payload);
                }
            });

        // Send message
        ChatMessage message = ChatMessage.builder()
            .chatRoomId("test123")
            .content("Test message")
            .build();
        session.send("/app/chat.send", message);

        // Assert
        ChatMessage received = messageQueue.poll(5, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.getContent()).isEqualTo("Test message");
    }
}
```


***

## TÓM TẮT FLOW HOÀN CHỈNH

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                         CLIENTS                              │
│  (Browser, Mobile App với WebSocket + STOMP)                │
└──────────────────────┬──────────────────────────────────────┘
                       │ WebSocket + JWT
                       ↓
┌─────────────────────────────────────────────────────────────┐
│              WEBSOCKET SERVICE (Spring Boot)                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  • WebSocket Config (STOMP)                          │  │
│  │  • JWT Interceptor (Authentication)                  │  │
│  │  • Chat Controller (@MessageMapping)                 │  │
│  │  • Event Listeners (Connect/Disconnect)              │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────────────────┘
                       │ Kafka Producer/Consumer
                       ↓
┌─────────────────────────────────────────────────────────────┐
│                    KAFKA CLUSTER                             │
│  Topics: chat.message, user.status, chat.notification       │
│  Partitions: 3 (distributed by chatRoomId)                  │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ↓
┌─────────────────────────────────────────────────────────────┐
│              CHAT SERVICE (Business Logic)                   │
│  • ChatService (save, retrieve, mark read)                  │
│  • ChatRoomService (create, manage rooms)                   │
│  • UserSessionService (online status)                       │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ↓
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                            │
│  • MongoDB: Chat messages, Chat rooms                       │
│  • Redis (optional): Sessions, Cache                        │
└─────────────────────────────────────────────────────────────┘
```


### Key Points

1. **WebSocket** xử lý realtime connection với client
2. **STOMP** cung cấp protocol rõ ràng cho messaging
3. **Kafka** làm message broker để scale và decouple services
4. **JWT** xác thực user ở tầng STOMP (không dùng HTTP auth)
5. **MongoDB** lưu chat history và room info
6. **SimpMessagingTemplate** broadcast message tới WebSocket clients

Quy trình này đảm bảo:

- ✅ **Scalability**: Scale horizontal với Kafka partitioning
- ✅ **Reliability**: At-least-once delivery, manual commit
- ✅ **Security**: JWT authentication ở STOMP level
- ✅ **Performance**: Batch processing, connection pooling
- ✅ **Maintainability**: Clear separation of concerns
<span style="display:none">[^1][^10][^11][^12][^13][^14][^15][^16][^17][^18][^19][^2][^20][^21][^22][^23][^24][^25][^26][^27][^3][^4][^5][^6][^7][^8][^9]</span>

<div align="center">⁂</div>

[^1]: https://dev.to/subhransu/realtime-chat-app-using-kafka-springboot-reactjs-and-websockets-lc

[^2]: https://www.swiftorial.com/tutorials/software_architecture/microservices/service_communication/websockets

[^3]: https://stackoverflow.com/questions/55851466/stomp-protocol-implementation-with-kafka

[^4]: https://stackoverflow.com/questions/46431343/spring-websocket-integration-with-kafka

[^5]: https://stackoverflow.com/questions/61405784/how-to-build-a-scalable-realtime-chat-messaging-with-websocket

[^6]: https://docs.spring.io/spring-integration/reference/kafka.html

[^7]: https://nuancesprog.ru/p/20949/

[^8]: https://www.geeksforgeeks.org/system-design/websockets-in-microservices-architecture/

[^9]: https://docs.spring.io/spring-framework/reference/web/websocket/stomp/client.html

[^10]: https://github.com/ivangfr/springboot-kafka-websocket

[^11]: https://bkjam.github.io/posts/2022-05-04-building-a-websocket-server-in-a-microservice-architecture/

[^12]: https://docs.spring.io/spring-integration/reference/stomp.html

[^13]: https://www.youtube.com/watch?v=sBLZcqtqiQo

[^14]: https://www.reddit.com/r/golang/comments/10mkh7y/scalable_chat_application_in_a_microservices/

[^15]: https://wans1027.tistory.com/29

[^16]: https://www.devglan.com/spring-boot/spring-boot-websocket-example

[^17]: https://github.com/saraogiraj94/stomp-kafka-bridge

[^18]: https://tsh.io/blog/how-to-scale-websocket/

[^19]: https://www.youtube.com/watch?v=IzTPUl3WsBg

[^20]: https://www.geeksforgeeks.org/springboot/spring-boot-web-socket/

[^21]: https://stackoverflow.com/questions/55851466/stomp-protocol-implementation-with-kafka/55852145

[^22]: https://ably.com/topic/websocket-architecture-best-practices

[^23]: https://www.videosdk.live/developer-hub/websocket/spring-websocket

[^24]: https://www.instaclustr.com/education/apache-kafka/apache-kafka-integration-challenges-solutions-and-best-practices/

[^25]: https://websocket.org/guides/websockets-at-scale/

[^26]: https://developer.vonage.com/en/blog/create-websocket-server-spring-boot-dr

[^27]: https://velog.io/@zvyg1023/Stomp-Kafka를-이용한-1-대-1-채팅-구현-4-메시지-전송-및-처리

