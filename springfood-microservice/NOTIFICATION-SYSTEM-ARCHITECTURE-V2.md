# Kiến trúc Hệ thống Thông báo - Tái sử dụng Chat WebSocket Server

## Tổng quan

Tài liệu này mô tả kiến trúc tối ưu cho hệ thống thông báo, trong đó **TÁI SỬ DỤNG** WebSocket Server đã có ở Chat Service thay vì tạo thêm server mới. Điều này giúp:
- Giảm số lượng WebSocket connections từ client
- Tiết kiệm tài nguyên server
- Đơn giản hóa client implementation
- Dễ dàng maintain

---

## 1. Kiến trúc Tổng thể (Optimized Architecture)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         BUSINESS SERVICES LAYER                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ Order Service│  │ Shop Service │  │ Product Svc  │  │ Payment Svc  │   │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘   │
│         │                 │                 │                 │            │
│         └─────────────────┴─────────────────┴─────────────────┘            │
│                                   │                                         │
│                          Publish Domain Events                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │      KAFKA MESSAGE BUS        │
                    │  Topics:                      │
                    │  - order.events               │
                    │  - shipping.events            │
                    │  - notification.events        │
                    └───────────────┬───────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
                    ▼                               ▼
        ┌───────────────────────┐       ┌───────────────────────┐
        │ NOTIFICATION SERVICE  │       │   CHAT SERVICE        │
        │                       │       │                       │
        │ ┌─────────────────┐  │       │ ┌─────────────────┐  │
        │ │ Event Consumers │  │       │ │ Chat Logic      │  │
        │ │ - OrderConsumer │  │       │ │ - Messages      │  │
        │ │ - ShipConsumer  │  │       │ │ - Conversations │  │
        │ └────────┬────────┘  │       │ └─────────────────┘  │
        │          ▼            │       │                       │
        │ ┌─────────────────┐  │       │ ┌─────────────────┐  │
        │ │ Notification    │  │       │ │ Kafka Consumer  │  │
        │ │ Processor       │  │       │ │ (notification.  │  │
        │ └────────┬────────┘  │       │ │  events)        │  │
        │          ▼            │       │ └────────┬────────┘  │
        │ ┌─────────────────┐  │       │          │            │
        │ │ PostgreSQL      │  │       │          ▼            │
        │ │ (Store)         │  │       │ ┌─────────────────┐  │
        │ └────────┬────────┘  │       │ │ WebSocket       │  │
        │          │            │       │ │ Server (STOMP)  │  │
        │          │ Publish    │       │ │                 │  │
        │          ▼            │       │ │ Endpoints:      │  │
        │ ┌─────────────────┐  │       │ │ /ws/chat        │  │
        │ │ Kafka Producer  │──┼───────┼>│ /ws/notifications│ │
        │ │ (notification.  │  │       │ │                 │  │
        │ │  events)        │  │       │ │ Destinations:   │  │
        │ └─────────────────┘  │       │ │ /user/queue/    │  │
        └───────────────────────┘       │ │  - chat         │  │
                                        │ │  - notifications│  │
                                        │ └────────┬────────┘  │
                                        └──────────┼───────────┘
                                                   │
                                                   ▼
                                        ┌───────────────────┐
                                        │   CLIENT APPS     │
                                        │  - Web (React)    │
                                        │  - Mobile (iOS)   │
                                        │  - Mobile (Android│
                                        │                   │
                                        │  1 WebSocket      │
                                        │  Connection       │
                                        │  cho cả Chat +    │
                                        │  Notification     │
                                        └───────────────────┘
```

---

## 2. Luồng Hoạt Động Chi Tiết

### 2.1. Flow: Từ Business Event đến Client

```
STEP 1: Business Event
──────────────────────
OrderService.processPaymentSuccess()
  ├─ Update order status
  ├─ Save to database
  └─ Publish to Kafka: "order.events"
       {
         eventType: "ORDER_PAID",
         orderId: 123,
         customerId: 456,
         shopId: 789
       }

STEP 2: Notification Service Consumes
──────────────────────────────────────
OrderEventConsumer (Notification Service)
  ├─ Consume from "order.events"
  ├─ Process event
  ├─ Map to notification types
  ├─ Check user preferences
  ├─ Render templates
  ├─ Save to PostgreSQL
  └─ Publish to Kafka: "notification.events"
       {
         type: "NOTIFICATION_CREATED",
         notificationId: 9876,
         userId: 456,
         title: "Đơn hàng đã thanh toán",
         body: "Đơn hàng #ORD-001...",
         data: {...}
       }

STEP 3: Chat Service Consumes (WebSocket Push)
───────────────────────────────────────────────
NotificationEventConsumer (Chat Service)
  ├─ Consume from "notification.events"
  ├─ Check user online (Redis)
  └─ If online:
      └─ Push via WebSocket
           messagingTemplate.convertAndSendToUser(
             "456",
             "/queue/notifications",
             notificationMessage
           )

STEP 4: Client Receives
────────────────────────
Client App
  ├─ Connected to: ws://chat-service/ws/notifications
  ├─ Subscribed to: /user/queue/notifications
  ├─ Receive notification
  ├─ Display toast/banner
  └─ Update badge count
```


---

## 3. Implementation Chi Tiết

### 3.1. Notification Service - Publish Notification Events

```java
// notification-service/src/main/java/com/theblood/notification/service/NotificationProcessor.java

@Service
@Slf4j
public class NotificationProcessor {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    
    @Autowired
    private UserPreferenceService preferenceService;
    
    @Transactional
    public void processOrderEvent(OrderEvent event) {
        // 1. Map event → notification types
        List<NotificationMapping> mappings = getNotificationMappings(event.getEventType());
        
        for (NotificationMapping mapping : mappings) {
            // 2. Determine recipients
            List<Recipient> recipients = determineRecipients(event, mapping.getTargetRoles());
            
            for (Recipient recipient : recipients) {
                // 3. Check user preference
                UserPreference pref = preferenceService.getPreference(
                    recipient.getUserId(), 
                    mapping.getNotificationType()
                );
                
                if (!pref.isEnabled()) {
                    continue;
                }
                
                // 4. Render notification content
                NotificationContent content = templateService.render(
                    mapping.getTemplateKey(),
                    recipient.getLocale(),
                    event.toTemplateData()
                );
                
                // 5. Save to database
                Notification notification = Notification.builder()
                    .userId(recipient.getUserId())
                    .type(mapping.getNotificationType())
                    .title(content.getTitle())
                    .body(content.getBody())
                    .data(event.getMetadata())
                    .status(NotificationStatus.PENDING)
                    .createdAt(Instant.now())
                    .build();
                
                notification = notificationRepository.save(notification);
                
                // 6. Publish notification event to Kafka
                // Chat Service sẽ consume và push qua WebSocket
                publishNotificationEvent(notification, recipient, pref);
                
                // 7. Send email/push if enabled
                if (pref.isEmailEnabled()) {
                    emailService.send(notification, recipient);
                }
                if (pref.isPushEnabled()) {
                    pushService.send(notification, recipient);
                }
            }
        }
    }
    
    private void publishNotificationEvent(Notification notification, Recipient recipient, UserPreference pref) {
        // Chỉ publish nếu in-app notification được enable
        if (!pref.isInAppEnabled()) {
            return;
        }
        
        NotificationEvent event = NotificationEvent.builder()
            .type("NOTIFICATION_CREATED")
            .notificationId(notification.getId())
            .userId(recipient.getUserId())
            .notificationType(notification.getType())
            .title(notification.getTitle())
            .body(notification.getBody())
            .data(notification.getData())
            .createdAt(notification.getCreatedAt())
            .build();
        
        // Publish to Kafka topic "notification.events"
        kafkaTemplate.send("notification.events", recipient.getUserId().toString(), event);
        
        log.info("Published notification event {} for user {}", 
            notification.getId(), recipient.getUserId());
    }
}
```

### 3.2. Chat Service - Kafka Topic Configuration

```java
// chat/src/main/java/com/theblood/springfood/chat/config/ChatKafkaTopicConfig.java

@Configuration
public class ChatKafkaTopicConfig {
    
    @Bean
    public NewTopic chatMessageTopic() {
        return TopicBuilder.name("chat.messages")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic notificationEventTopic() {
        return TopicBuilder.name("notification.events")
            .partitions(3)
            .replicas(1)
            .build();
    }
}
```

### 3.3. Chat Service - Notification Event Consumer

```java
// chat/src/main/java/com/theblood/springfood/chat/service/kafka/NotificationEventConsumer.java

@Service
@Slf4j
public class NotificationEventConsumer {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private NotificationDeliveryTracker deliveryTracker;
    
    @KafkaListener(
        topics = "notification.events",
        groupId = "chat-service-notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotificationEvent(
        @Payload NotificationEvent event,
        @Header(KafkaHeaders.RECEIVED_KEY) String key,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received notification event: type={}, notificationId={}, userId={}", 
            event.getType(), event.getNotificationId(), event.getUserId());
        
        try {
            if ("NOTIFICATION_CREATED".equals(event.getType())) {
                pushNotificationToUser(event);
            }
        } catch (Exception e) {
            log.error("Error processing notification event: {}", event, e);
            // Error handling: retry, DLQ, etc.
        }
    }
    
    private void pushNotificationToUser(NotificationEvent event) {
        Long userId = event.getUserId();
        
        // 1. Check if user is online
        boolean isOnline = checkUserOnline(userId);
        
        if (!isOnline) {
            log.debug("User {} is offline, notification will be fetched on next login", userId);
            return;
        }
        
        // 2. Build notification message
        NotificationMessage message = NotificationMessage.builder()
            .id(event.getNotificationId())
            .type(event.getNotificationType())
            .title(event.getTitle())
            .body(event.getBody())
            .data(event.getData())
            .createdAt(event.getCreatedAt())
            .build();
        
        // 3. Push via WebSocket
        try {
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                message
            );
            
            log.info("Pushed notification {} to user {} via WebSocket", 
                event.getNotificationId(), userId);
            
            // 4. Track delivery
            deliveryTracker.markAsDelivered(event.getNotificationId(), userId);
            
            // 5. Invalidate cache
            invalidateUnreadCountCache(userId);
            
        } catch (Exception e) {
            log.error("Failed to push notification via WebSocket: {}", e.getMessage(), e);
        }
    }
    
    private boolean checkUserOnline(Long userId) {
        String key = "user:online:" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    private void invalidateUnreadCountCache(Long userId) {
        String key = "notification:unread:" + userId;
        redisTemplate.delete(key);
    }
}
```

### 3.4. Chat Service - WebSocket Configuration (Mở rộng)

```java
// chat/src/main/java/com/theblood/springfood/chat/config/WebSocketConfig.java

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker
        config.enableSimpleBroker("/topic", "/queue");
        
        // Application destination prefixes
        config.setApplicationDestinationPrefixes("/app");
        
        // User destination prefix
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint cho chat (existing)
        registry.addEndpoint("/ws/chat")
            .setAllowedOriginPatterns("*")
            .withSockJS();
        
        // Endpoint cho notifications (có thể dùng chung hoặc riêng)
        // Option 1: Dùng chung endpoint /ws/chat
        // Option 2: Tạo endpoint riêng /ws/notifications (nhưng vẫn cùng server)
        registry.addEndpoint("/ws/notifications")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new WebSocketAuthInterceptor());
    }
}
```

**Lưu ý**: Client có thể:
- Option 1: Connect đến `/ws/chat` và subscribe cả `/user/queue/chat` và `/user/queue/notifications`
- Option 2: Connect đến `/ws/notifications` riêng (nhưng vẫn cùng Chat Service)

Recommend: **Option 1** - Dùng 1 connection cho cả chat và notification


### 3.5. Chat Service - Notification Delivery Tracker

```java
// chat/src/main/java/com/theblood/springfood/chat/service/NotificationDeliveryTracker.java

