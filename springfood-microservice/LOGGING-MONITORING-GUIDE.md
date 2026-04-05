# Hướng Dẫn Logging và Monitoring cho Microservices

## 📋 Mục Lục
1. [Tổng Quan](#tổng-quan)
2. [Logging Strategy](#logging-strategy)
3. [Monitoring Strategy](#monitoring-strategy)
4. [Implementation Hiện Tại](#implementation-hiện-tại)
5. [Best Practices](#best-practices)
6. [Roadmap Triển Khai](#roadmap-triển-khai)

---

## 🎯 Tổng Quan

### Tại Sao Cần Logging và Monitoring?

**Logging** giúp bạn:
- Debug và troubleshoot issues
- Audit và compliance (theo dõi ai làm gì, khi nào)
- Phân tích hành vi người dùng
- Tìm hiểu root cause của bugs

**Monitoring** giúp bạn:
- Phát hiện sớm vấn đề (proactive)
- Đo lường performance và health của hệ thống
- Capacity planning (dự đoán khi nào cần scale)
- SLA/SLO tracking

### Các Thành Phần Chính

```
┌─────────────────────────────────────────────────────────┐
│                   OBSERVABILITY STACK                    │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │ LOGGING  │  │ METRICS  │  │ TRACING  │             │
│  └──────────┘  └──────────┘  └──────────┘             │
│       │             │              │                    │
│       ▼             ▼              ▼                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │   ELK    │  │Prometheus│  │  Jaeger  │             │
│  │  Stack   │  │ +Grafana │  │  /Zipkin │             │
│  └──────────┘  └──────────┘  └──────────┘             │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## 📝 Logging Strategy

### 1. Log Levels (Khi Nào Dùng Gì)

```java
// TRACE - Chi tiết nhất, chỉ dùng khi debug sâu
log.trace("Entering method calculateTotal with params: {}", params);

// DEBUG - Thông tin debug, development
log.debug("Processing order: orderId={}, items={}", orderId, items.size());

// INFO - Thông tin quan trọng về flow của application
log.info("Order created successfully: orderId={}, userId={}", orderId, userId);

// WARN - Cảnh báo, có vấn đề nhưng không critical
log.warn("Retry attempt {} failed for orderId={}", attempt, orderId);

// ERROR - Lỗi nghiêm trọng, cần xử lý
log.error("Failed to process payment for orderId={}", orderId, exception);
```

### 2. Structured Logging với MDC (Mapped Diagnostic Context)

**Tại sao cần MDC?**
- Thêm context vào mọi log message
- Dễ dàng filter và search logs
- Track request flow qua nhiều services

**Implementation:**

```java
// Set MDC context
MDC.put("userId", userId);
MDC.put("requestId", requestId);
MDC.put("conversationId", conversationId);

try {
    // Tất cả logs trong block này sẽ có context
    log.info("Processing message"); 
    // Output: [userId=123] [requestId=abc] Processing message
} finally {
    // QUAN TRỌNG: Luôn clear MDC để tránh memory leak
    MDC.clear();
}
```

### 3. Log Format Standards

**JSON Format (Production):**
```json
{
  "timestamp": "2024-02-24T10:30:45.123Z",
  "level": "INFO",
  "service": "chat-service",
  "traceId": "abc123",
  "spanId": "def456",
  "userId": "user123",
  "conversationId": "conv789",
  "message": "Message sent successfully",
  "duration_ms": 45
}
```

**Human-Readable Format (Development):**
```
2024-02-24 10:30:45.123 INFO  [chat-service] [userId=user123] Message sent successfully
```

### 4. What to Log (Nên Log Gì)

✅ **NÊN LOG:**
- Request/Response (với sensitive data đã mask)
- Business events (order created, payment processed)
- Errors và exceptions với stack trace
- Performance metrics (execution time)
- Security events (login, logout, failed auth)
- External API calls (với latency)

❌ **KHÔNG NÊN LOG:**
- Passwords, tokens, API keys
- Credit card numbers, PII (Personal Identifiable Information)
- Quá nhiều logs trong loops (gây performance issue)
- Sensitive business data

### 5. Log Aggregation với ELK Stack

```
┌─────────────┐
│ Application │
│   (Logback) │
└──────┬──────┘
       │ JSON logs
       ▼
┌─────────────┐
│  Filebeat   │ ← Collect logs
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Logstash   │ ← Parse & Transform
└──────┬──────┘
       │
       ▼
┌─────────────┐
│Elasticsearch│ ← Store & Index
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Kibana    │ ← Visualize & Search
└─────────────┘
```

---

## 📊 Monitoring Strategy

### 1. The Four Golden Signals

```
┌────────────────────────────────────────┐
│         FOUR GOLDEN SIGNALS            │
├────────────────────────────────────────┤
│                                        │
│  1. LATENCY                            │
│     ↳ Thời gian xử lý request          │
│                                        │
│  2. TRAFFIC                            │
│     ↳ Số lượng requests/second         │
│                                        │
│  3. ERRORS                             │
│     ↳ Tỷ lệ request thất bại           │
│                                        │
│  4. SATURATION                         │
│     ↳ CPU, Memory, Disk usage          │
│                                        │
└────────────────────────────────────────┘
```

### 2. Metrics Types

**Counter** - Chỉ tăng, không giảm
```java
// Ví dụ: Tổng số messages đã gửi
Counter messagesSent = Counter.builder("chat.messages.sent")
    .description("Total messages sent")
    .register(meterRegistry);

messagesSent.increment(); // +1
```

**Gauge** - Giá trị có thể tăng/giảm
```java
// Ví dụ: Số WebSocket connections hiện tại
Gauge.builder("chat.websocket.connections", userRegistry, 
    registry -> registry.getUserCount())
    .register(meterRegistry);
```

**Timer** - Đo thời gian và tần suất
```java
// Ví dụ: Thời gian xử lý message
Timer timer = Timer.builder("chat.message.processing")
    .description("Message processing time")
    .register(meterRegistry);

timer.record(() -> {
    // Code cần đo
    processMessage(message);
});
```

**Distribution Summary** - Phân phối giá trị
```java
// Ví dụ: Kích thước message
DistributionSummary.builder("chat.message.size")
    .baseUnit("bytes")
    .register(meterRegistry);
```

### 3. Metrics Naming Convention

```
<namespace>.<subsystem>.<metric>.<unit>

Ví dụ:
- chat.messages.sent.total
- chat.websocket.connections.active
- chat.kafka.errors.total
- chat.message.latency.seconds
- chat.redis.operations.duration.ms
```

### 4. Health Checks

**Liveness Probe** - Service có đang chạy không?
```yaml
# application.yml
management:
  endpoint:
    health:
      probes:
        enabled: true
```

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Check critical dependencies
        if (isDatabaseUp() && isKafkaUp()) {
            return Health.up().build();
        }
        return Health.down().build();
    }
}
```

**Readiness Probe** - Service có sẵn sàng nhận traffic không?

### 5. Alerting Strategy

**Alert Levels:**
```
┌─────────────────────────────────────────┐
│  CRITICAL (P1)                          │
│  ↳ Service down, data loss              │
│  ↳ Action: Wake up on-call engineer    │
├─────────────────────────────────────────┤
│  HIGH (P2)                              │
│  ↳ High error rate, slow response       │
│  ↳ Action: Investigate within 1 hour   │
├─────────────────────────────────────────┤
│  MEDIUM (P3)                            │
│  ↳ Degraded performance                 │
│  ↳ Action: Investigate next day        │
├─────────────────────────────────────────┤
│  LOW (P4)                               │
│  ↳ Minor issues, warnings               │
│  ↳ Action: Review in weekly meeting    │
└─────────────────────────────────────────┘
```

**Alert Examples:**
```yaml
# Prometheus Alert Rules
groups:
  - name: chat-service
    rules:
      # High error rate
      - alert: HighErrorRate
        expr: rate(chat_errors_total[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate in chat service"
          
      # High latency
      - alert: HighLatency
        expr: histogram_quantile(0.95, chat_message_latency_seconds) > 1
        for: 5m
        labels:
          severity: warning
          
      # Service down
      - alert: ServiceDown
        expr: up{job="chat-service"} == 0
        for: 1m
        labels:
          severity: critical
```

---

## 🔍 Implementation Hiện Tại

### Đã Có (✅)

1. **Logging Infrastructure**
   - ✅ Logback configuration (`logback-spring.xml`)
   - ✅ MDC utility class (`LoggingMDCUtil.java`)
   - ✅ Structured logging với correlation IDs

2. **Metrics với Micrometer**
   - ✅ `ChatMetricsService` - Custom metrics cho chat service
   - ✅ `SecurityMetersService` - Security metrics
   - ✅ Spring Boot Actuator endpoints
   - ✅ Prometheus endpoint (`/management/prometheus`)

3. **Metrics Đang Track**
   ```
   Chat Service:
   - chat.messages.sent
   - chat.messages.delivered
   - chat.messages.persisted
   - chat.auth.failures
   - chat.kafka.errors
   - chat.redis.errors
   - chat.websocket.connections
   - chat.message.latency
   - chat.persistence.latency
   
   Security:
   - security.authentication.invalid-tokens
   ```

### Chưa Có (❌)

1. **Log Aggregation**
   - ❌ ELK Stack chưa setup
   - ❌ Centralized logging
   - ❌ Log retention policy

2. **Monitoring Dashboard**
   - ❌ Grafana dashboards
   - ❌ Prometheus server
   - ❌ Alert manager

3. **Distributed Tracing**
   - ❌ Jaeger/Zipkin
   - ❌ Trace correlation across services

4. **Advanced Features**
   - ❌ Log sampling (để giảm volume)
   - ❌ Dynamic log level adjustment
   - ❌ Business metrics dashboard

---

## 💡 Best Practices

### 1. Logging Best Practices

```java
// ❌ BAD
log.info("User " + userId + " sent message " + messageId);

// ✅ GOOD - Sử dụng parameterized logging
log.info("User {} sent message {}", userId, messageId);

// ❌ BAD - Log trong loop
for (Message msg : messages) {
    log.debug("Processing message: {}", msg);
}

// ✅ GOOD - Log summary
log.debug("Processing {} messages", messages.size());

// ❌ BAD - Không có context
log.error("Failed to save");

// ✅ GOOD - Có context và exception
log.error("Failed to save message: messageId={}, conversationId={}", 
    messageId, conversationId, exception);
```

### 2. Metrics Best Practices

```java
// ✅ GOOD - Sử dụng tags để phân loại
Counter.builder("http.requests")
    .tag("method", "POST")
    .tag("endpoint", "/api/messages")
    .tag("status", "200")
    .register(registry);

// ✅ GOOD - Record timing
Timer.Sample sample = Timer.start(registry);
try {
    processMessage();
} finally {
    sample.stop(Timer.builder("message.processing")
        .tag("type", "chat")
        .register(registry));
}

// ✅ GOOD - Use meaningful names
// chat.messages.sent.total (clear)
// vs
// msg_cnt (unclear)
```

### 3. Performance Considerations

```java
// ❌ BAD - Expensive operation trong log
log.debug("User data: {}", user.fetchAllRelatedData());

// ✅ GOOD - Chỉ execute khi cần
if (log.isDebugEnabled()) {
    log.debug("User data: {}", user.fetchAllRelatedData());
}

// ✅ BETTER - Lazy evaluation
log.debug("User data: {}", () -> user.fetchAllRelatedData());
```

### 4. Security Best Practices

```java
// ❌ BAD - Log sensitive data
log.info("User login: username={}, password={}", username, password);

// ✅ GOOD - Mask sensitive data
log.info("User login: username={}, password=***", username);

// ✅ GOOD - Sử dụng utility để mask
log.info("Processing payment: {}", maskSensitiveData(paymentInfo));
```

---

## 🚀 Roadmap Triển Khai

### Phase 1: Foundation (1-2 tuần)

**Mục tiêu:** Setup cơ bản logging và monitoring

1. **Chuẩn hóa Logging**
   ```
   □ Tạo logging guidelines document
   □ Implement log masking utility
   □ Standardize log format across services
   □ Add correlation ID propagation
   ```

2. **Setup Prometheus**
   ```
   □ Deploy Prometheus server
   □ Configure scraping cho tất cả services
   □ Setup service discovery
   □ Configure retention policy
   ```

3. **Basic Dashboards**
   ```
   □ Setup Grafana
   □ Tạo dashboard cho mỗi service
   □ Monitor Four Golden Signals
   ```

### Phase 2: Centralization (2-3 tuần)

**Mục tiêu:** Centralized logging và advanced monitoring

1. **ELK Stack**
   ```
   □ Deploy Elasticsearch cluster
   □ Setup Logstash pipelines
   □ Configure Filebeat on all services
   □ Create Kibana dashboards
   □ Setup index lifecycle management
   ```

2. **Advanced Metrics**
   ```
   □ Add business metrics
   □ JVM metrics (heap, GC, threads)
   □ Database connection pool metrics
   □ Kafka consumer lag metrics
   ```

3. **Alerting**
   ```
   □ Setup Alertmanager
   □ Define alert rules
   □ Configure notification channels (Slack, Email)
   □ Create runbooks for common alerts
   ```

### Phase 3: Optimization (2-3 tuần)

**Mục tiêu:** Distributed tracing và optimization

1. **Distributed Tracing**
   ```
   □ Deploy Jaeger/Zipkin
   □ Add tracing instrumentation
   □ Correlate traces with logs
   □ Create trace-based dashboards
   ```

2. **Log Optimization**
   ```
   □ Implement log sampling
   □ Dynamic log level adjustment
   □ Log compression
   □ Cost optimization
   ```

3. **Advanced Features**
   ```
   □ Anomaly detection
   □ Predictive alerting
   □ SLO/SLA tracking
   □ Capacity planning dashboards
   ```

### Phase 4: Production Ready (1-2 tuần)

**Mục tiêu:** Production hardening

1. **Documentation**
   ```
   □ Runbooks cho common issues
   □ Dashboard documentation
   □ Alert response procedures
   □ Troubleshooting guides
   ```

2. **Testing**
   ```
   □ Chaos engineering tests
   □ Load testing với monitoring
   □ Failover testing
   □ Alert testing
   ```

3. **Training**
   ```
   □ Team training sessions
   □ On-call rotation setup
   □ Incident response drills
   ```

---

## 📚 Resources và Tools

### Recommended Stack

```yaml
Logging:
  - Logback (Java logging framework)
  - ELK Stack (Elasticsearch, Logstash, Kibana)
  - Filebeat (Log shipper)

Metrics:
  - Micrometer (Metrics facade)
  - Prometheus (Metrics storage)
  - Grafana (Visualization)

Tracing:
  - Spring Cloud Sleuth (Auto-instrumentation)
  - Jaeger hoặc Zipkin (Trace storage)

Alerting:
  - Prometheus Alertmanager
  - PagerDuty / Opsgenie (On-call management)
```

### Useful Links

- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Best Practices](https://prometheus.io/docs/practices/)
- [ELK Stack Guide](https://www.elastic.co/guide/)
- [Grafana Dashboards](https://grafana.com/grafana/dashboards/)
- [The Twelve-Factor App - Logs](https://12factor.net/logs)

---

## 🎓 Key Takeaways

1. **Logging là để hiểu "what happened"**
   - Debug issues
   - Audit trail
   - Business analytics

2. **Monitoring là để biết "what's happening now"**
   - Real-time health
   - Performance metrics
   - Proactive alerting

3. **Tracing là để hiểu "how it happened"**
   - Request flow
   - Performance bottlenecks
   - Service dependencies

4. **Ba pillars of observability:**
   ```
   Logs + Metrics + Traces = Complete Observability
   ```

5. **Start simple, iterate:**
   - Bắt đầu với basic logging và metrics
   - Thêm dần advanced features
   - Optimize dựa trên actual needs

---

## 📞 Next Steps

Để bắt đầu implement, bạn có thể:

1. Review implementation hiện tại
2. Chọn phase để bắt đầu (recommend: Phase 1)
3. Setup infrastructure (Prometheus, Grafana)
4. Standardize logging across services
5. Create first dashboards

Bạn muốn tôi giúp implement phần nào trước?
