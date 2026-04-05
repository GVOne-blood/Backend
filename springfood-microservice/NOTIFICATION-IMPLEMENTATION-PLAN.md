# Implementation Plan - Hệ Thống Thông Báo SpringFood

## 📊 Hiện Trạng (Current State Analysis)

### ✅ Đã Có (Existing Components)

#### 1. Notification Service - Basic Structure
```
✓ JHipster-generated service
✓ Database entity: Notification
✓ Repository: NotificationRepository
✓ Service layer: NotificationService, NotificationQueryService
✓ REST APIs: CRUD operations
✓ Security: JWT authentication
✓ Database: PostgreSQL với schema springfood_notification
```

#### 2. Entity Structure (Notification.java)
```java
✓ notificationId (String, PK)
✓ tableName (String) - Tên bảng liên quan
✓ objectId (String) - ID object liên quan
✓ notificationType (String) - Loại thông báo
✓ eventId (String) - ID event
✓ receiveId (String) - ID người nhận
✓ isActive (Integer) - Trạng thái active
✓ title (String, max 2000) - Tiêu đề
✓ body (String, max 2000) - Nội dung
✓ actionUrl (String, max 200) - URL action
✓ isViewed (Integer) - Đã xem
✓ isClicked (Integer) - Đã click
✓ Audit fields: createdBy, createdDate, lastModifiedBy, lastModifiedDate
```

#### 3. Existing APIs
```
✓ GET /api/notifications - Lấy danh sách notifications của user
✓ GET /api/notifications/unread-count - Đếm số chưa đọc
✓ PUT /api/notifications/{id}/view - Xem notification
✓ PUT /api/notifications/mark-as-read - Đánh dấu đã đọc
✓ PUT /api/notifications/mark-as-clicked - Đánh dấu đã click
✓ DELETE /api/notifications/{id} - Xóa notification
✓ DELETE /api/notifications/batch - Xóa nhiều notifications
```

#### 4. Dependencies (pom.xml)
```
✓ Spring Boot 3.x
✓ Spring Data JPA
✓ Spring Security + OAuth2
✓ PostgreSQL
✓ Liquibase
✓ Spring Cloud OpenFeign
✓ Redisson (Redis client)
✓ Common module (custom)
✓ Client module (custom - Feign wrapper)
```

#### 5. Infrastructure Available in Project
```
✓ Kafka: KafkaTemplate có sẵn ở order-service, product-service, chat-service
✓ Redis: RedisTemplate có sẵn ở product-service, chat-service
✓ WebSocket: WebSocketConfig có sẵn ở chat-service
✓ Feign Client: ClientFactory có sẵn trong client module
✓ Common module: Exception handling, i18n, utilities
```

---

## ❌ Chưa Có (Missing Components)

### 1. Event-Driven Architecture
```
❌ Kafka configuration trong notification-service
❌ Kafka consumers để nhận business events
❌ Kafka producers để publish notification events
❌ Event DTOs và mapping logic
```

### 2. Notification Processing Logic
```
❌ Event → Notification mapping service
❌ Notification template service
❌ User preference service
❌ Notification type definitions (enum/constants)
❌ Recipient determination logic
```

### 3. Multi-Channel Delivery
```
❌ Email notification service
❌ Push notification service (FCM/APNS)
❌ In-app notification via WebSocket (integration với chat-service)
❌ Channel dispatcher
❌ Delivery tracking
```

### 4. Database Schema Extensions
```
❌ notification_preferences table
❌ notification_delivery table
❌ notification_templates table (optional)
❌ notification_types table (optional)
```

### 5. Integration với Business Services
```
❌ Order service → Kafka events
❌ Shipping service → Kafka events
❌ Product service → Kafka events
❌ Shop service → Kafka events
❌ Payment service → Kafka events
```

### 6. Real-time Push
```
❌ Integration với chat-service WebSocket
❌ Kafka topic: notification.events
❌ Chat service consumer cho notification events
```

---

## 🎯 Implementation Plan

### Phase 1: Infrastructure Setup (Week 1)

#### 1.1. Kafka Configuration
**Location**: `notification/src/main/java/com/theblood/notification/config/`

**Tasks**:
- [ ] Tạo `KafkaProducerConfig.java`
  - Bean: `KafkaTemplate<String, Object>`
  - JSON serializer
  - Bootstrap servers từ env variables
  