@Service
@Slf4j
public class NotificationDeliveryTracker {
    
    @Autowired
    private KafkaTemplate<String, NotificationDeliveryEvent> kafkaTemplate;
    
    public void markAsDelivered(Long notificationId, Long userId) {
        // Publish delivery confirmation back to Kafka
        // Notification Service có thể consume để update delivery status
        NotificationDeliveryEvent event = NotificationDeliveryEvent.builder()
            .notificationId(notificationId)
            .userId(userId)
            .channel("IN_APP")
            .status("DELIVERED")
            .deliveredAt(Instant.now())
            .build();
        
        kafkaTemplate.send("notification.delivery", notificationId.toString(), event);
        
        log.debug("Marked notification {} as delivered to user {}", notificationId, userId);
    }
}
```

### 3.6. Client Implementation

```javascript
// Frontend - React/Vue/Angular

class NotificationWebSocketClient {
    constructor() {
        this.stompClient = null;
        this.connected = false;
    }
    
    connect(token) {
        const socket = new SockJS('http://chat-service:8080/ws/chat');
        this.stompClient = Stomp.over(socket);
        
        const headers = {
            'Authorization': `Bearer ${token}`
        };
        
        this.stompClient.connect(headers, (frame) => {
            console.log('Connected to WebSocket:', frame);
            this.connected = true;
            
            // Subscribe to chat messages
            this.stompClient.subscribe('/user/queue/chat', (message) => {
                this.handleChatMessage(JSON.parse(message.body));
            });
            
            // Subscribe to notifications
            this.stompClient.subscribe('/user/queue/notifications', (message) => {
                this.handleNotification(JSON.parse(message.body));
            });
            
            // Subscribe to typing indicators
            this.stompClient.subscribe('/user/queue/typing', (message) => {
                this.handleTypingIndicator(JSON.parse(message.body));
            });
        }, (error) => {
            console.error('WebSocket connection error:', error);
            this.reconnect(token);
        });
    }
    
