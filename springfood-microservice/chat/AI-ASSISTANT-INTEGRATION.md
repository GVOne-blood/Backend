# AI Assistant Integration - Gemini với Spring AI

## Tổng quan

Chat service đã được tích hợp AI Assistant sử dụng Google Gemini thông qua Spring AI framework. AI assistant có thể:

- Tư vấn món ăn, nhà hàng
- Hướng dẫn đặt hàng
- Giải đáp thắc mắc về dịch vụ
- Hỗ trợ khách hàng 24/7

## Kiến trúc

```
User → WebSocket/REST → AIAssistantController
                              ↓
                        GeminiAIService
                              ↓
                    Spring AI ChatClient
                              ↓
                        Gemini API
```

### Components

1. **AIAssistantService** - Interface cho AI service
2. **GeminiAIService** - Implementation với Gemini model
3. **AIAssistantController** - REST + WebSocket endpoints
4. **ChatMemory** - In-memory conversation history (có thể chuyển sang Redis)

## Setup

### 1. Dependencies (đã thêm vào pom.xml)

```xml
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-vertex-ai-gemini-spring-boot-starter</artifactId>
</dependency>
```

### 2. Configuration

Thêm Gemini API key vào `.env` file:

```bash
GEMINI_API_KEY=your-actual-api-key-here
```

Hoặc set environment variable:

```bash
export GEMINI_API_KEY=your-api-key
```

### 3. Get Gemini API Key (FREE)

1. Truy cập: https://aistudio.google.com/app/apikey
2. Đăng nhập với Google account
3. Click "Create API Key"
4. Copy API key và thêm vào `.env`

**Lưu ý:** Gemini API có free tier rất hào phóng:
- 15 requests/minute
- 1 million tokens/month
- Đủ cho development và testing

## API Endpoints

### REST API

#### 1. Chat (Synchronous)

```http
POST /api/ai/chat
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "message": "Gợi ý món ăn ngon cho tôi",
  "conversationId": "optional-conversation-id"
}
```

Response:
```json
{
  "conversationId": "uuid",
  "message": "Gợi ý món ăn ngon cho tôi",
  "response": "Tôi gợi ý bạn thử...",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

#### 2. Chat Stream (Server-Sent Events)

```http
GET /api/ai/chat/stream?message=Xin chào&conversationId=uuid
Authorization: Bearer <jwt-token>
```

Response: Stream of text chunks

#### 3. Clear History

```http
DELETE /api/ai/history/{conversationId}
Authorization: Bearer <jwt-token>
```

### WebSocket API

#### Connect

```javascript
const socket = new SockJS('http://localhost:9098/ws');
const stompClient = Stomp.over(socket);

stompClient.connect(
  { Authorization: `Bearer ${token}` },
  (frame) => {
    console.log('Connected:', frame);
    
    // Subscribe to AI responses
    stompClient.subscribe('/user/queue/ai/response', (message) => {
      console.log('AI chunk:', message.body);
    });
    
    stompClient.subscribe('/user/queue/ai/complete', (message) => {
      console.log('AI completed:', message.body);
    });
    
    stompClient.subscribe('/user/queue/ai/error', (message) => {
      console.error('AI error:', message.body);
    });
  }
);
```

#### Send Message

```javascript
stompClient.send('/app/ai/chat', {}, JSON.stringify({
  message: 'Gợi ý món ăn ngon',
  conversationId: 'optional-uuid'
}));
```

## Testing

### 1. Test với cURL (REST)

```bash
# Get JWT token first
TOKEN="your-jwt-token"

# Chat request
curl -X POST http://localhost:9098/api/ai/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Xin chào, bạn có thể giúp gì cho tôi?"
  }'

# Stream request
curl -N http://localhost:9098/api/ai/chat/stream?message=Xin%20chào \
  -H "Authorization: Bearer $TOKEN"