- [ ] Tạo `KafkaConsumerConfig.java`
  - Consumer factory
  - JSON deserializer
  - Group ID: `notification-service-group`
  
- [ ] Tạo `KafkaTopicConfig.java`
  - Topic: `notification.events` (3 partitions)
  - Topic: `notification.delivery` (3 partitions)

**Config trong application.yml**:
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: notification-service-group
      auto-offset-reset: earliest
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

#### 1.2. Redis Configuration (Optional - for caching)
**Location**: `notification/src/main/java/com/theblood/notification/config/`

**Tasks**:
- [ ] Tạo `RedisConfig.java` (copy pattern từ product-service hoặc chat-service)
- [ ] Bean: `RedisTemplate<String, String>`
- [ ] Connection factory

**Config trong application.yml**:
```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
```

#### 1.3. Database Schema Extensions
**Location**: `notification/src/main/resources/config/liquibase/changelog/`

**Tasks**:
- [ ] Tạo migration: `notification_preferences` table
  ```sql
  CREATE TABLE notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    in_app_enabled BOOLEAN DEFAULT TRUE,
    email_enabled BOOLEAN DEFAULT FALSE,
    push_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, notification_type)
  );
  ```

- [ ] Tạo migration: `notification_delivery` table
  ```sql
  CREATE TABLE notification_delivery (
    id BIGSERIAL PRIMARY KEY,
    notification_id VARCHAR(255) NOT NULL,
    channel VARCHAR(50) NOT NULL, -- IN_APP, EMAIL, PUSH
    status VARCHAR(50) NOT NULL, -- PENDING, SENT, DELIVERED, FAILED
    error_message TEXT,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (notification_id) REFERENCES notification(notification_id)
  );
  ```

- [ ] Tạo migration: Update `notification` table
  ```sql
  ALTER TABLE notification 
    ADD COLUMN data JSONB,
    ADD COLUMN status VARCHAR(50) DEFAULT 'PENDING',
    ADD COLUMN sent_at TIMESTAMP,
    ADD COLUMN read_at TIMESTAMP;
  ```

---

### Phase 2: Core Notification Logic (Week 2)

#### 2.1. Domain Models & DTOs
**Location**: `notification/src/main/java/com/theblood/notification/`

**Tasks**:
- [ ] Tạo `domain/NotificationPreference.java` entity
- [ ] Tạo `domain/NotificationDelivery.java` entity
- [ ] Tạo `service/dto/NotificationEventDTO.java`
- [ ] Tạo `service/dto/NotificationPreferenceDTO.java`
- [ ] Tạo `service/dto/NotificationDeliveryDTO.java`
- [ ] Tạo `constant/NotificationType.java` enum
- [ ] Tạo `constant/NotificationChannel.java` enum
- [ ] Tạo `constant/DeliveryStatus.java` enum

**NotificationType.java** (Example):
```java
public enum NotificationType {
    // Order events
    ORDER_CREATED,
    ORDER_PAID,
    ORDER_CANCELLED,
    ORDER_CONFIRMED,
    
    // Shipping events
    SHIPMENT_CREATED,
    SHIPMENT_PICKED_UP,
    SHIPMENT_IN_TRANSIT,
    SHIPMENT_DELIVERED,
    SHIPMENT_FAILED,
    
    // Product events
    PRODUCT_APPROVED,
    PRODUCT_REJECTED,
    PRODUCT_OUT_OF_STOCK,
    PRODUCT_BACK_IN_STOCK,
    
    // Shop events
    SHOP_APPROVED,
    SHOP_REJECTED,
    SHOP_SUSPENDED,
    
    // Payment events
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    REFUND_COMPLETED,
    
    // Chat events
    NEW_MESSAGE,
    
    // Review events
    NEW_REVIEW
}
```

#### 2.2. Notification Processing Service
**Location**: `notification/src/main/java/com/theblood/notification/service/`

**Tasks**:
- [ ] Tạo `NotificationProcessorService.java`
  - Method: `processBusinessEvent(BusinessEvent event)`
  - Map event → notification types
  - Determine recipients
  - Check user preferences
  - Render notification content
  - Save to database
  - Publish to Kafka (notification.events)

