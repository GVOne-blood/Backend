# Shared Infrastructure Configuration Guide

## Tổng quan

Tài liệu này hướng dẫn cấu hình chung cho Kafka, Email và các infrastructure khác được sử dụng bởi nhiều services trong hệ thống SpringFood.

---

## 1. Kafka Configuration - Shared Approach

### 1.1. Vấn đề hiện tại

Hiện tại mỗi service tự config Kafka riêng:
- `order-service/kafka/config/KafkaConfig.java`
- `product-service/kafka/config/KafkaProducerConfig.java`
- `chat/config/KafkaProducerConfig.java`

→ **Duplicate code**, khó maintain, inconsistent configuration

### 1.2. Giải pháp: Centralized Kafka Configuration

#### Option 1: Common Module (RECOMMENDED)

**Tạo Kafka config trong common module** để tất cả services tái sử dụng.

**Location**: `common/src/main/java/com/theblood/springfood/common/config/kafka/`

**Structure**:
```
common/
└── src/main/java/com/theblood/springfood/common/config/kafka/
    ├── KafkaCommonConfig.java          # Base configuration
    ├── KafkaProducerAutoConfiguration.java
    ├── KafkaConsumerAutoConfiguration.java
    └── KafkaProperties.java            # Configuration properties
```

**Implementation**:

```java
// common/src/main/java/com/theblood/springfood/common/config/kafka/KafkaProperties.java
package com.theblood.springfood.common.config.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "springfood.kafka")
public class KafkaProperties {
    private String bootstrapServers = "localhost:9092";
    private Producer producer = new Producer();
    private Consumer consumer = new Consumer();
    
    @Data
    public static class Producer {
        private String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";
        private String valueSerializer = "org.springframework.kafka.support.serializer.JsonSerializer";
        private Integer retries = 3;
        private String acks = "all";
        private Integer batchSize = 16384;
        private Integer lingerMs = 10;
    }
    
    @Data
    public static class Consumer {
        private String groupIdPrefix = "springfood";
        private String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
        private String valueDeserializer = "org.springframework.kafka.support.serializer.JsonDeserializer";
        private String autoOffsetReset = "earliest";
        private Boolean enableAutoCommit = false;
        private String trustedPackages = "*";
    }
}
```

```java
// common/src/main/java/com/theblood/springfood/common/config/kafka/KafkaProducerAutoConfiguration.java
package com.theblood.springfood.common.config.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(KafkaProperties.class)
@ConditionalOnProperty(prefix = "springfood.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KafkaProducerAutoConfiguration {
    
    private final KafkaProperties kafkaProperties;
    
    public KafkaProducerAutoConfiguration(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProperties.getProducer().getKeySerializer());
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProperties.getProducer().getValueSerializer());
        configProps.put(ProducerConfig.RETRIES_CONFIG, kafkaProperties.getProducer().getRetries());
        configProps.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.getProducer().getAcks());
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaProperties.getProducer().getBatchSize());
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, kafkaProperties.getProducer().getLingerMs());
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

```java
// common/src/main/java/com/theblood/springfood/common/config/kafka/KafkaConsumerAutoConfiguration.java
package com.theblood.springfood.common.config.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@EnableConfigurationProperties(KafkaProperties.class)
@ConditionalOnProperty(prefix = "springfood.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumerAutoConfiguration {
    
    private final KafkaProperties kafkaProperties;
    
    public KafkaConsumerAutoConfiguration(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }
    
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaProperties.getConsumer().getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaProperties.getConsumer().getValueDeserializer());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getConsumer().getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaProperties.getConsumer().getEnableAutoCommit());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, kafkaProperties.getConsumer().getTrustedPackages());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.lang.Object");
        
        return new DefaultKafkaConsumerFactory<>(props);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
```

#### 1.3. Cách sử dụng trong từng service

**Step 1**: Đảm bảo service depend vào common module

```xml
<!-- pom.xml của service -->
<dependency>
    <groupId>com.theblood</groupId>
    <artifactId>common</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

**Step 2**: Config trong application.yml

