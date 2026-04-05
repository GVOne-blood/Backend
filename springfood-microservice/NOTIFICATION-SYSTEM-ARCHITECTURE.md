# Kiến trúc Hệ thống Thông báo (Notification System Architecture)

## Tổng quan

Tài liệu này mô tả chi tiết kiến trúc và luồng hoạt động của hệ thống thông báo trong nền tảng SpringFood Marketplace, sử dụng Kafka, WebSocket và các công nghệ liên quan để đảm bảo thông báo realtime, đáng tin cậy và có khả năng mở rộng cao.

---

## 1. Kiến trúc Tổng thể (High-Level Architecture)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         BUSINESS SERVICES LAYER                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ Order Service│  │ Shop Service │  │ Product Svc  │  │ Payment Svc  │   │
│  │              │  │              │  │              │  │              │   │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘   │
│         │                 │                 │                 │            │
│         └─────────────────┴─────────────────┴─────────────────┘            │
│                                   │                                         │
│                          Publish Domain Events                              │
│                                   ▼                                         │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │      KAFKA MESSAGE BUS        │
                    │  Topics: order.events,        │
                    │  shipping.events, etc.        │
                    └───────────────┬───────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │   NOTIFICATION SERVICE        │
                    │  ┌─────────────────────────┐  │
                    │  │  Event Consumers        │  │
                    │  │  - OrderEventConsumer   │  │
                    │  │  - ShippingConsumer     │  │
                    │  │  - PaymentConsumer      │  │
                    │  └──────────┬──────────────┘  │
                    │             ▼                  │
                    │  ┌─────────────────────────┐  │
                    │  │ Notification Processor  │  │
                    │  │ - Event → Notification  │  │
                    │  │ - Template Rendering    │  │
                    │  │ - User Preference Check │  │
                    │  └──────────┬──────────────┘  │
                    │             ▼                  │
                    │  ┌─────────────────────────┐  │
                    │  │  Notification Store     │  │
                    │  │  (PostgreSQL + Redis)   │  │
                    │  └──────────┬──────────────┘  │
                    │             ▼                  │
                    │  ┌─────────────────────────┐  │
                    │  │  Channel Dispatcher     │  │
                    │  │  - In-App               │  │
                    │  │  - Email                │  │
                    │  │  - Push (FCM/APNS)      │  │
                    │  └──────────┬──────────────┘  │
                    └─────────────┼─────────────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │                           │
                    ▼                           ▼
        ┌───────────────────┐       ┌───────────────────┐
        │  WEBSOCKET SERVER │       │  EXTERNAL SERVICES│
        │  (STOMP/SockJS)   │       │  - Email Provider │
        │                   │       │  - FCM/APNS       │
        │  Real-time Push   │       │                   │
        └─────────┬─────────┘       └───────────────────┘
                  │
                  ▼
        ┌───────────────────┐
        │   CLIENT APPS     │
        │  - Web (React)    │
        │  - Mobile (iOS)   │
        │  - Mobile (Android│
        └───────────────────┘
```

---

## 2. Các Thành phần Chính (Core Components)

### 2.1. Business Services (Các Service Nghiệp vụ)

**Vai trò**: Phát sinh các sự kiện nghiệp vụ (domain events) khi có hành động quan trọng xảy ra.

**Ví dụ các service**:
- `order-service`: Quản lý đơn hàng
- `shop-service`: Quản lý shop và sản phẩm
- `payment-service`: Xử lý thanh toán
- `shipping-service`: Quản lý vận chuyển
- `authentication`: Quản lý tài khoản

**Cách hoạt động**:

```java
// Trong OrderService - khi đơn hàng được thanh toán thành công
@Service
public class OrderService {
    
    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    @Transactional
    public void processPaymentSuccess(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // Cập nhật trạng thái đơn hàng
        order.setStatus(OrderStatus.PAID);
        order.setPaymentTime(Instant.now());
        orderRepository.save(order);
        
        // Publish event lên Kafka
        OrderEvent event = OrderEvent.builder()
            .eventType("ORDER_PAID")
            .orderId(order.getId())
            .customerId(order.getCustomerId())
            .shopId(order.getShopId())
            .orderNumber(order.getOrderNumber())
            .totalAmount(order.getTotalAmount())
            .paymentMethod(order.getPaymentMethod())
            .timestamp(Instant.now())
            .metadata(buildMetadata(order))
            .build();
        
        kafkaTemplate.send("order.events", order.getId().toString(), event);
        
        log.info("Published ORDER_PAID event for order: {}", orderId);
    }
}
```

**Lưu ý quan trọng**:
- Service nghiệp vụ KHÔNG gọi trực tiếp Notification Service
- Chỉ publish event lên Kafka và tiếp tục xử lý logic nghiệp vụ
- Đảm bảo tính decoupling và khả năng mở rộng

---

### 2.2. Kafka Message Bus

**Vai trò**: Trung gian truyền tải sự kiện giữa các service, đảm bảo:
- Asynchronous communication
- Event persistence và replay
- High throughput và scalability
- Fault tolerance

**Cấu trúc Topics**:

```yaml
Topics:
  - order.events          # Sự kiện đơn hàng (created, paid, cancelled, etc.)
  - shipping.events       # Sự kiện vận chuyển (picked_up, in_transit, delivered, etc.)
  - payment.events        # Sự kiện thanh toán (pending, success, failed, refund, etc.)
  - account.events        # Sự kiện tài khoản (registered, verified, locked, etc.)
  - product.events        # Sự kiện sản phẩm (created, approved, out_of_stock, etc.)
  - shop.events           # Sự kiện shop (registered, approved, suspended, etc.)
  - chat.events           # Sự kiện chat (new_message, etc.)
  - review.events         # Sự kiện đánh giá (new_review, reported, etc.)

Partitioning Strategy:
  - Key: userId hoặc orderId để đảm bảo ordering
  - Partitions: 3-6 partitions per topic (tuỳ traffic)
  
Retention:
  - 7 days (có thể replay nếu cần)
```

---

### 2.3. Notification Service

Đây là service trung tâm xử lý toàn bộ logic thông báo. Gồm các module con:

#### 2.3.1. Event Consumers

**Vai trò**: Lắng nghe và consume events từ Kafka topics

```java
@Service
@Slf4j
public class OrderEventConsumer {
    
    @Autowired
    private NotificationProcessor notificationProcessor;
    
    @KafkaListener(
        topics = "order.events",
        groupId = "notification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderEvent(
        @Payload OrderEvent event,
        @Header(KafkaHeaders.RECEIVED_KEY) String key,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received order event: type={}, orderId={}, partition={}, offset={}", 
            event.getEventType(), event.getOrderId(), partition, offset);
        
        try {
            // Xử lý event và tạo notification
            notificationProcessor.processOrderEvent(event);
        } catch (Exception e) {
            log.error("Error processing order event: {}", event, e);
            // Error handling: retry, DLQ, alert, etc.
        }
    }
}
```


#### 2.3.2. Notification Processor

**Vai trò**: Xử lý logic nghiệp vụ thông báo
- Map event → notification type
- Xác định người nhận (recipients)
- Kiểm tra user preferences
- Render template
- Lưu vào database
- Dispatch đến các channels

```java
@Service
@Slf4j
public class NotificationProcessor {
    