```

### 2. Test với Postman

Import collection từ `chat/test-resources/Chat-Realtime-API.postman_collection.json` và thêm:

```json
{
  "name": "AI Chat",
  "request": {
    "method": "POST",
    "url": "http://localhost:9098/api/ai/chat",
    "header": [
      {
        "key": "Authorization",
        "value": "Bearer {{jwt_token}}"
      }
    ],
    "body": {
      "mode": "raw",
      "raw": "{\n  \"message\": \"Gợi ý món ăn ngon\"\n}"
    }
  }
}
```

### 3. Test với WebSocket Client

Sử dụng file `chat/test-resources/websocket-test-client.html` và thêm:

```javascript
// Subscribe to AI responses
stompClient.subscribe('/user/queue/ai/response', function(message) {
  appendMessage('AI', message.body);
});

// Send AI message
function sendAIMessage() {
  const message = document.getElementById('messageInput').value;
  stompClient.send('/app/ai/chat', {}, JSON.stringify({
    message: message
  }));
}
```

## Features

### 1. Conversation History

AI assistant tự động lưu lịch sử 10 tin nhắn gần nhất để duy trì context:

```java
// Automatically managed by ChatMemory
List<Message> history = chatMemory.get(conversationId, MAX_HISTORY_SIZE);
```

### 2. Streaming Response

Real-time streaming cho trải nghiệm chat mượt mà:

```java
Flux<String> stream = aiAssistantService.chatStream(
  conversationId, 
  userId, 
  message
);
```

### 3. Error Handling

Graceful error handling với fallback messages:

```java
.onErrorReturn("Xin lỗi, tôi đang gặp sự cố kỹ thuật.")
```

## Customization

### 1. System Prompt

Chỉnh sửa trong `AIConfiguration.java`:

```java
.defaultSystem("""
    Bạn là trợ lý AI của SpringFood...
    [Customize your prompt here]
    """);
```

### 2. Model Parameters

Chỉnh sửa trong `application-dev.yml`:

```yaml
spring:
  ai:
    vertex:
      ai:
        gemini:
          chat:
            options:
              temperature: 0.7  # Creativity (0-1)
              max-tokens: 1000  # Response length
              top-p: 0.95       # Nucleus sampling
```

### 3. Chat Memory Storage

Chuyển từ in-memory sang Redis:

```java
@Bean
public ChatMemory chatMemory(RedisTemplate<String, Object> redisTemplate) {
    return new RedisChatMemory(redisTemplate);
}
```

## Production Considerations

### 1. Rate Limiting

Thêm rate limiting để tránh vượt quota:

```java
@RateLimiter(name = "ai-assistant", fallbackMethod = "rateLimitFallback")
public String chat(String conversationId, String userId, String message) {
    // ...
}
```

### 2. Caching

Cache responses cho câu hỏi phổ biến:

```java
@Cacheable(value = "ai-responses", key = "#message")
public String chat(String conversationId, String userId, String message) {
    // ...
}
```

### 3. Monitoring

Thêm metrics cho AI service:

```java
@Timed(value = "ai.chat.duration", description = "AI chat duration")
@Counted(value = "ai.chat.requests", description = "AI chat requests")
public String chat(...) {
    // ...
}
```

### 4. Cost Management

- Monitor usage qua Google Cloud Console
- Set budget alerts
- Implement request throttling
- Cache common responses

## Troubleshooting

### 1. API Key không hoạt động

```bash
# Check environment variable
echo $GEMINI_API_KEY

# Verify API key tại: https://aistudio.google.com/app/apikey
```

### 2. Rate limit exceeded

```
Error: 429 Too Many Requests
Solution: Implement exponential backoff hoặc giảm request rate
```

### 3. Response quá chậm

```
- Giảm max-tokens
- Tăng temperature (faster but less accurate)
- Sử dụng gemini-1.5-flash thay vì gemini-pro
```

## Next Steps

1. ✅ Basic AI chat integration
2. ⏳ Integrate với product/order data
3. ⏳ Add function calling (order placement, product search)
4. ⏳ Multi-language support
5. ⏳ Voice input/output
6. ⏳ Image understanding (menu photos)

## Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Gemini API Documentation](https://ai.google.dev/docs)
- [Spring AI Gemini](https://docs.spring.io/spring-ai/reference/api/chat/vertexai-gemini-chat.html)