```yaml
# order-service/src/main/resources/config/application.yml
springfood:
  kafka:
    enabled: true
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id-prefix: order-service
```

**Step 3**: Sử dụng trong code

```java
// order-service/src/main/java/com/theblood/orderservice/service/OrderService.java
@Service
public class OrderService {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;  // Auto-injected từ common
    
    public void publishOrderEvent(OrderEvent event) {
        kafkaTemplate.send("order.events", event.getOrderId().toString(), event);
    }
}
```

**Step 4**: Consumer trong service

```java
// notification-service/src/main/java/com/theblood/notification/service/kafka/OrderEventConsumer.java
@Service
@Slf4j
public class OrderEventConsumer {
    
    @KafkaListener(
        topics = "order.events",
        groupId = "notification-service-group"
    )
    public void consumeOrderEvent(@Payload OrderEvent event) {
        log.info("Received order event: {}", event);
        // Process event
    }
}
```

**Step 5**: Xóa các KafkaConfig riêng lẻ

```bash
# Xóa các file config duplicate
rm order-service/src/main/java/com/theblood/orderservice/kafka/config/KafkaConfig.java
rm product-service/src/main/java/com/theblood/productservice/kafka/config/KafkaProducerConfig.java
# etc.
```

---

## 2. Email Configuration - Shared Approach

### 2.1. Vấn đề hiện tại

- Dependency `spring-boot-starter-mail` đã có trong nhiều services
- Nhưng CHƯA có config và implementation
- Cần email cho: notification, order confirmation, password reset, etc.

### 2.2. Giải pháp: Centralized Email Service

#### Option 1: Email Service trong Common Module (RECOMMENDED)

**Location**: `common/src/main/java/com/theblood/springfood/common/service/email/`

**Structure**:
```
common/
└── src/main/java/com/theblood/springfood/common/
    ├── config/email/
    │   ├── EmailProperties.java
    │   └── EmailAutoConfiguration.java
    └── service/email/
        ├── EmailService.java
        ├── EmailServiceImpl.java
        └── dto/
            └── EmailDTO.java
```

**Implementation**:

```java
// common/src/main/java/com/theblood/springfood/common/config/email/EmailProperties.java
package com.theblood.springfood.common.config.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "springfood.email")
public class EmailProperties {
    private boolean enabled = false;
    private String from = "noreply@springfood.com";
    private String fromName = "SpringFood";
    private String replyTo;
    private Smtp smtp = new Smtp();
    private Template template = new Template();
    
    @Data
    public static class Smtp {
        private String host = "smtp.gmail.com";
        private Integer port = 587;
        private String username;
        private String password;
        private boolean auth = true;
        private boolean starttlsEnable = true;
        private boolean starttlsRequired = true;
    }
    
    @Data
    public static class Template {
        private String basePath = "classpath:/templates/email/";
        private String defaultLocale = "vi_VN";
    }
}
```

```java
// common/src/main/java/com/theblood/springfood/common/config/email/EmailAutoConfiguration.java
package com.theblood.springfood.common.config.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(EmailProperties.class)
@ConditionalOnProperty(prefix = "springfood.email", name = "enabled", havingValue = "true")
public class EmailAutoConfiguration {
    
    private final EmailProperties emailProperties;
    
    public EmailAutoConfiguration(EmailProperties emailProperties) {
        this.emailProperties = emailProperties;
    }
    
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        mailSender.setHost(emailProperties.getSmtp().getHost());
        mailSender.setPort(emailProperties.getSmtp().getPort());
        mailSender.setUsername(emailProperties.getSmtp().getUsername());
        mailSender.setPassword(emailProperties.getSmtp().getPassword());
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", emailProperties.getSmtp().isAuth());
        props.put("mail.smtp.starttls.enable", emailProperties.getSmtp().isStarttlsEnable());
        props.put("mail.smtp.starttls.required", emailProperties.getSmtp().isStarttlsRequired());
        props.put("mail.debug", "false");
        
        return mailSender;
    }
}
```

