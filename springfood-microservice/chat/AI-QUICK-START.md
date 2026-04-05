# AI Assistant Quick Start

## 🎯 TL;DR - 100% MIỄN PHÍ

Tất cả features đều FREE với Gemini API key. Không cần thẻ tín dụng, không có chi phí ẩn.

## Setup trong 3 bước

### 1. Get Gemini API Key (FREE - 2 phút)

```bash
# Truy cập: https://aistudio.google.com/app/apikey
# Đăng nhập → Create API Key → Copy key
```

**Free Tier:**
- 15 requests/minute
- 1,000,000 tokens/month (~30,000 conversations)
- Không hết hạn, không cần thẻ tín dụng

### 2. Add API Key vào .env

Tạo/edit file `.env` trong thư mục `chat/`:

```bash
GEMINI_API_KEY=your-actual-api-key-here
```

### 3. Run Chat Service

```bash
cd chat
mvn spring-boot:run
```

## ✅ Đã thay đổi gì?

### 1. Endpoint mới: `/api/ai-assistant`
```bash
# OLD: /api/ai/chat
# NEW: /api/ai-assistant/chat

# WebSocket
# OLD: /app/ai/chat
# NEW: /app/ai-assistant/chat
```

### 2. Authentication
- **REST endpoints**: Không cần auth (đã qua Gateway, dùng UserContextHolder)
- **WebSocket**: Vẫn cần JWT token (đặc tính của WebSocket protocol)

### 3. Fallback Mechanism
```
Primary: gemini-1.5-flash (15 req/min)
    ↓ (nếu rate limit)
Fallback: gemini-1.5-pro (2 req/min)
    ↓ (nếu vẫn fail)
Friendly error message
```

## Test AI Assistant

### Option 1: WebSocket Test Client (Recommended)

1. Mở file: `chat/test-resources/ai-chat-test.html` trong browser
2. Nhập JWT token (get từ login API)
3. Chat với AI! 🤖

### Option 2: cURL (REST - Không cần token)

```bash
# Chat với AI (qua Gateway)
curl -X POST http://localhost:8080/api/chat/ai-assistant/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Gợi ý món ăn ngon cho tôi"
  }'

# Stream response
curl -N http://localhost:8080/api/chat/ai-assistant/chat/stream?message=Xin%20chào
```

### Option 3: Direct to Service (Cần token)

```bash
TOKEN="your-jwt-token"

curl -X POST http://localhost:9098/api/ai-assistant/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Xin chào AI!"
  }'
```

## API Endpoints

### REST API (qua Gateway - Không cần auth)
- **Chat**: `POST /api/chat/ai-assistant/chat`
- **Stream**: `GET /api/chat/ai-assistant/chat/stream?message=hello`
- **Clear History**: `DELETE /api/chat/ai-assistant/history/{conversationId}`

### WebSocket (Cần JWT token)
- **Connect**: `ws://localhost:9098/ws`
- **Send**: `/app/ai-assistant/chat`
- **Receive**: `/user/queue/ai-assistant/response`
- **Complete**: `/user/queue/ai-assistant/complete`
- **Error**: `/user/queue/ai-assistant/error`

## Features - Tất cả MIỄN PHÍ

✅ Real-time streaming responses  
✅ Conversation history (10 messages)  
✅ Automatic fallback (flash → pro)  
✅ Rate limit handling  
✅ Error recovery  
✅ WebSocket + REST support  
✅ Vietnamese language  
✅ Context-aware responses  
✅ UserContextHolder integration  

## Troubleshooting

### API Key không hoạt động?
```bash
# Check .env file
cat chat/.env

# Verify API key tại: https://aistudio.google.com/app/apikey
# Make sure key is active and not revoked
```

### Connection refused?
```bash
# Make sure chat service is running on port 9098
curl http://localhost:9098/management/health

# Check if Gemini API is accessible
curl https://generativelanguage.googleapis.com/v1beta/models?key=YOUR_API_KEY
```

### Rate limit exceeded?
```
Error: 429 Too Many Requests
Solution: Fallback mechanism tự động kick in
- Primary: gemini-1.5-flash (15 req/min)
- Fallback: gemini-1.5-pro (2 req/min)
```

### UserContext is null?
```bash
# Make sure request goes through API Gateway
# Gateway extracts user info and sets UserContextHolder

# Direct to service (port 9098) - Need JWT token
# Via Gateway (port 8080) - No token needed
```

## 💰 Pricing - Chi tiết

Xem file `chat/AI-PRICING-FAQ.md` để biết:
- Chi tiết về free tier
- Ước tính usage cho production
- Cost optimization tips
- FAQ về pricing

**Tóm tắt:** HOÀN TOÀN MIỄN PHÍ với API key của bạn!

## Next Steps

📖 **Full documentation**: `chat/AI-ASSISTANT-INTEGRATION.md`

💰 **Pricing & FAQ**: `chat/AI-PRICING-FAQ.md`

🎯 **Customize system prompt**: 
```java
// chat/src/main/java/com/theblood/springfood/chat/config/AIConfiguration.java
.defaultSystem("Your custom prompt here...")
```

🚀 **Production setup**:
- Rate limiting per user
- Response caching
- Monitoring & alerts
- Multiple API keys rotation

## Example Conversations

```
User: "Gợi ý món ăn ngon"
AI: "Tôi gợi ý bạn thử:
     🍜 Phở bò - Món ăn truyền thống
     🍲 Bún chả - Đặc sản Hà Nội
     🥘 Cơm tấm - Món ăn phổ biến
     
     Bạn thích món nào? 😊"

User: "Làm sao đặt hàng?"
AI: "Để đặt hàng trên SpringFood:
     1. Chọn món ăn yêu thích
     2. Thêm vào giỏ hàng
     3. Chọn địa chỉ giao hàng
     4. Thanh toán
     
     Cần hỗ trợ thêm không? 🛵"
```

## Architecture

```
User → API Gateway (extract user) → Chat Service
                                         ↓
                                   UserContextHolder
                                         ↓
                                   AIAssistantService
                                         ↓
                                   Spring AI ChatClient
                                         ↓
                                   Gemini API (FREE)
```

## Support

Có vấn đề? Check:
1. `AI-ASSISTANT-INTEGRATION.md` - Full docs
2. `AI-PRICING-FAQ.md` - Pricing & FAQ
3. `test-resources/ai-chat-test.html` - Test client
4. Google AI Studio - Usage dashboard