    @Autowired
    private NotificationTemplateService templateService;
    
    @Autowired
    private UserPreferenceService preferenceService;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private ChannelDispatcher channelDispatcher;
    
    @Transactional
    public void processOrderEvent(OrderEvent event) {
        // 1. Map event type → notification types
        List<NotificationMapping> mappings = getNotificationMappings(event.getEventType());
        
        for (NotificationMapping mapping : mappings) {
            // 2. Xác định recipients dựa trên role
            List<Recipient> recipients = determineRecipients(event, mapping.getTargetRoles());
            
            for (Recipient recipient : recipients) {
                // 3. Kiểm tra user preference
                UserPreference pref = preferenceService.getPreference(
                    recipient.getUserId(), 
                    mapping.getNotificationType()
                );
                
                if (!pref.isEnabled()) {
                    log.debug("User {} disabled notification type {}", 
                        recipient.getUserId(), mapping.getNotificationType());
                    continue;
                }
                
                // 4. Render notification content từ template
                NotificationContent content = templateService.render(
                    mapping.getTemplateKey(),
                    recipient.getLocale(),
                    event.toTemplateData()
                );
                
                // 5. Tạo notification entity
                Notification notification = Notification.builder()
                    .userId(recipient.getUserId())
                    .type(mapping.getNotificationType())
                    .title(content.getTitle())
                    .body(content.getBody())
                    .data(event.getMetadata())
                    .status(NotificationStatus.PENDING)
                    .createdAt(Instant.now())
                    .build();
                
                // 6. Lưu vào database
                notification = notificationRepository.save(notification);
                
                // 7. Dispatch đến các channels
                List<Channel> enabledChannels = pref.getEnabledChannels();
                channelDispatcher.dispatch(notification, enabledChannels, recipient);
            }
        }
    }
    
    private List<Recipient> determineRecipients(OrderEvent event, List<UserRole> roles) {
        List<Recipient> recipients = new ArrayList<>();
        
        for (UserRole role : roles) {
            switch (role) {
                case CUSTOMER:
                    recipients.add(new Recipient(event.getCustomerId(), role, "vi_VN"));
                    break;
                case SHOP_OWNER:
                    recipients.add(new Recipient(event.getShopId(), role, "vi_VN"));
                    break;
                case ADMIN:
                    // Lấy danh sách admin phụ trách
                    recipients.addAll(getResponsibleAdmins(event));
                    break;
            }
        }
        
        return recipients;
    }
}
```


#### 2.3.3. Notification Store (Database Layer)

**PostgreSQL - Persistent Storage**:

```sql
-- Bảng notifications - lưu trữ tất cả thông báo
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    data JSONB,
    status VARCHAR(50) NOT NULL, -- PENDING, SENT, READ, FAILED
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMP,
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_user_status (user_id, status),
    INDEX idx_type (type)
);

-- Bảng notification_delivery - tracking việc gửi qua từng channel
CREATE TABLE notification_delivery (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL REFERENCES notifications(id),
    channel VARCHAR(50) NOT NULL, -- IN_APP, EMAIL, PUSH
    status VARCHAR(50) NOT NULL, -- PENDING, SENT, FAILED, DELIVERED
    error_message TEXT,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    INDEX idx_notification (notification_id),
    INDEX idx_status (status)
);

-- Bảng user_notification_preferences
CREATE TABLE user_notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    channels JSONB, -- {"in_app": true, "email": true, "push": false}
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, notification_type)
);
```

**Redis - Caching & Real-time Data**:

```yaml
Redis Usage:
  # 1. Cache unread count
  Key: "notification:unread:{userId}"
  Type: String
  TTL: 5 minutes
  Value: số lượng thông báo chưa đọc
  
  # 2. Cache recent notifications
  Key: "notification:recent:{userId}"
  Type: List
  TTL: 10 minutes
  Value: List các notification IDs gần nhất
  
  # 3. User online status (cho WebSocket)
  Key: "user:online:{userId}"
  Type: String
  TTL: 30 seconds (heartbeat)
  Value: sessionId
  
  # 4. Notification delivery tracking
  Key: "notification:delivery:{notificationId}"
  Type: Hash
  TTL: 1 hour
  Fields: {channel: status}