```java
// common/src/main/java/com/theblood/springfood/common/service/email/EmailService.java
package com.theblood.springfood.common.service.email;

import com.theblood.springfood.common.service.email.dto.EmailDTO;

public interface EmailService {
    void sendEmail(EmailDTO emailDTO);
    void sendHtmlEmail(EmailDTO emailDTO);
    void sendTemplateEmail(String templateName, EmailDTO emailDTO, Object context);
}
```

```java
// common/src/main/java/com/theblood/springfood/common/service/email/EmailServiceImpl.java
package com.theblood.springfood.common.service.email;

import com.theblood.springfood.common.config.email.EmailProperties;
import com.theblood.springfood.common.service.email.dto.EmailDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "springfood.email", name = "enabled", havingValue = "true")
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;
    private final SpringTemplateEngine templateEngine;
    
    public EmailServiceImpl(
        JavaMailSender mailSender,
        EmailProperties emailProperties,
        SpringTemplateEngine templateEngine
    ) {
        this.mailSender = mailSender;
        this.emailProperties = emailProperties;
        this.templateEngine = templateEngine;
    }
    
    @Override
    @Async
    public void sendEmail(EmailDTO emailDTO) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailProperties.getFrom());
            message.setTo(emailDTO.getTo());
            message.setSubject(emailDTO.getSubject());
            message.setText(emailDTO.getContent());
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", emailDTO.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", emailDTO.getTo(), e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    @Override
    @Async
    public void sendHtmlEmail(EmailDTO emailDTO) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
            );
            
            helper.setFrom(emailProperties.getFrom(), emailProperties.getFromName());
            helper.setTo(emailDTO.getTo());
            helper.setSubject(emailDTO.getSubject());
            helper.setText(emailDTO.getContent(), true);
            
            if (emailDTO.getCc() != null && emailDTO.getCc().length > 0) {
                helper.setCc(emailDTO.getCc());
            }
            
            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully to: {}", emailDTO.getTo());
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", emailDTO.getTo(), e.getMessage());
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }
    
    @Override
    @Async
    public void sendTemplateEmail(String templateName, EmailDTO emailDTO, Object context) {
        try {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariable("data", context);
            
            String htmlContent = templateEngine.process(templateName, thymeleafContext);
            emailDTO.setContent(htmlContent);
            
            sendHtmlEmail(emailDTO);
        } catch (Exception e) {
            log.error("Failed to send template email: {}", e.getMessage());
            throw new RuntimeException("Failed to send template email", e);
        }
    }
}
```

```java
// common/src/main/java/com/theblood/springfood/common/service/email/dto/EmailDTO.java
package com.theblood.springfood.common.service.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDTO {
    private String to;
    private String[] cc;
    private String subject;
    private String content;
    private boolean isHtml;
}
```

#### 2.3. Cách sử dụng trong từng service

**Step 1**: Config trong application.yml

```yaml
# notification-service/src/main/resources/config/application-dev.yml
springfood:
  email:
    enabled: true
    from: noreply@springfood.com
    from-name: SpringFood Notification
    smtp:
      host: ${MAIL_HOST:smtp.gmail.com}
      port: ${MAIL_PORT:587}
      username: ${MAIL_USERNAME}
      password: ${MAIL_PASSWORD}
      auth: true
      starttls-enable: true
```

**Step 2**: Sử dụng trong code

```java
// notification-service/src/main/java/com/theblood/notification/service/channel/EmailNotificationChannel.java
@Service
public class EmailNotificationChannel {
    
    @Autowired
    private EmailService emailService;  // Auto-injected từ common
    
    public void send(Notification notification, String userEmail) {
        EmailDTO email = EmailDTO.builder()
            .to(userEmail)
            .subject(notification.getTitle())
            .content(notification.getBody())
            .isHtml(true)
            .build();
        
        emailService.sendHtmlEmail(email);
    }
    
    public void sendWithTemplate(Notification notification, String userEmail, Object data) {
        EmailDTO email = EmailDTO.builder()
            .to(userEmail)
            .subject(notification.getTitle())
            .build();
        
        emailService.sendTemplateEmail("notification-email", email, data);
    }
}
```