    handleNotification(notification) {
        console.log('Received notification:', notification);
        
        // Display toast/banner
        this.showToast({
            title: notification.title,
            body: notification.body,
            type: this.getNotificationType(notification.type),
            duration: 5000
        });
        
        // Update notification badge
        this.updateNotificationBadge();
        
        // Play sound
        if (this.shouldPlaySound(notification.type)) {
            this.playNotificationSound();
        }
        
        // Store in local state
        this.addNotificationToList(notification);
    }
    
    showToast(options) {
        // Implementation depends on UI library
        // Example with react-toastify:
        toast.success(options.body, {
            position: "top-right",
            autoClose: options.duration,
            hideProgressBar: false,
        });
    }
    
    updateNotificationBadge() {
        // Fetch unread count from API
        fetch('/api/notifications/unread-count')
            .then(res => res.json())
            .then(data => {
                document.getElementById('notification-badge').textContent = data.count;
            });
    }
    
    reconnect(token) {
        setTimeout(() => {
            console.log('Attempting to reconnect...');
            this.connect(token);
        }, 5000);
    }
    
    disconnect() {
        if (this.stompClient && this.connected) {
            this.stompClient.disconnect();
            this.connected = false;
        }
    }
}

// Usage
const wsClient = new NotificationWebSocketClient();
wsClient.connect(authToken);
```

---

## 4. Kafka Topics Structure

```yaml
Topics trong hệ thống:

1. order.events
   Purpose: Business events từ Order Service
   Producers: order-service
   Consumers: notification-service, analytics-service
   Partitions: 3
   Retention: 7 days
   
2. shipping.events
   Purpose: Business events từ Shipping Service
   Producers: shipping-service
   Consumers: notification-service, order-service
   Partitions: 3
   Retention: 7 days
   
3. notification.events
   Purpose: Notification events để push qua WebSocket
   Producers: notification-service
   Consumers: chat-service (WebSocket push)
   Partitions: 3
   Retention: 1 day
   Message Format:
     {
       type: "NOTIFICATION_CREATED",
       notificationId: 123,
       userId: 456,
       notificationType: "ORDER_PAID",
       title: "...",
       body: "...",
       data: {...},
       createdAt: "2024-02-23T10:30:00Z"
     }
   
4. notification.delivery
   Purpose: Tracking notification delivery status
   Producers: chat-service
   Consumers: notification-service (update delivery status)
   Partitions: 3
   Retention: 3 days
   Message Format:
     {
       notificationId: 123,
       userId: 456,
       channel: "IN_APP",
       status: "DELIVERED",
       deliveredAt: "2024-02-23T10:30:01Z"
     }
   
5. chat.messages
   Purpose: Chat messages (existing)
   Producers: chat-service
   Consumers: chat-service (broadcast, persistence)
   Partitions: 3
   Retention: 7 days
