# Common Module - Infrastructure Usage Guide

## Overview

Common module cung cấp centralized configuration cho Kafka và Email, giúp tất cả services trong SpringFood microservices dùng chung infrastructure một cách nhất quán.

---

## 1. Kafka Configuration

### 1.1. Auto-Configuration

Kafka được tự động config khi service depend vào common module. Không cần tạo KafkaConfig riêng nữa!

### 1.2. Setup trong Service

**Step 1**: Đảm bảo dependency

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.theblood</groupId>
    <artifactId>common</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

**Step 2**: Config trong application.yml

```yaml
# src/main/resources/config/application.yml
springfood:
  kafka:
    enabled: true
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id-prefix: notification-service  # Tên service của bạn
```

**Step 3**: Sử dụng trong code

#### Producer (Gửi message)

```java
@Service
public class OrderService {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;  // Auto-injected
    
    public void publishOrderEvent(OrderEvent event) {
        kafkaTemplate.send("order.events", event.getOrderId().toString(), event);
    }
}
```

#### Consumer (Nhận message)

```java
@Service
@Slf4j
public class OrderEventConsumer {
    
    @KafkaListener(
        topics = "order.events",
        groupId = "notification-service-group"
    )
    public void consumeOrderEvent(@Payload OrderEvent event, Acknowledgment ack) {
        log.info("Received order event: {}", event);
        
        try {
            // Process event
            processOrder(event);
            
            // Manual commit
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process order event", e);
            // Don't acknowledge - message will be redelivered
        }
    }
}
```

### 1.3. Configuration Properties

Các properties có thể override trong application.yml:

```yaml
springfood:
  kafka:
    enabled: true  # Enable/disable Kafka
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      retries: 3
      acks: all
      batch-size: 16384
      linger-ms: 10
      enable-idempotence: true
      max-in-flight-requests-per-connection: 5
    consumer:
      group-id-prefix: my-service
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      enable-auto-commit: false
      trusted-packages: "*"
```

---

## 2. Email Configuration

### 2.1. Auto-Configuration

Email service được tự động config khi `springfood.email.enabled=true`.

### 2.2. Setup trong Service

**Step 1**: Config trong application.yml

```yaml
# src/main/resources/config/application-dev.yml
springfood:
  email:
    enabled: true
    from: noreply@springfood.com
    from-name: SpringFood Notification
    reply-to: support@springfood.com
    smtp:
      host: ${MAIL_HOST:smtp.gmail.com}
      port: ${MAIL_PORT:587}
      username: ${MAIL_USERNAME}
      password: ${MAIL_PASSWORD}
      auth: true
      starttls-enable: true
```

**Step 2**: Sử dụng trong code

#### Gửi plain text email

```java
@Service
public class NotificationService {
    
    @Autowired
    private EmailService emailService;  // Auto-injected
    
    public void sendWelcomeEmail(String userEmail) {
        EmailDTO email = EmailDTO.builder()
            .to(userEmail)
            .subject("Chào mừng đến với SpringFood")
            .content("Xin chào! Cảm ơn bạn đã đăng ký.")
            .build();
        
        emailService.sendEmail(email);
    }
}
```

#### Gửi HTML email

```java
public void sendOrderConfirmation(String userEmail, Order order) {
    String htmlContent = """
        <h2>Đơn hàng #%s đã được xác nhận</h2>
        <p>Tổng tiền: <strong>%s VNĐ</strong></p>
        <p>Cảm ơn bạn đã mua hàng!</p>
        """.formatted(order.getId(), order.getTotalAmount());
    
    EmailDTO email = EmailDTO.builder()
        .to(userEmail)
        .subject("Xác nhận đơn hàng #" + order.getId())
        .content(htmlContent)
        .isHtml(true)
        .build();
    
    emailService.sendHtmlEmail(email);
}
```

#### Gửi template email (Thymeleaf)

```java
public void sendNotificationEmail(String userEmail, Notification notification) {
    // Data object cho template
    Map<String, Object> data = Map.of(
        "title", notification.getTitle(),
        "body", notification.getBody(),
        "actionUrl", "https://springfood.com/notifications/" + notification.getId(),
        "actionText", "Xem chi tiết"
    );
    
    EmailDTO email = EmailDTO.builder()
        .to(userEmail)
        .subject(notification.getTitle())
        .build();
    
    // Template: common/src/main/resources/templates/email/notification-email.html
    emailService.sendTemplateEmail("notification-email", email, data);
}
```

### 2.3. Tạo Email Template

Tạo file template trong `common/src/main/resources/templates/email/`:

```html
<!-- my-template.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${data.title}">Title</title>
</head>
<body>
    <h1 th:text="${data.title}">Title</h1>
    <p th:text="${data.message}">Message</p>
    <a th:href="${data.link}">Click here</a>
</body>
</html>
```

### 2.4. Gmail Configuration

Để dùng Gmail, cần tạo App Password:

1. Vào Google Account Settings
2. Security → 2-Step Verification
3. App passwords → Generate new password
4. Copy password vào `.env`:

```bash
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-char-app-password
```

---

## 3. Environment Variables

Cập nhật file `.env` ở root project:

```bash
# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_ENABLED=true

# Email Configuration
MAIL_ENABLED=true
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@springfood.com
MAIL_FROM_NAME=SpringFood
```

---

## 4. Migration từ Config Cũ

### 4.1. Xóa Kafka Config Cũ

Nếu service có KafkaConfig riêng, xóa các file sau:

```bash
# Ví dụ trong notification-service
rm src/main/java/com/theblood/notification/config/KafkaProducerConfig.java
rm src/main/java/com/theblood/notification/config/KafkaConsumerConfig.java
```

### 4.2. Update Code

**Trước:**
```java
@Autowired
private KafkaTemplate<String, NotificationEvent> notificationKafkaTemplate;
```

**Sau:**
```java
@Autowired
private KafkaTemplate<String, Object> kafkaTemplate;  // Generic type
```

### 4.3. Update application.yml

**Trước:**
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: ...
      value-serializer: ...
```

**Sau:**
```yaml
springfood:
  kafka:
    enabled: true
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

---

## 5. Testing

### 5.1. Kafka Test

```java
@SpringBootTest
class KafkaIntegrationTest {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Test
    void testKafkaProducer() {
        TestEvent event = new TestEvent("test-data");
        kafkaTemplate.send("test.topic", "key", event);
        
        // Verify message sent
    }
}
```

### 5.2. Email Test

```java
@SpringBootTest
class EmailServiceTest {
    
    @Autowired
    private EmailService emailService;
    
    @Test
    void testSendEmail() {
        EmailDTO email = EmailDTO.builder()
            .to("test@example.com")
            .subject("Test Email")
            .content("This is a test")
            .build();
        
        emailService.sendEmail(email);
        
        // Verify email sent (check logs or use mock)
    }
}
```

---

## 6. Troubleshooting

### Kafka Issues

**Problem**: `Cannot connect to Kafka`
```
Solution: 
- Check KAFKA_BOOTSTRAP_SERVERS in .env
- Verify Kafka is running: docker ps | grep kafka
- Check network connectivity
```

**Problem**: `Consumer not receiving messages`
```
Solution:
- Verify topic exists
- Check consumer group ID
- Ensure manual acknowledgment is called
```

### Email Issues

**Problem**: `Authentication failed`
```
Solution:
- Gmail: Use App Password, not regular password
- Check MAIL_USERNAME and MAIL_PASSWORD in .env
- Verify 2FA is enabled for Gmail
```

**Problem**: `Email not sent (no error)`
```
Solution:
- Check springfood.email.enabled=true
- Verify @EnableAsync is present
- Check application logs for async errors
```

---

## 7. Best Practices

### Kafka

✅ Luôn dùng manual acknowledgment cho consumers
✅ Set appropriate group ID cho mỗi consumer
✅ Handle exceptions properly, don't acknowledge failed messages
✅ Use meaningful topic names (e.g., `order.events`, `notification.events`)
✅ Log all Kafka operations for debugging

### Email

✅ Always use async sending (đã có sẵn trong EmailServiceImpl)
✅ Use templates cho emails phức tạp
✅ Set meaningful subject lines
✅ Include unsubscribe link nếu là marketing email
✅ Test với real email trước khi deploy production

---

## 8. Benefits

### Centralized Configuration

✅ Single source of truth
✅ Consistent configuration across all services
✅ Easy to update (chỉ update common module)
✅ Reduce code duplication
✅ Easier testing và mocking

### Maintainability

✅ Thay đổi config ở 1 chỗ, apply cho tất cả services
✅ Dễ dàng add new features (e.g., retry logic, circuit breaker)
✅ Centralized error handling
✅ Better logging và monitoring

---

## 9. Next Steps

1. ✅ Kafka và Email config đã được tạo trong common module
2. ⏳ Update notification-service để dùng config chung
3. ⏳ Migrate các services khác (order, product, shop, etc.)
4. ⏳ Test integration
5. ⏳ Update documentation
6. ⏳ Deploy to staging/production

---

## Support

Nếu gặp vấn đề, check:
- Application logs
- Kafka broker logs
- Email server logs
- Environment variables trong .env

Hoặc tham khảo `SHARED-INFRASTRUCTURE-CONFIG.md` để biết thêm chi tiết.
