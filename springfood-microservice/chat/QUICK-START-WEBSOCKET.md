# 🚀 Quick Start - WebSocket Chat

## 1️⃣ Start Chat Service (5 seconds)

```bash
cd f:\Document\TASC\Backend\springfood-microservice\chat
mvn spring-boot:run
```

**Wait for:** `Started ChatApp in X seconds`

## 2️⃣ Test với HTML Client (10 seconds)

```bash
# Mở test client
start test-resources\websocket-angular-test.html
```

**Hoặc chạy script:**
```bash
TEST-WEBSOCKET.bat
```

## 3️⃣ Get JWT Token

### Option A: Từ Angular App
1. Login tại `http://localhost:4200`
2. F12 → Application → Cookies
3. Copy `access_token` value

### Option B: Từ Postman
```bash
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```

Copy `access_token` từ response

## 4️⃣ Test Chat

1. Paste JWT token vào test client
2. Click **Connect**
3. Send message: `"Xin chào"`
4. See streaming response! ✨

## 🎯 Expected Result

```
Status: Connected ✅
You: Xin chào
AI: Xin chào! Tôi là trợ lý AI của SpringFood...
System: AI response complete
```

## 🐛 Troubleshooting

### Connection Refused?
```bash
# Check service
curl http://localhost:9098/actuator/health
```

### Authentication Failed?
- Get fresh JWT token (login again)
- Check token is not expired

### No AI Response?
- Check `.env` has `GEMINI_API_KEY`
- Check backend logs: `logs/spring.log`

## 📚 Full Documentation

- Backend: `WEBSOCKET-INTEGRATION-GUIDE.md`
- Frontend: `../Frontend/Springfood-frontend/springfood/WEBSOCKET-CHAT-INTEGRATION.md`
- Summary: `../WEBSOCKET-INTEGRATION-SUMMARY.md`