```

---

## 5. Sequence Diagram - Complete Flow

```
Customer  OrderService  Kafka(order)  NotificationSvc  Kafka(notif)  ChatService  WebSocket  Client
   │           │             │               │               │             │           │         │
   │──Pay─────>│             │               │               │             │           │         │
   │           │             │               │               │             │           │         │
   │           │──Update DB  │               │               │             │           │         │
   │           │             │               │               │             │           │         │
   │           │──Publish───>│               │               │             │           │         │
   │           │  ORDER_PAID │               │               │             │           │         │
   │           │             │               │               │             │           │         │
   │           │             │──Consume─────>│               │             │           │         │
   │           │             │               │               │             │           │         │
   │           │             │               │──Process      │             │           │         │
   │           │             │               │  Map Event    │             │           │         │
   │           │             │               │  Check Prefs  │             │           │         │
   │           │             │               │  Render       │             │           │         │
   │           │             │               │               │             │           │         │
   │           │             │               │──Save to DB   │             │           │         │
   │           │             │               │               │             │           │         │
   │           │             │               │──Publish─────>│             │           │         │
   │           │             │               │  NOTIFICATION │             │           │         │
   │           │             │               │  _CREATED     │             │           │         │
   │           │             │               │               │             │           │         │
   │           │             │               │               │──Consume───>│           │         │
   │           │             │               │               │             │           │         │
   │           │             │               │               │             │──Check    │         │
   │           │             │               │               │             │  Online   │         │
   │           │             │               │               │             │           │         │
   │           │             │               │               │             │──Push────>│         │
   │           │             │               │               │             │  via WS   │         │
   │           │             │               │               │             │           │         │
   │           │             │               │               │             │           │──Show──>│
   │           │             │               │               │             │           │  Toast  │
   │           │             │               │               │             │           │         │
   │           │             │               │               │<────Publish─│           │         │
   │           │             │               │               │  DELIVERED  │           │         │
   │           │             │               │               │             │           │         │
   │           │             │               │<──Consume─────│             │           │         │
   │           │             │               │               │             │           │         │
   │           │             │               │──Update       │             │           │         │
   │           │             │               │  Delivery     │             │           │         │
   │           │             │               │  Status       │             │           │         │
```


---

## 6. So Sánh: Kiến Trúc Cũ vs Mới

### 6.1. Kiến Trúc Cũ (Separate WebSocket Server)

```
❌ KHÔNG TỐI ƯU

Client
  ├─ WebSocket Connection 1 → Chat Service (cho chat)
  └─ WebSocket Connection 2 → Notification Service (cho notifications)

Nhược điểm:
- Client phải maintain 2 WebSocket connections
- Tốn bandwidth và battery (mobile)
- Phức tạp hơn trong client code
- 2 servers phải maintain WebSocket infrastructure
- Tốn tài nguyên server
```

### 6.2. Kiến Trúc Mới (Shared WebSocket Server)

```
✅ TỐI ƯU