- [ ] Tạo `NotificationTemplateService.java`
  - Method: `render(NotificationType type, Locale locale, Map<String, Object> data)`
  - Template cho từng notification type
  - Support i18n (vi_VN, en_US)

- [ ] Tạo `UserPreferenceService.java`
  - CRUD operations cho preferences
  - Default preferences cho user mới
  - Cache preferences trong Redis

- [ ] Tạo `RecipientDeterminationService.java`
  - Determine recipients based on event
  - Support multiple roles (CUSTOMER, SHOP_OWNER, ADMIN)

---

### Phase 3: Event Consumers (Week 3)

#### 3.1. Kafka Event Consumers
**Location**: `notification/src/main/java/com/theblood/notification/service/kafka/`

**Tasks**:
- [ ] Tạo `OrderEventConsumer.java`
  - Topic: `order.events`
  - Process: ORDER_CREATED, ORDER_PAID, ORDER_CANCELLED, etc.

- [ ] Tạo `ShippingEventConsumer.java`
  - Topic: `shipping.events`
  - Process: SHIPMENT_CREATED, DELIVERED, etc.

- [ ] Tạo `ProductEventConsumer.java`
  - Topic: `product.events`
  - Process: PRODUCT_APPROVED, OUT_OF_STOCK, etc.

- [ ] Tạo `PaymentEventConsumer.java`
  - Topic: `payment.events`
  - Process: PAYMENT_SUCCESS, REFUND_COMPLETED, etc.

**Pattern**:
```java
@Service
@Slf4j
public class OrderEventConsumer {
    
    @Autowired
    private NotificationProcessorService processorService;
    
    @KafkaListener(
        topics = "order.events",
        groupId = "notification-service-group"
    )
    public void consumeOrderEvent(@Payload OrderEvent event) {
        log.info("Received order event: {}", event.getEventType());
        processorService.processBusinessEvent(event);
    }
}
```

#### 3.2. Event DTOs
**Location**: `notification/src/main/java/com/theblood/notification/service/dto/events/`

**Tasks**:
- [ ] Tạo `OrderEvent.java`
- [ ] Tạo `ShippingEvent.java`
- [ ] Tạo `ProductEvent.java`
- [ ] Tạo `PaymentEvent.java`
- [ ] Tạo base class `BusinessEvent.java`

---

### Phase 4: Multi-Channel Delivery (Week 4)

#### 4.1. Channel Dispatcher
**Location**: `notification/src/main/java/com/theblood/notification/service/channel/`

**Tasks**:
- [ ] Tạo `ChannelDispatcher.java`
  - Dispatch notification đến các channels
  - Async execution
  - Error handling

- [ ] Tạo interface `NotificationChannel.java`
  ```java
  public interface NotificationChannel {
      void send(Notification notification, Recipient recipient, NotificationDelivery delivery);
      NotificationChannelType getChannelType();
  }
  ```

#### 4.2. In-App Channel (WebSocket via Kafka)
**Location**: `notification/src/main/java/com/theblood/notification/service/channel/`

**Tasks**:
- [ ] Tạo `InAppNotificationChannel.java`
  - Publish notification event to Kafka topic `notification.events`
  - Chat service sẽ consume và push qua WebSocket

**Pattern**:
```java
@Service
public class InAppNotificationChannel implements NotificationChannel {
    
    @Autowired
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    
    @Override
    public void send(Notification notification, Recipient recipient, NotificationDelivery delivery) {
        NotificationEvent event = NotificationEvent.builder()
            .type("NOTIFICATION_CREATED")
            .notificationId(notification.getNotificationId())
            .userId(recipient.getUserId())
            .title(notification.getTitle())
            .body(notification.getBody())
            .data(notification.getData())
            .createdAt(notification.getCreatedDate())
            .build();
        
        kafkaTemplate.send("notification.events", recipient.getUserId(), event);
        
        updateDeliveryStatus(delivery, DeliveryStatus.SENT);
    }
}
```

#### 4.3. Email Channel
**Location**: `notification/src/main/java/com/theblood/notification/service/channel/`

**Tasks**:
- [ ] Tạo `EmailNotificationChannel.java`
  - Use Spring Mail
  - HTML templates với Thymeleaf
  - Async sending
  - Retry logic