```


#### 2.3.4. Channel Dispatcher

**Vai trò**: Gửi thông báo đến các kênh khác nhau (In-App, Email, Push)

```java
@Service
@Slf4j
public class ChannelDispatcher {
    
    @Autowired
    private InAppNotificationChannel inAppChannel;
    
    @Autowired
    private EmailNotificationChannel emailChannel;
    
    @Autowired
    private PushNotificationChannel pushChannel;
    
    @Autowired
    private NotificationDeliveryRepository deliveryRepository;
    
    @Async("notificationExecutor")
    public void dispatch(Notification notification, List<Channel> channels, Recipient recipient) {
        for (Channel channel : channels) {
            NotificationDelivery delivery = NotificationDelivery.builder()
                .notificationId(notification.getId())
                .channel(channel)
                .status(DeliveryStatus.PENDING)
                .createdAt(Instant.now())
                .build();
            
            delivery = deliveryRepository.save(delivery);
            
            try {
                switch (channel) {
                    case IN_APP:
                        inAppChannel.send(notification, recipient, delivery);
                        break;
                    case EMAIL:
                        emailChannel.send(notification, recipient, delivery);
                        break;
                    case PUSH:
                        pushChannel.send(notification, recipient, delivery);
                        break;
                }
            } catch (Exception e) {
                log.error("Failed to send notification via {}: {}", channel, e.getMessage());
                updateDeliveryStatus(delivery, DeliveryStatus.FAILED, e.getMessage());
            }
        }
    }
}
```

---

### 2.4. WebSocket Server (Real-time Communication)

**Vai trò**: Đây là thành phần QUAN TRỌNG NHẤT cho thông báo realtime. WebSocket Server có nhiệm vụ:

1. **Duy trì kết nối persistent** với client (web/mobile)
2. **Push thông báo realtime** ngay khi có sự kiện
3. **Quản lý session** và user online status
4. **Broadcast** thông báo đến đúng user đang online

#### 2.4.1. Cấu hình WebSocket

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker cho các destination /topic và /queue
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix cho các message từ client
        config.setApplicationDestinationPrefixes("/app");
        
        // User-specific destination prefix
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
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


#### 2.4.2. In-App Notification Channel (WebSocket Implementation)

```java
@Service
@Slf4j
public class InAppNotificationChannel {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private NotificationDeliveryRepository deliveryRepository;
    