Client
  └─ WebSocket Connection 1 → Chat Service
       ├─ Subscribe: /user/queue/chat (chat messages)
       ├─ Subscribe: /user/queue/notifications (notifications)
       └─ Subscribe: /user/queue/typing (typing indicators)

Ưu điểm:
- Client chỉ cần 1 WebSocket connection
- Tiết kiệm bandwidth và battery
- Đơn giản hơn trong client code
- Chỉ 1 server maintain WebSocket infrastructure
- Tiết kiệm tài nguyên server
- Dễ dàng scale (scale Chat Service là đủ)
```

---

## 7. Vai Trò Của Từng Service

### 7.1. Notification Service

```
RESPONSIBILITIES:
├─ Consume business events từ Kafka (order.events, shipping.events, etc.)
├─ Process và map events → notification types
├─ Check user preferences
├─ Render notification templates
├─ Save notifications to PostgreSQL
├─ Publish notification events to Kafka (notification.events)
├─ Send email notifications
├─ Send push notifications (FCM/APNS)
└─ Provide REST API để query notifications

KHÔNG LÀM:
✗ Maintain WebSocket connections
✗ Push realtime notifications trực tiếp
✗ Track user online status
```

### 7.2. Chat Service

```
RESPONSIBILITIES:
├─ Handle chat messages (existing)
├─ Maintain WebSocket connections
├─ Track user online status (Redis)
├─ Consume notification events từ Kafka (notification.events)
├─ Push notifications qua WebSocket
├─ Publish delivery confirmations (notification.delivery)
└─ Handle typing indicators, read receipts, etc.

THÊM CHỨC NĂNG:
+ Consume notification.events topic
+ Push notifications qua WebSocket
+ Track notification delivery
```

### 7.3. Business Services (Order, Shipping, etc.)

```
RESPONSIBILITIES:
├─ Handle business logic
├─ Update database
└─ Publish domain events to Kafka

KHÔNG LÀM:
✗ Gọi trực tiếp Notification Service
✗ Quan tâm đến notification logic
```

---

## 8. Configuration Files

### 8.1. Notification Service - application.yml

```yaml
# notification/src/main/resources/config/application.yml

spring:
  application:
    name: notification-service
  
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    
    # Consumer config - consume business events
    consumer:
      group-id: notification-service-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
    
    # Producer config - produce notification events
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      
  datasource:
    url: jdbc:postgresql://localhost:5432/notification_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

# Email configuration
notification:
  email:
    provider: sendgrid
    api-key: ${SENDGRID_API_KEY}
    from: noreply@springfood.com
    
  # Push notification configuration
  push:
    fcm:
      server-key: ${FCM_SERVER_KEY}
    apns:
      key-id: ${APNS_KEY_ID}
      team-id: ${APNS_TEAM_ID}
```

### 8.2. Chat Service - application.yml (Updated)

```yaml
# chat/src/main/resources/config/application.yml

spring:
  application:
    name: chat-service
  
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    
    consumer:
      group-id: chat-service-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
    
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    
# WebSocket configuration
websocket:
  allowed-origins: "*"
  heartbeat:
    interval: 25000  # 25 seconds
    timeout: 30000   # 30 seconds
```

---

## 9. REST APIs

### 9.1. Notification Service APIs

```java
// GET /api/notifications - Lấy danh sách notifications
@GetMapping("/api/notifications")
public ResponseEntity<Page<NotificationDTO>> getNotifications(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(required = false) Boolean unread,
    Principal principal
) {
    Long userId = Long.parseLong(principal.getName());
    Page<NotificationDTO> notifications = notificationService.getUserNotifications(
        userId, unread, PageRequest.of(page, size)
    );
    return ResponseEntity.ok(notifications);
}