**Config trong application.yml**:
```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

#### 4.4. Push Notification Channel
**Location**: `notification/src/main/java/com/theblood/notification/service/channel/`

**Tasks**:
- [ ] Tạo `PushNotificationChannel.java`
  - FCM for Android
  - APNS for iOS
  - Device token management
  - Retry logic

**Dependencies cần thêm**:
```xml
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

---

### Phase 5: Chat Service Integration (Week 5)

#### 5.1. Chat Service - Notification Consumer
**Location**: `chat/src/main/java/com/theblood/springfood/chat/service/kafka/`

**Tasks**:
- [ ] Tạo `NotificationEventConsumer.java`
  - Topic: `notification.events`
  - Check user online status (Redis)
  - Push qua WebSocket nếu online

**Pattern**:
```java
@Service
@Slf4j
public class NotificationEventConsumer {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @KafkaListener(
        topics = "notification.events",
        groupId = "chat-service-notification-group"
    )
    public void consumeNotificationEvent(@Payload NotificationEvent event) {
        if (isUserOnline(event.getUserId())) {
            messagingTemplate.convertAndSendToUser(
                event.getUserId(),
                "/queue/notifications",
                event
            );
        }
    }
    
    private boolean isUserOnline(String userId) {
        return Boolean.TRUE.equals(
            redisTemplate.hasKey("user:online:" + userId)
        );
    }
}
```

#### 5.2. Chat Service - Kafka Topic Config
**Location**: `chat/src/main/java/com/theblood/springfood/chat/config/`

**Tasks**:
- [ ] Update `ChatKafkaTopicConfig.java`
  - Add topic: `notification.events`

#### 5.3. Chat Service - WebSocket Endpoint
**Location**: `chat/src/main/java/com/theblood/springfood/chat/config/`

**Tasks**:
- [ ] Update `WebSocketConfig.java` (nếu cần)
  - Đảm bảo có endpoint `/ws/notifications` hoặc dùng chung `/ws/chat`
  - Client subscribe: `/user/queue/notifications`

---

### Phase 6: Business Services Integration (Week 6)

#### 6.1. Order Service
**Location**: `order-service/src/main/java/com/theblood/orderservice/`

**Tasks**:
- [ ] Update `OrderServiceImpl.java`
  - Publish events: ORDER_CREATED, ORDER_PAID, ORDER_CANCELLED
  - Use existing KafkaTemplate

**Pattern**:
```java
@Transactional
public void processPaymentSuccess(Long orderId) {
    // Business logic
    order.setStatus(OrderStatus.PAID);
    orderRepository.save(order);
    
    // Publish event
    OrderEvent event = OrderEvent.builder()
        .eventType("ORDER_PAID")
        .orderId(orderId)
        .customerId(order.getCustomerId())
        .shopId(order.getShopId())
        .orderNumber(order.getOrderNumber())
        .totalAmount(order.getTotalAmount())
        .timestamp(Instant.now())
        .build();
    
    kafkaTemplate.send("order.events", orderId.toString(), event);
}
```

#### 6.2. Product Service
**Tasks**:
- [ ] Publish events: PRODUCT_APPROVED, PRODUCT_OUT_OF_STOCK

#### 6.3. Shop Service
**Tasks**:
- [ ] Publish events: SHOP_APPROVED, SHOP_REJECTED

#### 6.4. Payment Service
**Tasks**:
- [ ] Publish events: PAYMENT_SUCCESS, REFUND_COMPLETED

---

### Phase 7: REST APIs Enhancement (Week 7)

#### 7.1. Notification REST Controller
**Location**: `notification/src/main/java/com/theblood/notification/web/rest/`

**Tasks**:
- [ ] Update `NotificationResource.java`
  - Add: `GET /api/notifications/preferences` - Lấy preferences
  - Add: `PUT /api/notifications/preferences` - Cập nhật preferences
  - Add: `GET /api/notifications/sync` - Sync notifications (for offline users)
  - Enhance: `GET /api/notifications` - Add filters (type, status, date range)

#### 7.2. Admin APIs
**Tasks**:
- [ ] Add: `GET /api/admin/notifications/stats` - Statistics
- [ ] Add: `GET /api/admin/notifications/delivery-status` - Delivery tracking
- [ ] Add: `POST /api/admin/notifications/broadcast` - Broadcast notification

