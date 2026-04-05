# Logging & Monitoring - Implementation Examples

## 📝 Table of Contents
1. [Logging Examples](#logging-examples)
2. [Metrics Examples](#metrics-examples)
3. [Health Checks](#health-checks)
4. [Configuration Examples](#configuration-examples)

---

## 🔍 Logging Examples

### 1. Service Layer Logging

```java
@Service
@Slf4j
public class OrderService {
    
    public Order createOrder(CreateOrderRequest request) {
        // INFO: Business event
        log.info("Creating order: userId={}, items={}", 
            request.getUserId(), request.getItems().size());
        
        LoggingMDCUtil.setUserId(request.getUserId());
        LoggingMDCUtil.setOrderId(UUID.randomUUID().toString());
        
        try {
            // DEBUG: Processing details
            log.debug("Validating order items: {}", request.getItems());
            
            Order order = processOrder(request);
            
            // INFO: Success
            log.info("Order created successfully: orderId={}, total={}", 
                order.getId(), order.getTotal());
            
            return order;
            
        } catch (InsufficientStockException e) {
            // WARN: Expected business exception
            log.warn("Insufficient stock for order: userId={}, items={}", 
                request.getUserId(), request.getItems(), e);
            throw e;
            
        } catch (Exception e) {
            // ERROR: Unexpected error
            log.error("Failed to create order: userId={}", 
                request.getUserId(), e);
            throw new OrderCreationException("Order creation failed", e);
            
        } finally {
            LoggingMDCUtil.clear();
        }
    }
}
```

### 2. Controller Layer Logging

```java
@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody @Valid CreateOrderRequest request,
            @RequestHeader("X-Request-ID") String requestId) {
        
        LoggingMDCUtil.setRequestId(requestId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Received create order request: userId={}", 
                request.getUserId());
            
            Order order = orderService.createOrder(request);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Order created: orderId={}, duration={}ms", 
                order.getId(), duration);
            
            return ResponseEntity.ok(toResponse(order));
            
        } finally {
            LoggingMDCUtil.clear();
        }
    }
}
```

### 3. Kafka Consumer Logging

```java
@Service
@Slf4j
public class OrderEventConsumer {
    
    @KafkaListener(topics = "order-events")
    public void handleOrderEvent(ConsumerRecord<String, OrderEvent> record) {
        OrderEvent event = record.value();
        
        LoggingMDCUtil.setOrderId(event.getOrderId());
        LoggingMDCUtil.setUserId(event.getUserId());
        
        try {
            log.info("Received order event: type={}, orderId={}, partition={}, offset={}", 
                event.getType(), event.getOrderId(), 
                record.partition(), record.offset());
            
            processEvent(event);
            
            log.debug("Order event processed successfully");
            
        } catch (Exception e) {
            log.error("Failed to process order event: orderId={}, offset={}", 
                event.getOrderId(), record.offset(), e);
            // Handle error (DLQ, retry, etc.)
        } finally {
            LoggingMDCUtil.clear();
        }
    }
}
```


### 4. External API Call Logging

```java
@Service
@Slf4j
public class PaymentGatewayClient {
    
    private final RestTemplate restTemplate;
    
    public PaymentResponse processPayment(PaymentRequest request) {
        String paymentId = UUID.randomUUID().toString();
        LoggingMDCUtil.setPaymentId(paymentId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Calling payment gateway: amount={}, currency={}", 
                request.getAmount(), request.getCurrency());
            
            PaymentResponse response = restTemplate.postForObject(
                "/api/payments", request, PaymentResponse.class);
            
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("Payment gateway response: status={}, duration={}ms", 
                response.getStatus(), duration);
            
            return response;
            
        } catch (HttpClientErrorException e) {
            log.error("Payment gateway client error: status={}, body={}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException("Payment failed", e);
            
        } catch (HttpServerErrorException e) {
            log.error("Payment gateway server error: status={}", 
                e.getStatusCode(), e);
            throw new PaymentException("Payment gateway unavailable", e);
            
        } catch (ResourceAccessException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Payment gateway timeout: duration={}ms", duration, e);
            throw new PaymentException("Payment gateway timeout", e);
            
        } finally {
            LoggingMDCUtil.clear();
        }
    }
}
```

### 5. Async Processing Logging

```java
@Service
@Slf4j
public class EmailService {
    
    @Async
    public CompletableFuture<Void> sendOrderConfirmation(Order order) {
        // Copy MDC context to async thread
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        
        return CompletableFuture.runAsync(() -> {
            // Restore MDC context
            if (mdcContext != null) {
                MDC.setContextMap(mdcContext);
            }
            
            try {
                log.info("Sending order confirmation email: orderId={}, email={}", 
                    order.getId(), maskEmail(order.getCustomerEmail()));
                
                emailClient.send(buildEmail(order));
                
                log.info("Order confirmation email sent successfully");
                
            } catch (Exception e) {
                log.error("Failed to send order confirmation email: orderId={}", 
                    order.getId(), e);
            } finally {
                MDC.clear();
            }
        });
    }
    
    private String maskEmail(String email) {
        // user@example.com -> u***@example.com
        int atIndex = email.indexOf('@');
        if (atIndex > 1) {
            return email.charAt(0) + "***" + email.substring(atIndex);
        }
        return "***";
    }
}
```

---

## 📊 Metrics Examples

### 1. Custom Metrics Service

```java
@Service
public class OrderMetricsService {
    
    private final MeterRegistry meterRegistry;
    
    // Counters
    private final Counter ordersCreated;
    private final Counter ordersFailed;
    private final Counter paymentsProcessed;
    
    // Gauges
    private final AtomicInteger pendingOrders = new AtomicInteger(0);
    
    // Timers
    private final Timer orderProcessingTimer;
    private final Timer paymentProcessingTimer;
    
    public OrderMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.ordersCreated = Counter.builder("orders.created.total")
            .description("Total orders created")
            .tag("service", "order-service")
            .register(meterRegistry);
        
        this.ordersFailed = Counter.builder("orders.failed.total")
            .description("Total orders failed")
            .tag("service", "order-service")
            .register(meterRegistry);
        
        this.paymentsProcessed = Counter.builder("payments.processed.total")
            .description("Total payments processed")
            .register(meterRegistry);
        
        // Initialize gauge
        Gauge.builder("orders.pending", pendingOrders, AtomicInteger::get)
            .description("Number of pending orders")
            .register(meterRegistry);
        
        // Initialize timers
        this.orderProcessingTimer = Timer.builder("orders.processing.duration")
            .description("Order processing duration")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.paymentProcessingTimer = Timer.builder("payments.processing.duration")
            .description("Payment processing duration")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
    }
    
    public void recordOrderCreated() {
        ordersCreated.increment();
    }
    
    public void recordOrderFailed(String reason) {
        Counter.builder("orders.failed.total")
            .tag("reason", reason)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordOrderProcessingTime(Runnable operation) {
        orderProcessingTimer.record(operation);
    }
    
    public <T> T recordOrderProcessingTime(Supplier<T> operation) {
        return orderProcessingTimer.record(operation);
    }
    
    public void incrementPendingOrders() {
        pendingOrders.incrementAndGet();
    }
    
    public void decrementPendingOrders() {
        pendingOrders.decrementAndGet();
    }
}
```

### 2. Using Metrics in Service

```java
@Service
@Slf4j
public class OrderService {
    
    private final OrderMetricsService metricsService;
    
    public Order createOrder(CreateOrderRequest request) {
        metricsService.incrementPendingOrders();
        
        try {
            Order order = metricsService.recordOrderProcessingTime(() -> {
                // Process order
                return processOrderInternal(request);
            });
            
            metricsService.recordOrderCreated();
            log.info("Order created: orderId={}", order.getId());
            
            return order;
            
        } catch (InsufficientStockException e) {
            metricsService.recordOrderFailed("insufficient_stock");
            throw e;
            
        } catch (PaymentException e) {
            metricsService.recordOrderFailed("payment_failed");
            throw e;
            
        } catch (Exception e) {
            metricsService.recordOrderFailed("unknown");
            throw e;
            
        } finally {
            metricsService.decrementPendingOrders();
        }
    }
}
```

### 3. HTTP Client Metrics

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate(MeterRegistry meterRegistry) {
        RestTemplate restTemplate = new RestTemplate();
        
        // Add interceptor for metrics
        restTemplate.getInterceptors().add(
            new MetricsClientHttpRequestInterceptor(meterRegistry)
        );
        
        return restTemplate;
    }
}

public class MetricsClientHttpRequestInterceptor 
        implements ClientHttpRequestInterceptor {
    
    private final MeterRegistry meterRegistry;
    
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, 
            byte[] body, 
            ClientHttpRequestExecution execution) throws IOException {
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            ClientHttpResponse response = execution.execute(request, body);
            
            sample.stop(Timer.builder("http.client.requests")
                .tag("method", request.getMethod().name())
                .tag("uri", request.getURI().getPath())
                .tag("status", String.valueOf(response.getStatusCode().value()))
                .tag("outcome", getOutcome(response))
                .register(meterRegistry));
            
            return response;
            
        } catch (IOException e) {
            sample.stop(Timer.builder("http.client.requests")
                .tag("method", request.getMethod().name())
                .tag("uri", request.getURI().getPath())
                .tag("status", "IO_ERROR")
                .tag("outcome", "CLIENT_ERROR")
                .register(meterRegistry));
            
            throw e;
        }
    }
    
    private String getOutcome(ClientHttpResponse response) throws IOException {
        int status = response.getStatusCode().value();
        if (status >= 200 && status < 300) return "SUCCESS";
        if (status >= 400 && status < 500) return "CLIENT_ERROR";
        if (status >= 500) return "SERVER_ERROR";
        return "UNKNOWN";
    }
}
```

### 4. Database Metrics

```java
@Configuration
public class DataSourceMetricsConfig {
    
    @Bean
    public DataSource dataSource(
            DataSourceProperties properties,
            MeterRegistry meterRegistry) {
        
        HikariDataSource dataSource = properties
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
        
        // Register HikariCP metrics
        dataSource.setMetricRegistry(meterRegistry);
        dataSource.setPoolName("order-service-pool");
        
        return dataSource;
    }
}

// Metrics available:
// - hikaricp.connections.active
// - hikaricp.connections.idle
// - hikaricp.connections.pending
// - hikaricp.connections.max
// - hikaricp.connections.min
// - hikaricp.connections.usage
// - hikaricp.connections.timeout
```

### 5. JVM Metrics

```java
@Configuration
public class MetricsConfig {
    
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            // Add common tags to all metrics
            registry.config()
                .commonTags(
                    "application", "order-service",
                    "environment", getEnvironment(),
                    "instance", getInstanceId()
                );
            
            // Register JVM metrics
            new ClassLoaderMetrics().bindTo(registry);
            new JvmMemoryMetrics().bindTo(registry);
            new JvmGcMetrics().bindTo(registry);
            new JvmThreadMetrics().bindTo(registry);
            new ProcessorMetrics().bindTo(registry);
            new FileDescriptorMetrics().bindTo(registry);
            new UptimeMetrics().bindTo(registry);
        };
    }
}
```

---

## 🏥 Health Checks

### 1. Custom Health Indicator

```java
@Component
public class KafkaHealthIndicator implements HealthIndicator {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Override
    public Health health() {
        try {
            // Try to get metadata (lightweight check)
            kafkaTemplate.getProducerFactory()
                .createProducer()
                .partitionsFor("health-check-topic");
            
            return Health.up()
                .withDetail("kafka", "Available")
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("kafka", "Unavailable")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            boolean isValid = conn.isValid(1); // 1 second timeout
            
            if (isValid) {
                return Health.up()
                    .withDetail("database", "Available")
                    .withDetail("validationQuery", "SELECT 1")
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "Connection invalid")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("database", "Unavailable")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

### 2. Readiness and Liveness Probes

```yaml
# application.yml
management:
  endpoint:
    health:
      probes:
        enabled: true
      show-details: always
      group:
        liveness:
          include: livenessState
        readiness:
          include: readinessState,db,kafka,redis
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true
```

```java
@RestController
@RequestMapping("/actuator/health")
public class CustomHealthController {
    
    private final HealthEndpoint healthEndpoint;
    
    @GetMapping("/liveness")
    public ResponseEntity<Health> liveness() {
        // Check if application is running
        Health health = healthEndpoint.healthForPath("liveness");
        return ResponseEntity
            .status(getHttpStatus(health))
            .body(health);
    }
    
    @GetMapping("/readiness")
    public ResponseEntity<Health> readiness() {
        // Check if application is ready to serve traffic
        Health health = healthEndpoint.healthForPath("readiness");
        return ResponseEntity
            .status(getHttpStatus(health))
            .body(health);
    }
    
    private int getHttpStatus(Health health) {
        return health.getStatus() == Status.UP ? 200 : 503;
    }
}
```

---

## ⚙️ Configuration Examples

### 1. Logback Configuration (JSON Format)

```xml
<!-- logback-spring.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    
    <!-- Console appender for development -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- JSON appender for production -->
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>userId</includeMdcKeyName>
            <includeMdcKeyName>requestId</includeMdcKeyName>
            <includeMdcKeyName>orderId</includeMdcKeyName>
            <includeMdcKeyName>conversationId</includeMdcKeyName>
            
            <customFields>{"service":"order-service"}</customFields>
            
            <fieldNames>
                <timestamp>timestamp</timestamp>
                <message>message</message>
                <logger>logger</logger>
                <thread>thread</thread>
                <level>level</level>
                <stackTrace>stack_trace</stackTrace>
            </fieldNames>
        </encoder>
    </appender>
    
    <!-- File appender with rolling -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application-%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxHistory>30</maxHistory>
            <totalSizeCap>10GB</totalSizeCap>
            <timeBasedFileNamingAndTriggeringPolicy 
                class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>
    
    <!-- Async appender for performance -->
    <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>512</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <appender-ref ref="FILE"/>
    </appender>
    
    <!-- Profile-specific configuration -->
    <springProfile name="dev">
        <root level="DEBUG">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
    
    <springProfile name="prod">
        <root level="INFO">
            <appender-ref ref="JSON"/>
            <appender-ref ref="ASYNC"/>
        </root>
    </springProfile>
    
    <!-- Package-specific log levels -->
    <logger name="com.theblood.springfood" level="DEBUG"/>
    <logger name="org.springframework" level="INFO"/>
    <logger name="org.hibernate" level="WARN"/>
    <logger name="org.apache.kafka" level="INFO"/>
    
</configuration>
```

### 2. Application Properties for Monitoring

```yaml
# application.yml
spring:
  application:
    name: order-service

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /management
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
    metrics:
      enabled: true
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
      slo:
        http.server.requests: 100ms, 500ms, 1s, 2s

logging:
  level:
    root: INFO
    com.theblood.springfood: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 3. Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: 'springfood-cluster'
    environment: 'production'

scrape_configs:
  - job_name: 'order-service'
    metrics_path: '/management/prometheus'
    static_configs:
      - targets: ['order-service:8080']
        labels:
          service: 'order-service'
  
  - job_name: 'chat-service'
    metrics_path: '/management/prometheus'
    static_configs:
      - targets: ['chat-service:8081']
        labels:
          service: 'chat-service'
  
  - job_name: 'product-service'
    metrics_path: '/management/prometheus'
    static_configs:
      - targets: ['product-service:8082']
        labels:
          service: 'product-service'

alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']

rule_files:
  - 'alerts/*.yml'
```

### 4. Docker Compose for Monitoring Stack

```yaml
# docker-compose-monitoring.yml
version: '3.8'

services:
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - ./alerts:/etc/prometheus/alerts
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=30d'
  
  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_USERS_ALLOW_SIGN_UP=false
    volumes:
      - grafana-data:/var/lib/grafana
      - ./grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./grafana/datasources:/etc/grafana/provisioning/datasources
  
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
  
  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    ports:
      - "5000:5000"
    volumes:
      - ./logstash/pipeline:/usr/share/logstash/pipeline
    depends_on:
      - elasticsearch
  
  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch

volumes:
  prometheus-data:
  grafana-data:
  elasticsearch-data:
```

---

## 🎯 Quick Start Checklist

### For New Service

```
□ Add Micrometer dependency
□ Configure Spring Boot Actuator
□ Enable Prometheus endpoint
□ Add custom metrics service
□ Implement health indicators
□ Configure logback-spring.xml
□ Add MDC utility class
□ Add correlation ID filter
□ Configure log levels
□ Add service to Prometheus scrape config
□ Create Grafana dashboard
□ Setup alerts
```

### Dependencies (pom.xml)

```xml
<!-- Metrics -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- Logging -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

---

Bạn có thể bắt đầu implement từ những examples này! 🚀