    public void send(Notification notification, Recipient recipient, NotificationDelivery delivery) {
        try {
            // 1. Kiểm tra user có đang online không
            boolean isOnline = checkUserOnline(recipient.getUserId());
            
            if (isOnline) {
                // 2. Push qua WebSocket đến user cụ thể
                NotificationMessage message = NotificationMessage.builder()
                    .id(notification.getId())
                    .type(notification.getType())
                    .title(notification.getTitle())
                    .body(notification.getBody())
                    .data(notification.getData())
                    .createdAt(notification.getCreatedAt())
                    .build();
                
                // Gửi đến destination: /user/{userId}/queue/notifications
                messagingTemplate.convertAndSendToUser(
                    recipient.getUserId().toString(),
                    "/queue/notifications",
                    message
                );
                
                log.info("Sent in-app notification {} to user {} via WebSocket", 
                    notification.getId(), recipient.getUserId());
                
                // 3. Update delivery status
                updateDeliveryStatus(delivery, DeliveryStatus.SENT);
                
                // 4. Invalidate cache
                invalidateUnreadCountCache(recipient.getUserId());
                
            } else {
                log.debug("User {} is offline, notification will be fetched on next login", 
                    recipient.getUserId());
                updateDeliveryStatus(delivery, DeliveryStatus.PENDING);
            }
            
        } catch (Exception e) {
            log.error("Error sending in-app notification: {}", e.getMessage(), e);
            updateDeliveryStatus(delivery, DeliveryStatus.FAILED, e.getMessage());
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

#### 2.4.3. WebSocket Event Listener

```java
@Component
@Slf4j
public class WebSocketEventListener {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // Lấy userId từ principal (đã authenticate)
        Principal principal = headerAccessor.getUser();
        if (principal != null) {
            Long userId = Long.parseLong(principal.getName());
            
            // Đánh dấu user online trong Redis
            String key = "user:online:" + userId;
            redisTemplate.opsForValue().set(key, sessionId, 30, TimeUnit.SECONDS);
            
            log.info("User {} connected via WebSocket, session: {}", userId, sessionId);
            
            // Gửi các notification pending (nếu có)
            sendPendingNotifications(userId);
        }
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        Principal principal = headerAccessor.getUser();
        if (principal != null) {
            Long userId = Long.parseLong(principal.getName());
            
            // Xoá trạng thái online
            String key = "user:online:" + userId;
            redisTemplate.delete(key);
            
            log.info("User {} disconnected, session: {}", userId, sessionId);
        }
    }
}
```


---

## 3. Luồng Hoạt Động Chi Tiết (Detailed Flow)

### 3.1. Flow Hoàn Chỉnh: Từ Event đến Notification

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ STEP 1: Business Event Occurs                                               │
└─────────────────────────────────────────────────────────────────────────────┘

User đặt hàng và thanh toán thành công
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ OrderService.processPaymentSuccess()                                        │
│  1. Update order status = PAID                                              │
│  2. Save to database                                                        │
│  3. Publish OrderEvent to Kafka topic "order.events"                        │
│     - eventType: "ORDER_PAID"                                               │
│     - orderId, customerId, shopId, amount, etc.                             │
└─────────────────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ STEP 2: Kafka Message Bus                                                   │
│  Topic: order.events                                                        │
│  Partition: based on orderId                                                │
│  Retention: 7 days                                                          │
└─────────────────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ STEP 3: Notification Service Consumes Event                                 │
│  OrderEventConsumer.consumeOrderEvent()                                     │
│   - Receive event from Kafka                                                │
│   - Log event details                                                       │
│   - Call NotificationProcessor                                              │
└─────────────────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ STEP 4: Process & Create Notifications                                      │
│  NotificationProcessor.processOrderEvent()                                  │
│                                                                              │
│  4.1. Map event → notification types                                        │
│       ORDER_PAID → [CUSTOMER_ORDER_PAID, SHOP_NEW_ORDER]                    │
│                                                                              │
│  4.2. For CUSTOMER_ORDER_PAID:                                              │
│       - Recipient: customerId                                               │
│       - Check user preference: enabled? channels?                           │
│       - Render template with order data                                     │
│       - Create Notification entity                                          │
│       - Save to PostgreSQL                                                  │
│       - Dispatch to channels: [IN_APP, EMAIL]                               │
│                                                                              │
│  4.3. For SHOP_NEW_ORDER:                                                   │
│       - Recipient: shopId                                                   │
│       - Check user preference                                               │
│       - Render template                                                     │
│       - Create Notification entity                                          │
│       - Save to PostgreSQL                                                  │
│       - Dispatch to channels: [IN_APP, EMAIL, PUSH]                         │
└─────────────────────────────────────────────────────────────────────────────┘
         │
         ├─────────────────┬─────────────────┬─────────────────┐
         ▼                 ▼                 ▼                 ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   IN-APP     │  │    EMAIL     │  │     PUSH     │  │   IN-APP     │
│  (Customer)  │  │  (Customer)  │  │    (Shop)    │  │   (Shop)     │
└──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ STEP 5: Channel Dispatch - IN-APP (WebSocket)                               │
└─────────────────────────────────────────────────────────────────────────────┘

InAppNotificationChannel.send()
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 5.1. Check user online status in Redis                                      │
│      Key: "user:online:{userId}"                                            │
│      - If EXISTS → User is online                                           │
│      - If NOT EXISTS → User is offline                                      │
└─────────────────────────────────────────────────────────────────────────────┘
         │
         ├─── User ONLINE ───────────────────────────────────────┐
         │                                                        │
         ▼                                                        │
┌─────────────────────────────────────────────────────────┐     │
│ 5.2. Push via WebSocket                                 │     │
│      messagingTemplate.convertAndSendToUser(            │     │
│          userId,                                        │     │
│          "/queue/notifications",                        │     │
│          notificationMessage                            │     │
│      )                                                  │     │
│                                                         │     │
│ 5.3. Update delivery status = SENT                     │     │
│ 5.4. Invalidate unread count cache                     │     │
└─────────────────────────────────────────────────────────┘     │
         │                                                        │
         ▼                                                        │
┌─────────────────────────────────────────────────────────┐     │
│ WebSocket Connection                                    │     │
│  - Protocol: STOMP over WebSocket                       │     │
│  - Destination: /user/{userId}/queue/notifications      │     │
│  - Message delivered to client in real-time             │     │
└─────────────────────────────────────────────────────────┘     │
         │                                                        │
         ▼                                                        │
┌─────────────────────────────────────────────────────────┐     │
│ Client App (Web/Mobile)                                 │     │
│  - Receive notification via WebSocket                   │     │
│  - Display toast/banner                                 │     │
│  - Update notification badge count                      │     │
│  - Play sound (optional)                                │     │
└─────────────────────────────────────────────────────────┘     │
                                                                 │
         ┌───────────────────────────────────────────────────────┘
         │
         └─── User OFFLINE ─────────────────────────────────────┐
                                                                 │
                                                                 ▼
                                        ┌─────────────────────────────────────┐
                                        │ 5.5. Mark delivery status = PENDING │
                                        │      Notification stored in DB      │
                                        │      Will be fetched on next login  │
                                        └─────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ STEP 6: Channel Dispatch - EMAIL                                            │
└─────────────────────────────────────────────────────────────────────────────┘

EmailNotificationChannel.send()
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 6.1. Get user email from database                                           │
│ 6.2. Render HTML email template                                             │
│ 6.3. Send via Email Provider (SendGrid/AWS SES/SMTP)                        │
│ 6.4. Update delivery status = SENT                                          │
│ 6.5. Handle bounce/delivery reports (webhook)                               │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ STEP 7: Channel Dispatch - PUSH (FCM/APNS)                                  │
└─────────────────────────────────────────────────────────────────────────────┘

PushNotificationChannel.send()
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 7.1. Get user device tokens from database                                   │
│ 7.2. Build push notification payload                                        │
│ 7.3. Send to FCM (Android) / APNS (iOS)                                     │
│ 7.4. Update delivery status = SENT                                          │
│ 7.5. Handle delivery reports                                                │
└─────────────────────────────────────────────────────────────────────────────┘
```


---

## 4. Vai Trò của WebSocket trong Hạ Tầng

### 4.1. Tại sao cần WebSocket?

**HTTP Request-Response (Traditional)**:
```
Client ──────────> Server
       (Request)
       
Client <────────── Server
       (Response)
```
- Client phải chủ động gửi request (polling)
- Không hiệu quả cho real-time
- Tốn bandwidth và server resources

**WebSocket (Persistent Connection)**:
```
Client ←─────────→ Server
    (Bidirectional, Full-Duplex)
    
Server ─────────> Client
    (Push anytime)
```
- Kết nối persistent, 2 chiều
- Server có thể push data bất cứ lúc nào
- Hiệu quả cho real-time communication

### 4.2. WebSocket trong Notification System

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    VAI TRÒ CỦA WEBSOCKET SERVER                              │
└─────────────────────────────────────────────────────────────────────────────┘

1. CONNECTION MANAGEMENT (Quản lý kết nối)
   ┌──────────────────────────────────────────────────────────────┐
   │ - Maintain persistent connections với clients                │
   │ - Authenticate users khi connect                             │
   │ - Track user sessions (userId → sessionId mapping)           │
   │ - Handle reconnection logic                                  │
   │ - Heartbeat/ping-pong để giữ connection alive               │
   └──────────────────────────────────────────────────────────────┘

2. REAL-TIME PUSH (Đẩy thông báo realtime)
   ┌──────────────────────────────────────────────────────────────┐
   │ - Nhận notification từ Notification Service                  │
   │ - Push ngay lập tức đến client đang online                   │
   │ - Không cần client phải poll/request                         │
   │ - Latency thấp (< 100ms)                                     │
   └──────────────────────────────────────────────────────────────┘

3. USER PRESENCE (Trạng thái online/offline)
   ┌──────────────────────────────────────────────────────────────┐
   │ - Track user online status                                   │
   │ - Store in Redis: user:online:{userId} → sessionId          │
   │ - TTL 30s, refresh bằng heartbeat                           │
   │ - Notification Service check trước khi push                  │
   └──────────────────────────────────────────────────────────────┘

4. MESSAGE ROUTING (Định tuyến tin nhắn)
   ┌──────────────────────────────────────────────────────────────┐
   │ - Route notification đến đúng user                           │
   │ - Support broadcast (all users)                              │
   │ - Support unicast (specific user)                            │
   │ - Support multicast (group of users)                         │
   └──────────────────────────────────────────────────────────────┘

5. SCALABILITY (Khả năng mở rộng)
   ┌──────────────────────────────────────────────────────────────┐
   │ - Multiple WebSocket server instances                        │
   │ - Load balancer với sticky session                           │
   │ - Redis Pub/Sub để sync giữa các instances                  │
   │ - Horizontal scaling khi traffic tăng                        │
   └──────────────────────────────────────────────────────────────┘
```


### 4.3. WebSocket vs Kafka - Sự Khác Biệt

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         KAFKA vs WEBSOCKET                                   │
└─────────────────────────────────────────────────────────────────────────────┘

KAFKA (Backend Communication)
├─ Mục đích: Service-to-Service communication
├─ Pattern: Event-driven, Pub/Sub
├─ Persistence: Lưu trữ events (7 days retention)
├─ Consumers: Backend services (Notification Service, Analytics, etc.)
├─ Throughput: Rất cao (millions events/second)
├─ Latency: Acceptable (vài ms - vài giây)
├─ Use case: Decouple services, event sourcing, data pipeline
└─ Example: OrderService → Kafka → NotificationService

WEBSOCKET (Client Communication)
├─ Mục đích: Server-to-Client real-time push
├─ Pattern: Bidirectional, Full-Duplex
├─ Persistence: Không lưu trữ (ephemeral)
├─ Consumers: End-user clients (Web, Mobile apps)
├─ Throughput: Moderate (thousands connections/server)
├─ Latency: Rất thấp (< 100ms)
├─ Use case: Real-time notifications, chat, live updates
└─ Example: NotificationService → WebSocket → Client App

┌─────────────────────────────────────────────────────────────────────────────┐
│                    LUỒNG KẾT HỢP KAFKA + WEBSOCKET                           │
└─────────────────────────────────────────────────────────────────────────────┘

OrderService ──[Kafka]──> NotificationService ──[WebSocket]──> Client
     │                           │                                │
     │                           │                                │
  Publish                    Consume                          Receive
  Event                      Process                          Display
                            Push
                            
- Kafka: Reliable, persistent, backend communication
- WebSocket: Fast, real-time, client communication
- Kết hợp: Best of both worlds
```

### 4.4. WebSocket Architecture với Multiple Instances

Khi scale WebSocket server, cần giải quyết vấn đề routing:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              MULTI-INSTANCE WEBSOCKET ARCHITECTURE                           │
└─────────────────────────────────────────────────────────────────────────────┘

                        ┌─────────────────┐
                        │  Load Balancer  │
                        │ (Sticky Session)│
                        └────────┬────────┘
                                 │
                ┌────────────────┼────────────────┐
                │                │                │
                ▼                ▼                ▼
        ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
        │ WS Server 1  │ │ WS Server 2  │ │ WS Server 3  │
        │ Users: A,B,C │ │ Users: D,E,F │ │ Users: G,H,I │
        └──────┬───────┘ └──────┬───────┘ └──────┬───────┘
               │                │                │
               └────────────────┼────────────────┘
                                │
                                ▼
                        ┌──────────────┐
                        │  Redis Pub/Sub│
                        │  Channel:     │
                        │  notifications│
                        └──────────────┘
                                ▲
                                │
                        ┌───────┴───────┐
                        │ Notification  │
                        │   Service     │
                        └───────────────┘

FLOW:
1. User A connects → routed to WS Server 1
2. Notification Service tạo notification cho User A
3. Notification Service publish message to Redis channel "notifications"
4. All WS Servers subscribe to Redis channel
5. WS Server 1 nhận message, check "User A có connect với mình không?"
6. Nếu có → push qua WebSocket đến User A
7. WS Server 2, 3 nhận message nhưng ignore (User A không connect với họ)
```


**Implementation với Redis Pub/Sub**:

```java
@Service
@Slf4j
public class InAppNotificationChannel {
    
    @Autowired
    private RedisTemplate<String, NotificationMessage> redisTemplate;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // Publish notification to Redis channel
    public void send(Notification notification, Recipient recipient, NotificationDelivery delivery) {
        NotificationMessage message = NotificationMessage.builder()
            .id(notification.getId())
            .userId(recipient.getUserId())
            .type(notification.getType())
            .title(notification.getTitle())
            .body(notification.getBody())
            .data(notification.getData())
            .createdAt(notification.getCreatedAt())
            .build();
        
        // Publish to Redis channel
        redisTemplate.convertAndSend("notifications", message);
        
        log.info("Published notification {} to Redis channel", notification.getId());
    }
}

@Component
@Slf4j
public class NotificationRedisSubscriber {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private WebSocketSessionRegistry sessionRegistry;
    
    // Subscribe to Redis channel
    @RedisListener(topics = "notifications")
    public void onMessage(NotificationMessage message) {
        Long userId = message.getUserId();
        
        // Check if user has active WebSocket session on THIS server instance
        if (sessionRegistry.hasSession(userId)) {
            // Push to user via WebSocket
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                message
            );
            
            log.info("Pushed notification {} to user {} via WebSocket", 
                message.getId(), userId);
        } else {
            log.debug("User {} not connected to this WS server instance, skipping", userId);
        }
    }
}

@Component
public class WebSocketSessionRegistry {
    
    private final ConcurrentHashMap<Long, Set<String>> userSessions = new ConcurrentHashMap<>();
    
    public void registerSession(Long userId, String sessionId) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
            .add(sessionId);
    }
    
    public void removeSession(Long userId, String sessionId) {
        Set<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }
    
    public boolean hasSession(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }
}
```

---

## 5. Ví dụ Cụ Thể: Luồng Thông Báo "Đơn Hàng Đã Thanh Toán"

### 5.1. Sequence Diagram

```
Customer    OrderService    Kafka    NotificationService    PostgreSQL    Redis    WebSocket    Client
   │              │            │              │                  │           │          │          │
   │──Pay Order──>│            │              │                  │           │          │          │
   │              │            │              │                  │           │          │          │
   │              │──Update DB─>              │                  │           │          │          │
   │              │            │              │                  │           │          │          │
   │              │──Publish──>│              │                  │           │          │          │
   │              │  ORDER_PAID│              │                  │           │          │          │
   │              │            │              │                  │           │          │          │
   │              │            │──Consume────>│                  │           │          │          │
   │              │            │              │                  │           │          │          │
   │              │            │              │──Map Event       │           │          │          │
   │              │            │              │  to Notifications│           │          │          │
   │              │            │              │                  │           │          │          │
   │              │            │              │──Check Prefs────>│           │          │          │
   │              │            │              │<─────────────────│           │          │          │
   │              │            │              │                  │           │          │          │
   │              │            │              │──Save Notif─────>│           │          │          │
   │              │            │              │<─────────────────│           │          │          │
   │              │            │              │                  │           │          │          │
   │              │            │              │──Check Online───>│           │          │          │
   │              │            │              │<─────────────────│           │          │          │
   │              │            │              │                  │           │          │          │
   │              │            │              │──Publish to Redis────────────>│          │          │
   │              │            │              │                  │           │          │          │
   │              │            │              │                  │           │<─Subscribe│          │
   │              │            │              │                  │           │          │          │
   │              │            │              │                  │           │──Push───>│          │
   │              │            │              │                  │           │          │          │
   │              │            │              │                  │           │          │──Display─>│
   │              │            │              │                  │           │          │  Toast   │
   │<─────────────────────────────────────────────────────────────────────────────────────────────│
```


### 5.2. Chi Tiết Từng Bước

**Bước 1: Customer thanh toán đơn hàng**
```
POST /api/orders/123/payment
{
  "paymentMethod": "CREDIT_CARD",
  "amount": 500000
}
```

**Bước 2: OrderService xử lý**
```java
@Transactional
public void processPayment(Long orderId, PaymentRequest request) {
    // 1. Validate payment
    // 2. Call payment gateway
    // 3. Update order status
    Order order = orderRepository.findById(orderId).orElseThrow();
    order.setStatus(OrderStatus.PAID);
    order.setPaymentTime(Instant.now());
    orderRepository.save(order);
    
    // 4. Publish event
    OrderEvent event = OrderEvent.builder()
        .eventType("ORDER_PAID")
        .orderId(orderId)
        .customerId(order.getCustomerId())
        .shopId(order.getShopId())
        .orderNumber(order.getOrderNumber())
        .totalAmount(order.getTotalAmount())
        .items(order.getItems())
        .timestamp(Instant.now())
        .build();
    
    kafkaTemplate.send("order.events", orderId.toString(), event);
}
```

**Bước 3: Kafka lưu trữ event**
```
Topic: order.events
Partition: 2 (based on orderId hash)
Offset: 12345
Key: "123"
Value: {
  "eventType": "ORDER_PAID",
  "orderId": 123,
  "customerId": 456,
  "shopId": 789,
  "orderNumber": "ORD-2024-001",
  "totalAmount": 500000,
  "timestamp": "2024-02-23T10:30:00Z"
}
```

**Bước 4: NotificationService consume event**
```java
@KafkaListener(topics = "order.events", groupId = "notification-service-group")
public void consumeOrderEvent(OrderEvent event) {
    log.info("Received ORDER_PAID event for order {}", event.getOrderId());
    notificationProcessor.processOrderEvent(event);
}
```

**Bước 5: Xử lý và tạo notifications**
```java
public void processOrderEvent(OrderEvent event) {
    // Map: ORDER_PAID → 2 notifications
    // 1. CUSTOMER_ORDER_PAID (cho customer)
    // 2. SHOP_NEW_ORDER (cho shop owner)
    
    // For Customer
    createNotification(
        userId: event.getCustomerId(),
        type: "CUSTOMER_ORDER_PAID",
        title: "Đơn hàng đã thanh toán thành công",
        body: "Đơn hàng #ORD-2024-001 của bạn đã được thanh toán. Shop đang chuẩn bị hàng.",
        channels: [IN_APP, EMAIL]
    );
    
    // For Shop Owner
    createNotification(
        userId: event.getShopId(),
        type: "SHOP_NEW_ORDER",
        title: "Đơn hàng mới đã thanh toán",
        body: "Bạn có đơn hàng mới #ORD-2024-001 trị giá 500,000đ. Vui lòng chuẩn bị hàng.",
        channels: [IN_APP, EMAIL, PUSH]
    );
}
```

**Bước 6: Lưu vào PostgreSQL**
```sql
INSERT INTO notifications (user_id, type, title, body, data, status, created_at)
VALUES (456, 'CUSTOMER_ORDER_PAID', 'Đơn hàng đã thanh toán thành công', 
        'Đơn hàng #ORD-2024-001...', '{"orderId": 123}', 'PENDING', NOW());
-- Returns id: 9876
```

**Bước 7: Check user online status**
```java
String key = "user:online:456";
boolean isOnline = redisTemplate.hasKey(key);
// Returns: true (user đang online)
```

**Bước 8: Publish to Redis Pub/Sub**
```java
NotificationMessage message = NotificationMessage.builder()
    .id(9876L)
    .userId(456L)
    .type("CUSTOMER_ORDER_PAID")
    .title("Đơn hàng đã thanh toán thành công")
    .body("Đơn hàng #ORD-2024-001...")
    .data(Map.of("orderId", 123))
    .createdAt(Instant.now())
    .build();

redisTemplate.convertAndSend("notifications", message);
```

**Bước 9: WebSocket Server nhận và push**
```java
@RedisListener(topics = "notifications")
public void onMessage(NotificationMessage message) {
    if (sessionRegistry.hasSession(message.getUserId())) {
        messagingTemplate.convertAndSendToUser(
            "456", // userId
            "/queue/notifications",
            message
        );
    }
}
```

**Bước 10: Client nhận notification**
```javascript
// Client đã subscribe: /user/queue/notifications
stompClient.subscribe('/user/queue/notifications', (message) => {
    const notification = JSON.parse(message.body);
    
    // Display toast
    showToast({
        title: notification.title,
        body: notification.body,
        type: 'success'
    });
    
    // Update badge count
    updateNotificationBadge();
    
    // Play sound
    playNotificationSound();
});
```

---

## 6. Các Tình Huống Đặc Biệt

### 6.1. User Offline

```
Khi user offline:
1. Notification vẫn được tạo và lưu vào DB
2. Delivery status = PENDING
3. Khi user login lại:
   - Client gọi API: GET /api/notifications?unread=true
   - Server trả về danh sách notifications chưa đọc
   - Client hiển thị badge count
```

### 6.2. WebSocket Connection Lost

```
Client phát hiện connection lost:
1. Tự động reconnect (exponential backoff)
2. Sau khi reconnect thành công:
   - Gọi API sync: GET /api/notifications/sync?since={lastReceivedTimestamp}
   - Lấy các notifications bị miss trong lúc disconnect
3. Merge với local state
```

### 6.3. High Traffic / Spike

```
Khi có traffic spike (flash sale, big event):
1. Kafka buffer events (không bị mất)
2. Notification Service scale out (thêm consumers)
3. WebSocket Server scale out (load balancer)
4. Redis Pub/Sub handle routing
5. Rate limiting cho email/push channels
6. Priority queue: Critical notifications first
```

### 6.4. Notification Retry

```
Nếu gửi thất bại:
1. Email/Push: Retry với exponential backoff
   - Retry 1: sau 1 phút
   - Retry 2: sau 5 phút
   - Retry 3: sau 15 phút
   - Max retries: 3
2. Sau max retries: Move to Dead Letter Queue (DLQ)
3. Alert admin để investigate
```

---

## 7. Monitoring & Observability

### 7.1. Metrics cần track

```yaml
Kafka Metrics:
  - Consumer lag (notifications chưa được xử lý)
  - Event throughput (events/second)
  - Processing time per event

Notification Metrics:
  - Notifications created (by type, by channel)
  - Delivery success rate (by channel)
  - Delivery latency (time from event to delivery)
  - Failed deliveries (by reason)

WebSocket Metrics:
  - Active connections count
  - Connection duration
  - Messages sent/received
  - Connection errors

Database Metrics:
  - Query performance
  - Unread notifications count
  - Storage growth rate
```

### 7.2. Logging

```java
// Structured logging với correlation ID
log.info("Processing order event", 
    Map.of(
        "correlationId", event.getCorrelationId(),
        "eventType", event.getEventType(),
        "orderId", event.getOrderId(),
        "customerId", event.getCustomerId(),
        "timestamp", event.getTimestamp()
    )
);
```

### 7.3. Alerting

```yaml
Alerts:
  - Consumer lag > 1000: Critical
  - Delivery failure rate > 5%: Warning
  - WebSocket connection errors > 10%: Warning
  - Database query time > 1s: Warning
  - Redis connection lost: Critical
```

---

## 8. Security Considerations

### 8.1. WebSocket Authentication

```java
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                
                // Validate JWT
                if (jwtTokenProvider.validateToken(jwt)) {
                    Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                    accessor.setUser(new UserPrincipal(userId));
                } else {
                    throw new AuthenticationException("Invalid token");
                }
            } else {
                throw new AuthenticationException("Missing token");
            }
        }
        
        return message;
    }
}
```

### 8.2. Authorization

```java
// Đảm bảo user chỉ nhận được notifications của chính họ
public List<Notification> getUserNotifications(Long userId, Principal principal) {
    Long authenticatedUserId = Long.parseLong(principal.getName());
    
    if (!userId.equals(authenticatedUserId)) {
        throw new ForbiddenException("Cannot access other user's notifications");
    }
    
    return notificationRepository.findByUserId(userId);
}
```

### 8.3. Data Privacy

```
- Không log sensitive data (payment details, personal info)
- Encrypt notification data at rest (database encryption)
- Use HTTPS/WSS for transport encryption
- GDPR compliance: Allow users to delete their notifications
```

---

## 9. Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         PRODUCTION DEPLOYMENT                                │
└─────────────────────────────────────────────────────────────────────────────┘

                            ┌──────────────┐
                            │ Load Balancer│
                            │  (Nginx/ALB) │
                            └──────┬───────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │              │              │
                    ▼              ▼              ▼
            ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
            │Notification  │ │Notification  │ │Notification  │
            │Service Pod 1 │ │Service Pod 2 │ │Service Pod 3 │
            └──────┬───────┘ └──────┬───────┘ └──────┬───────┘
                   │                │                │
                   └────────────────┼────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
                    ▼                               ▼
            ┌──────────────┐              ┌──────────────┐
            │   Kafka      │              │  PostgreSQL  │
            │   Cluster    │              │   Primary    │
            │  (3 brokers) │              │  + Replicas  │
            └──────────────┘              └──────────────┘
                    │
                    │
                    ▼
            ┌──────────────┐
            │    Redis     │
            │   Cluster    │
            │ (Sentinel)   │
            └──────────────┘
                    │
                    │
                    ▼
            ┌──────────────┐
            │  WebSocket   │
            │   Servers    │
            │ (3 instances)│
            └──────────────┘

Resources:
- Notification Service: 3 pods, 2 CPU, 4GB RAM each
- Kafka: 3 brokers, 4 CPU, 8GB RAM each
- PostgreSQL: Primary + 2 replicas, 4 CPU, 16GB RAM
- Redis: 3 nodes (Sentinel), 2 CPU, 4GB RAM each
- WebSocket: 3 instances, 2 CPU, 4GB RAM each
```

---

## 10. Tổng Kết

### 10.1. Luồng Tổng Quát

```
Business Event → Kafka → Notification Service → Database + Redis → WebSocket → Client
     (1)          (2)            (3)                  (4)             (5)        (6)

1. Service nghiệp vụ publish event
2. Kafka buffer và distribute events
3. Notification Service consume và xử lý
4. Lưu trữ và cache
5. Push realtime qua WebSocket
6. Client nhận và hiển thị
```

### 10.2. Vai Trò WebSocket

```
WebSocket Server là CẦU NỐI giữa Backend và Client:

Backend (Notification Service)  ←→  WebSocket Server  ←→  Client Apps
         (Kafka, DB)                  (Real-time)         (Web, Mobile)

Nhiệm vụ:
✓ Maintain persistent connections
✓ Push notifications instantly
✓ Track user online status
✓ Route messages correctly
✓ Scale horizontally
✓ Handle reconnections
```

### 10.3. Lợi Ích Của Kiến Trúc Này

```
✓ Decoupling: Services không phụ thuộc trực tiếp
✓ Scalability: Có thể scale từng component độc lập
✓ Reliability: Kafka đảm bảo không mất events
✓ Real-time: WebSocket push ngay lập tức
✓ Flexibility: Dễ dàng thêm channels mới
✓ Observability: Track metrics ở mọi layer
✓ Maintainability: Code rõ ràng, dễ maintain
```

---

## 11. Next Steps - Implementation Checklist

```
Phase 1: Core Infrastructure
□ Setup Kafka cluster
□ Create topics và partitions
□ Setup PostgreSQL database
□ Setup Redis cluster
□ Implement basic event publishing

Phase 2: Notification Service
□ Implement Kafka consumers
□ Implement notification processor
□ Implement template service
□ Implement user preference service
□ Setup database schema

Phase 3: WebSocket Integration
□ Implement WebSocket server
□ Implement authentication
□ Implement session management
□ Implement Redis Pub/Sub
□ Test real-time push

Phase 4: Channels
□ Implement In-App channel
□ Implement Email channel
□ Implement Push channel (FCM/APNS)
□ Implement retry logic

Phase 5: Monitoring & Testing
□ Setup metrics collection
□ Setup logging
□ Setup alerting
□ Load testing
□ End-to-end testing

Phase 6: Production
□ Deploy to staging
□ Performance tuning
□ Security audit
□ Deploy to production
□ Monitor and optimize
```

---

**Tài liệu này cung cấp blueprint hoàn chỉnh để implement hệ thống thông báo với Kafka, WebSocket và các công nghệ liên quan.**