---

### Phase 8: Testing & Monitoring (Week 8)

#### 8.1. Unit Tests
**Tasks**:
- [ ] NotificationProcessorServiceTest
- [ ] NotificationTemplateServiceTest
- [ ] UserPreferenceServiceTest
- [ ] Channel tests (InApp, Email, Push)
- [ ] Consumer tests

#### 8.2. Integration Tests
**Tasks**:
- [ ] End-to-end flow test: Order event → Notification → WebSocket
- [ ] Kafka integration test
- [ ] Database integration test

#### 8.3. Monitoring & Metrics
**Location**: `notification/src/main/java/com/theblood/notification/management/`

**Tasks**:
- [ ] Tạo `NotificationMetricsService.java`
  - Metrics: notifications_created_total
  - Metrics: notifications_sent_total (by channel)
  - Metrics: notification_processing_duration
  - Metrics: delivery_failure_rate

#### 8.4. Health Checks
**Tasks**:
- [ ] Tạo `NotificationHealthIndicator.java`
  - Check Kafka connectivity
  - Check database connectivity
  - Check Redis connectivity (if used)

---

## 📋 Configuration Checklist

### Environment Variables Cần Thiết

```bash
# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Redis (optional)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=springfood_notification
DB_USERNAME=postgres
DB_PASSWORD=

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=

# Push Notifications
FCM_SERVER_KEY=
APNS_KEY_ID=
APNS_TEAM_ID=
```

### Docker Compose Updates

```yaml
# Thêm vào docker-compose.yml
services:
  notification:
    image: notification:latest
    ports:
      - "8095:8095"
    environment:
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - REDIS_HOST=redis
      - DB_HOST=postgres
    depends_on:
      - kafka
      - redis
      - postgres
```

---

## 🔄 Integration Flow Summary

```
1. Business Event Occurs
   ↓
2. Business Service publishes to Kafka (order.events, etc.)
   ↓
3. Notification Service consumes event
   ↓
4. Process: Map → Check Preferences → Render → Save DB
   ↓
5. Dispatch to Channels:
   ├─ In-App: Publish to Kafka (notification.events)
   ├─ Email: Send via SMTP
   └─ Push: Send via FCM/APNS
   ↓
6. Chat Service consumes notification.events
   ↓
7. Push to WebSocket if user online
   ↓
8. Client receives notification
```

---

## 📊 Effort Estimation

| Phase | Tasks | Estimated Time | Priority |
|-------|-------|----------------|----------|
| Phase 1: Infrastructure | Kafka, Redis, DB schema | 5 days | HIGH |
| Phase 2: Core Logic | Processing, templates, preferences | 5 days | HIGH |
| Phase 3: Event Consumers | Kafka consumers | 5 days | HIGH |
| Phase 4: Multi-Channel | Email, Push, In-App | 5 days | MEDIUM |
| Phase 5: Chat Integration | WebSocket push | 3 days | HIGH |
| Phase 6: Business Integration | Update services | 5 days | HIGH |
| Phase 7: APIs | REST endpoints | 3 days | MEDIUM |
| Phase 8: Testing | Unit, Integration, E2E | 5 days | HIGH |
| **Total** | | **36 days (~7-8 weeks)** | |

---

## 🎯 Success Criteria

- [ ] User nhận được notification realtime khi có event
- [ ] User có thể config preferences (in-app, email, push)
- [ ] Notification được lưu trữ và query được
- [ ] Support multiple channels (in-app, email, push)
- [ ] Scalable architecture (Kafka, multiple instances)
- [ ] Monitoring và metrics hoạt động
- [ ] Test coverage > 80%
- [ ] Documentation đầy đủ

---

## 📝 Notes

1. **Tái sử dụng Infrastructure**: Dùng Kafka, Redis, WebSocket đã có
2. **Decoupling**: Services không gọi trực tiếp nhau, dùng Kafka
3. **Scalability**: Có thể scale từng component độc lập
4. **Reliability**: Kafka đảm bảo không mất events
5. **Flexibility**: Dễ thêm notification types và channels mới

---

**Plan này là ROADMAP, không phải implementation code. Mỗi phase sẽ được implement từng bước với code review và testing.**