// GET /api/notifications/unread-count - Lấy số lượng chưa đọc
@GetMapping("/api/notifications/unread-count")
public ResponseEntity<UnreadCountDTO> getUnreadCount(Principal principal) {
    Long userId = Long.parseLong(principal.getName());
    long count = notificationService.getUnreadCount(userId);
    return ResponseEntity.ok(new UnreadCountDTO(count));
}

// PUT /api/notifications/{id}/read - Đánh dấu đã đọc
@PutMapping("/api/notifications/{id}/read")
public ResponseEntity<Void> markAsRead(
    @PathVariable Long id,
    Principal principal
) {
    Long userId = Long.parseLong(principal.getName());
    notificationService.markAsRead(id, userId);
    return ResponseEntity.ok().build();
}

// PUT /api/notifications/read-all - Đánh dấu tất cả đã đọc
@PutMapping("/api/notifications/read-all")
public ResponseEntity<Void> markAllAsRead(Principal principal) {
    Long userId = Long.parseLong(principal.getName());
    notificationService.markAllAsRead(userId);
    return ResponseEntity.ok().build();
}

// DELETE /api/notifications/{id} - Xoá notification
@DeleteMapping("/api/notifications/{id}")
public ResponseEntity<Void> deleteNotification(
    @PathVariable Long id,
    Principal principal
) {
    Long userId = Long.parseLong(principal.getName());
    notificationService.deleteNotification(id, userId);
    return ResponseEntity.noContent().build();
}

// GET /api/notifications/preferences - Lấy user preferences
@GetMapping("/api/notifications/preferences")
public ResponseEntity<List<NotificationPreferenceDTO>> getPreferences(Principal principal) {
    Long userId = Long.parseLong(principal.getName());
    List<NotificationPreferenceDTO> prefs = preferenceService.getUserPreferences(userId);
    return ResponseEntity.ok(prefs);
}

// PUT /api/notifications/preferences - Cập nhật preferences
@PutMapping("/api/notifications/preferences")
public ResponseEntity<Void> updatePreferences(
    @RequestBody List<NotificationPreferenceDTO> preferences,
    Principal principal
) {
    Long userId = Long.parseLong(principal.getName());
    preferenceService.updatePreferences(userId, preferences);
    return ResponseEntity.ok().build();
}
```

---

## 10. Error Handling & Retry

### 10.1. Kafka Consumer Error Handling

```java
// chat/src/main/java/com/theblood/springfood/chat/config/KafkaErrorHandler.java

@Component
@Slf4j
public class KafkaErrorHandler implements ConsumerAwareListenerErrorHandler {
    
    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer) {
        log.error("Error processing Kafka message: {}", message, exception);
        
        // Log to monitoring system
        // Send alert if critical
        
        // Decide: retry, skip, or send to DLQ
        if (isRetryable(exception)) {
            // Retry logic
            return retryMessage(message, consumer);
        } else {
            // Send to Dead Letter Queue
            sendToDLQ(message);
            return null;
        }
    }
    
    private boolean isRetryable(Exception e) {
        // Network errors, temporary failures → retry
        // Parsing errors, validation errors → skip
        return e instanceof TransientDataAccessException 
            || e instanceof MessagingException;
    }
}
```

### 10.2. WebSocket Connection Retry (Client)

```javascript
class WebSocketClient {
    constructor() {
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.reconnectDelay = 1000; // Start with 1 second
    }
    
    connect(token) {
        // ... connection logic
        
        this.stompClient.connect(headers, 
            (frame) => {
                // Success
                this.reconnectAttempts = 0;
                this.reconnectDelay = 1000;
                this.onConnected(frame);
            },
            (error) => {
                // Error
                this.onError(error);
                this.scheduleReconnect(token);
            }
        );
    }
    
    scheduleReconnect(token) {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.error('Max reconnect attempts reached');
            this.onMaxReconnectAttemptsReached();
            return;
        }
        
        this.reconnectAttempts++;
        
