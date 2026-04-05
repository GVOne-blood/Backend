# 🧪 Chat Realtime Core - Testing Guide

Hướng dẫn đầy đủ để test Chat Realtime Core system.

## 📋 Mục Lục

1. [Chuẩn Bị](#chuẩn-bị)
2. [Test với Postman](#test-với-postman)
3. [Test với WebSocket Client](#test-với-websocket-client)
4. [Test Scenarios](#test-scenarios)
5. [Troubleshooting](#troubleshooting)

---

## 🚀 Chuẩn Bị

### 1. Start Infrastructure Services

```bash
cd F:\Document\TASC\Backend\springfood-microservice
docker-manage.bat start
```

Đợi cho đến khi các services sau đã sẵn sàng:
- ✅ PostgreSQL (localhost:5432)
- ✅ Kafka (localhost:9092)
- ✅ Redis (localhost:6379)
- ✅ Zookeeper (localhost:2181)

### 2. Build và Run Chat Service

```bash
cd chat
mvnw clean package -DskipTests
mvnw spring-boot:run
```

Chat service sẽ chạy tại: **http://localhost:8084**

### 3. Lấy JWT Token

Bạn cần JWT token từ Authentication Service. Token phải có:
- `sub`: userId (ví dụ: "user-1")
- `preferred_username`: username
- Valid signature và chưa expired

**Cách lấy token:**
```bash
# Login qua Authentication Service
POST http://localhost:8081/api/auth/login
Body: {
  "username": "user1",
  "password": "password123"
}

# Response sẽ chứa JWT token
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 📮 Test với Postman

### Import Collection

1. Mở Postman
2. Click **Import**
3. Chọn file: `chat/test-resources/Chat-Realtime-API.postman_collection.json`
4. Collection sẽ được import với tất cả requests

### Cấu Hình Variables

Sau khi import, cấu hình các variables:

1. Click vào collection "Chat Realtime Core API"
2. Chọn tab **Variables**
3. Cập nhật các giá trị:
   - `baseUrl`: `http://localhost:8084`
   - `jwtToken`: Paste JWT token của bạn
   - `conversationId`: Sẽ tự động set sau khi tạo conversation
   - `messageId`: Sẽ tự động set sau khi lấy messages

### Test Flow

**Bước 1: Tạo Conversation**
```
POST /api/conversations
```
- Chọn request "Create DIRECT Conversation"
- Click **Send**
- `conversationId` sẽ tự động được lưu vào variable

**Bước 2: Lấy Danh Sách Conversations**
```
GET /api/conversations
```
- Verify response có array conversations
- Check pagination info

**Bước 3: Lấy Chi Tiết Conversation**
```
GET /api/conversations/{{conversationId}}
```
- Verify conversation details
- Check participants list

**Bước 4: Lấy Message History**
```
GET /api/conversations/{{conversationId}}/messages
```
- Verify messages array
- Check pagination

**Bước 5: Check Health & Metrics**
```
GET /actuator/health
GET /actuator/metrics/chat.messages.sent
```

---

## 🔌 Test với WebSocket Client

### Mở Test Client

1. Mở file: `chat/test-resources/websocket-test-client.html` trong browser
2. Hoặc serve qua HTTP server:
   ```bash
   cd chat/test-resources
   python -m http.server 8000
   ```
   Sau đó mở: http://localhost:8000/websocket-test-client.html

### Cấu Hình Connection

1. **WebSocket URL**: `http://localhost:8084/ws`
2. **JWT Token**: Paste token của bạn
3. **Conversation ID**: ID của conversation (từ Postman)
4. **User ID**: userId của bạn (ví dụ: "user-1")

### Kết Nối

1. Click **🔌 Connect**
2. Đợi status chuyển sang **🟢 Connected**
3. Bạn sẽ thấy message "✅ Connected successfully!"

### Gửi Tin Nhắn

**Cách 1: Gửi tin nhắn thủ công**
1. Nhập tin nhắn vào input box
2. Click **📤 Send** hoặc nhấn Enter

**Cách 2: Gửi tin nhắn test**
1. Click **🧪 Test Message**
2. Tin nhắn test sẽ được gửi tự động

**Cách 3: Gửi tin nhắn reply**
1. Đợi nhận được tin nhắn
2. Click **💬 Reply Test**
3. Tin nhắn reply sẽ được gửi

### Test Typing Indicators

1. Click **⌨️ Start Typing**
2. Typing indicator sẽ được gửi mỗi 3 giây
3. Mở một tab khác với user khác để thấy typing indicator
4. Click **🛑 Stop Typing** để dừng

### Test Read Receipts

1. Đợi nhận được tin nhắn
2. Click **✅ Mark as Read**
3. Unread count sẽ được reset về 0

---

## 🎯 Test Scenarios

### Scenario 1: Two Users Chat

**Setup:**
- Mở 2 browser tabs
- Tab 1: User 1 (user-1)
- Tab 2: User 2 (user-2)
- Cùng conversationId

**Test Steps:**
1. Tab 1: Connect với JWT của user-1
2. Tab 2: Connect với JWT của user-2
3. Tab 1: Gửi tin nhắn "Hello from User 1"
4. ✅ Verify: Tab 2 nhận được tin nhắn
5. Tab 2: Gửi tin nhắn "Hi from User 2"
6. ✅ Verify: Tab 1 nhận được tin nhắn

### Scenario 2: Typing Indicators

**Test Steps:**
1. Tab 1: Click "Start Typing"
2. ✅ Verify: Tab 2 thấy "user-1 is typing..."
3. Đợi 5 giây
4. ✅ Verify: Typing indicator tự động biến mất (Redis TTL)
5. Tab 1: Click "Stop Typing"
6. ✅ Verify: Typing indicator biến mất ngay lập tức

### Scenario 3: Read Receipts

**Test Steps:**
1. Tab 1: Gửi tin nhắn
2. Tab 2: Click "Mark as Read"
3. ✅ Verify: Tab 1 nhận được read receipt event
4. Postman: GET /api/conversations/{conversationId}/unread-count
5. ✅ Verify: unreadCount = 0

### Scenario 4: Reply Messages

**Test Steps:**
1. Tab 1: Gửi tin nhắn "Original message"
2. Tab 2: Nhận được tin nhắn
3. Tab 2: Click "Reply Test"
4. ✅ Verify: Tab 1 nhận được reply với replyToMessageId
5. ✅ Verify: Reply message có preview của original message

### Scenario 5: Message Persistence

**Test Steps:**
1. Gửi 5 tin nhắn qua WebSocket
2. Đợi 2-3 giây (cho Kafka consumer xử lý)
3. Postman: GET /api/conversations/{conversationId}/messages
4. ✅ Verify: Tất cả 5 tin nhắn đã được lưu vào database
5. ✅ Verify: Messages được sắp xếp theo created_at DESC

### Scenario 6: Authorization

**Test Steps:**
1. Tạo conversation với user-1 và user-2
2. Connect với JWT của user-3 (không phải participant)
3. Gửi tin nhắn
4. ✅ Verify: Nhận được authorization error
5. ✅ Verify: Tin nhắn không được gửi

### Scenario 7: Invalid JWT

**Test Steps:**
1. Nhập JWT token không hợp lệ
2. Click Connect
3. ✅ Verify: Connection failed
4. ✅ Verify: Status hiển thị "Connection Failed"

### Scenario 8: Redis Unavailable (Graceful Degradation)

**Test Steps:**
1. Stop Redis: `docker stop redis`
2. Gửi tin nhắn qua WebSocket
3. ✅ Verify: Tin nhắn vẫn được gửi thành công
4. ✅ Verify: Typing indicators không hoạt động (graceful degradation)
5. ✅ Verify: Không có exception thrown
6. Start Redis lại: `docker start redis`

### Scenario 9: Kafka Message Ordering

**Test Steps:**
1. Gửi 10 tin nhắn liên tiếp rất nhanh
2. Đợi tất cả tin nhắn được nhận
3. ✅ Verify: Tin nhắn được nhận theo đúng thứ tự gửi
4. ✅ Verify: created_at timestamps tăng dần

### Scenario 10: Multiple Conversations

**Test Steps:**
1. Tạo 3 conversations khác nhau
2. Gửi tin nhắn vào mỗi conversation
3. ✅ Verify: Tin nhắn chỉ được gửi đến participants của conversation đó
4. ✅ Verify: Không có cross-conversation message leakage

---

## 📊 Monitoring & Metrics

### Check Health

```bash
curl http://localhost:8084/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "kafka": {"status": "UP"},
    "redis": {"status": "UP"}
  }
}
```

### Check Metrics

**Messages Sent:**
```bash
curl http://localhost:8084/actuator/metrics/chat.messages.sent
```

**Messages Delivered:**
```bash
curl http://localhost:8084/actuator/metrics/chat.messages.delivered
```

**WebSocket Connections:**
```bash
curl http://localhost:8084/actuator/metrics/chat.websocket.connections
```

**Message Latency:**
```bash
curl http://localhost:8084/actuator/metrics/chat.message.latency
```

---

## 🐛 Troubleshooting

### Issue 1: Cannot Connect WebSocket

**Symptoms:**
- Status shows "Connection Failed"
- Console error: "Failed to connect"

**Solutions:**
1. Check Chat service is running: `curl http://localhost:8084/actuator/health`
2. Verify JWT token is valid and not expired
3. Check browser console for detailed error
4. Verify WebSocket URL is correct: `http://localhost:8084/ws`

### Issue 2: Messages Not Received

**Symptoms:**
- Message sent but not received by other users
- No error shown

**Solutions:**
1. Check Kafka is running: `docker ps | grep kafka`
2. Verify both users are in the same conversation
3. Check Kafka consumer logs in Chat service console
4. Verify SimpUserRegistry has both users registered

### Issue 3: Typing Indicators Not Working

**Symptoms:**
- Typing indicator not showing
- No error

**Solutions:**
1. Check Redis is running: `docker ps | grep redis`
2. Verify conversation ID is correct
3. Check Redis keys: `docker exec -it redis redis-cli KEYS "typing:*"`
4. Verify TTL is set: `docker exec -it redis redis-cli TTL "typing:conv-123"`

### Issue 4: Read Receipts Not Updating

**Symptoms:**
- Unread count not reset
- Read receipt not broadcast

**Solutions:**
1. Check message ID is valid
2. Verify user is participant of conversation
3. Check Kafka topic "chat-read-receipts" has messages
4. Verify ReadReceiptConsumer is running

### Issue 5: Database Connection Error

**Symptoms:**
- Error: "Unable to acquire JDBC Connection"
- Messages not persisted

**Solutions:**
1. Check PostgreSQL is running: `docker ps | grep postgres`
2. Verify database credentials in application.yml
3. Check database exists: `docker exec -it postgres psql -U postgres -l`
4. Verify chat schema exists: `docker exec -it postgres psql -U postgres -d springfood -c "\dn"`

### Issue 6: JWT Token Expired

**Symptoms:**
- Connection fails with "Token expired"
- 401 Unauthorized

**Solutions:**
1. Get new JWT token from Authentication Service
2. Update token in Postman variables
3. Update token in WebSocket client
4. Reconnect

---

## 📝 Expected Results Summary

### ✅ REST APIs
- [x] Create conversation → 201 Created
- [x] Get conversations → 200 OK with array
- [x] Get conversation details → 200 OK
- [x] Get message history → 200 OK with pagination
- [x] Add participant → 201 Created
- [x] Get unread count → 200 OK
- [x] Search conversations → 200 OK

### ✅ WebSocket
- [x] Connect with valid JWT → Connected
- [x] Connect with invalid JWT → Connection failed
- [x] Send message → Delivered to all participants
- [x] Receive message → Real-time delivery
- [x] Typing indicator → Broadcast to participants
- [x] Read receipt → Unread count reset

### ✅ Kafka
- [x] Message published to "chat-messages" topic
- [x] Message consumed by broadcast consumer
- [x] Message consumed by persistence consumer
- [x] Read receipt published to "chat-read-receipts" topic
- [x] Message ordering maintained

### ✅ Redis
- [x] Typing indicator stored with TTL 5s
- [x] Typing indicator auto-expires
- [x] Typing indicator cleared on message send
- [x] Graceful degradation when Redis unavailable

### ✅ Database
- [x] Messages persisted to message table
- [x] Conversation metadata updated
- [x] Unread count incremented
- [x] Read receipts stored
- [x] Batch insert working (100 messages)

---

## 🎓 Tips & Best Practices

1. **Always check logs** - Chat service logs show detailed information about message flow
2. **Use browser DevTools** - Network tab shows WebSocket frames
3. **Monitor Kafka** - Use Kafka UI or CLI to check topics and messages
4. **Check Redis** - Use redis-cli to inspect keys and TTLs
5. **Test with multiple users** - Open multiple browser tabs/windows
6. **Test edge cases** - Invalid data, missing fields, unauthorized access
7. **Performance testing** - Send many messages quickly to test throughput
8. **Clean up** - Clear test data between test runs

---

## 📞 Support

Nếu gặp vấn đề, check:
1. Chat service logs: `chat/logs/spring.log`
2. Docker logs: `docker logs chat-service`
3. Kafka logs: `docker logs kafka`
4. PostgreSQL logs: `docker logs postgres`
5. Redis logs: `docker logs redis`

Happy Testing! 🚀