**Step 3**: Tạo email templates

```html
<!-- common/src/main/resources/templates/email/notification-email.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Thông báo từ SpringFood</title>
</head>
<body>
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2 th:text="${data.title}">Tiêu đề</h2>
        <p th:text="${data.body}">Nội dung</p>
        <a th:href="${data.actionUrl}" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; display: inline-block;">
            Xem chi tiết
        </a>
    </div>
</body>
</html>
```

---

## 3. Environment Variables - Centralized

### 3.1. Update .env file

```bash
# .env (root level)

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

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=123456

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=postgres
DB_PASSWORD=123456
```

### 3.2. Docker Compose Updates

```yaml
# docker-compose.yml
version: '3.8'

services:
  kafka:
    image: confluentinc/cp-kafka:latest
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    depends_on:
      - zookeeper
  
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
  
  # Services sử dụng chung Kafka
  notification-service:
    build: ./notification
    ports:
      - "8095:8095"
    environment:
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - MAIL_HOST=${MAIL_HOST}
      - MAIL_USERNAME=${MAIL_USERNAME}
      - MAIL_PASSWORD=${MAIL_PASSWORD}
    depends_on:
      - kafka
      - postgres
  
  order-service:
    build: ./order-service
    environment:
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - kafka
```

---

## 4. Migration Plan

### Phase 1: Setup Common Module (Week 1)

- [ ] Tạo Kafka config trong common module
- [ ] Tạo Email config trong common module
- [ ] Test trong 1 service (notification-service)
- [ ] Update documentation

### Phase 2: Migrate Services (Week 2)

- [ ] Migrate order-service
- [ ] Migrate product-service
- [ ] Migrate shop-service
- [ ] Migrate payment-service
- [ ] Migrate chat-service

### Phase 3: Cleanup (Week 3)

- [ ] Xóa các config duplicate
- [ ] Update tests
- [ ] Verify tất cả services hoạt động
- [ ] Update deployment scripts

---

## 5. Benefits

### Kafka Centralization
✅ Single source of truth cho Kafka config
✅ Consistent configuration across services
✅ Easy to update (chỉ update common module)
✅ Reduce code duplication
✅ Easier testing và mocking

### Email Centralization
✅ Reusable email service
✅ Template support với Thymeleaf
✅ Async sending
✅ Easy to switch email providers
✅ Centralized error handling

---

## 6. Testing

### Kafka Test

```java
@SpringBootTest
class KafkaIntegrationTest {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Test
    void testKafkaProducer() {
        TestEvent event = new TestEvent("test");
        kafkaTemplate.send("test.topic", event);
        // Assert
    }
}
```

### Email Test

```java
@SpringBootTest
class EmailServiceTest {
    
    @Autowired
    private EmailService emailService;
    
    @Test
    void testSendEmail() {
        EmailDTO email = EmailDTO.builder()
            .to("test@example.com")
            .subject("Test")
            .content("Test content")
            .build();
        
        emailService.sendEmail(email);
        // Assert
    }
}
```

---

## 7. Troubleshooting

### Kafka Issues

**Problem**: Cannot connect to Kafka
```
Solution: Check KAFKA_BOOTSTRAP_SERVERS environment variable
```

**Problem**: Consumer not receiving messages
```
Solution: Check group ID và topic name
```

### Email Issues

**Problem**: Authentication failed
```
Solution: 
- Gmail: Enable "Less secure app access" hoặc use App Password
- Check MAIL_USERNAME và MAIL_PASSWORD
```

**Problem**: Email not sent
```
Solution: Check springfood.email.enabled=true trong config
```

---

## Tóm tắt

1. **Kafka**: Centralize trong common module, tất cả services inject `KafkaTemplate`
2. **Email**: Centralize trong common module, tất cả services inject `EmailService`
3. **Config**: Dùng environment variables trong .env
4. **Migration**: Từng bước migrate services, test kỹ
5. **Cleanup**: Xóa duplicate code sau khi migrate xong

**Lợi ích lớn nhất**: Maintainability, Consistency, Reusability