        // Exponential backoff
        const delay = Math.min(
            this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1),
            30000 // Max 30 seconds
        );
        
        console.log(`Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts})`);
        
        setTimeout(() => {
            this.connect(token);
        }, delay);
    }
}
```

---

## 11. Monitoring & Metrics

### 11.1. Metrics to Track

```yaml
Notification Service Metrics:
  - notifications_created_total (by type)
  - notifications_processed_duration_seconds
  - notification_events_published_total
  - email_sent_total (success/failure)
  - push_sent_total (success/failure)
  
Chat Service Metrics:
  - websocket_connections_active
  - websocket_connections_total
  - notification_events_consumed_total
  - notifications_pushed_total (success/failure)
  - websocket_push_duration_seconds
  
Kafka Metrics:
  - kafka_consumer_lag (by topic, by consumer group)
  - kafka_messages_consumed_total
  - kafka_messages_produced_total
```

### 11.2. Health Checks

```java
// notification/src/main/java/com/theblood/notification/health/NotificationHealthIndicator.java

@Component
public class NotificationHealthIndicator implements HealthIndicator {
    
    @Autowired
    private KafkaTemplate kafkaTemplate;
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try {
            // Check Kafka connectivity
            kafkaTemplate.send("health-check", "ping").get(5, TimeUnit.SECONDS);
            
            // Check database connectivity
            try (Connection conn = dataSource.getConnection()) {
                conn.isValid(5);
            }
            
            return Health.up()
                .withDetail("kafka", "UP")
                .withDetail("database", "UP")
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withException(e)
                .build();
        }
    }
}
```

---

## 12. Tổng Kết

### 12.1. Luồng Tổng Quát

```
1. Business Service → Publish event to Kafka (order.events)
2. Notification Service → Consume event, process, save DB, publish to Kafka (notification.events)
3. Chat Service → Consume notification event, push via WebSocket
4. Client → Receive notification, display toast
```

### 12.2. Lợi Ích Của Kiến Trúc Này

```
✅ Tái sử dụng WebSocket infrastructure của Chat Service
✅ Client chỉ cần 1 WebSocket connection
✅ Tiết kiệm tài nguyên server và client
✅ Decoupling giữa các services qua Kafka
✅ Dễ dàng scale từng component độc lập
✅ Reliable delivery với Kafka persistence
✅ Flexible: Dễ thêm channels mới (email, push, SMS)
```

### 12.3. Trade-offs

```
⚠️ Chat Service phụ thuộc vào Notification Service (qua Kafka)
   → Giải pháp: Kafka đảm bảo reliability, có retry mechanism
   
⚠️ Chat Service phải handle thêm notification logic
   → Giải pháp: Logic đơn giản, chỉ consume và push, không có business logic
   
⚠️ Nếu Chat Service down, notifications không push realtime
   → Giải pháp: Client fetch notifications khi reconnect, có fallback mechanism
```

### 12.4. Implementation Checklist

```
Phase 1: Notification Service Core
□ Setup Kafka consumers cho business events
□ Implement notification processor
□ Setup PostgreSQL schema
□ Implement template service
□ Implement user preference service
□ Implement Kafka producer cho notification.events

Phase 2: Chat Service Integration
□ Add Kafka consumer cho notification.events
□ Implement notification push logic
□ Update WebSocket config (nếu cần)
□ Implement delivery tracking
□ Test WebSocket push

Phase 3: Additional Channels
□ Implement email service
□ Implement push notification service (FCM/APNS)
□ Implement retry logic

Phase 4: Client Integration
□ Update client WebSocket connection
□ Subscribe to /user/queue/notifications
□ Implement notification display (toast/banner)
□ Implement notification list UI
□ Implement preferences UI

Phase 5: Testing & Monitoring
□ Unit tests
□ Integration tests
□ Load testing
□ Setup metrics collection
□ Setup alerting
□ Deploy to production
```

---

**Kiến trúc này tối ưu hơn vì tái sử dụng WebSocket infrastructure đã có, giảm complexity và tài nguyên, đồng thời vẫn đảm bảo tính decoupling và scalability thông qua Kafka.**
