# 🚀 Ngrok Quick Start - SpringFood

## ✅ Setup Complete!

Ngrok đã được cài đặt và cấu hình thành công tại: `C:\ngrok\ngrok.exe`

---

## 📋 Các Bước Tiếp Theo

### 1️⃣ Start Backend Services

```bash
# Terminal 1: Start infrastructure (PostgreSQL, Redis, Kafka, Eureka)
cd springfood-microservice
docker-compose up -d

# Terminal 2: Start API Gateway
cd api-gateway
mvn spring-boot:run
```

Đợi cho đến khi thấy:
```
Started ApiGatewayApplication in X seconds
```

### 2️⃣ Start Ngrok Tunnel

```bash
# Terminal 3: Run the start script
cd springfood-microservice
start-ngrok.bat
```

Hoặc chạy trực tiếp:
```bash
C:\ngrok\ngrok.exe http 8080
```

### 3️⃣ Copy Ngrok URL

Từ output của ngrok, copy **HTTPS URL**:

```
Forwarding    https://abc123def456.ngrok.io -> http://localhost:8080
              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
              Copy this URL!
```

### 4️⃣ Update Frontend Environment

Mở file: `springfood/src/environments/environment.prod.ts`

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://abc123def456.ngrok.io/api/v1',  // ← Paste ngrok URL here
  wsUrl: 'wss://abc123def456.ngrok.io/ws',
};
```

### 5️⃣ Test API Connection

```bash
# Test từ local
curl https://abc123def456.ngrok.io/api/v1/products

# Hoặc mở browser
https://abc123def456.ngrok.io/api/v1/products
```

### 6️⃣ Deploy Frontend to Vercel

```bash
cd springfood
npm run build
vercel --prod
```

### 7️⃣ Test từ Vercel

Mở Vercel app và test các chức năng:
- Login
- Register  
- Browse products
- Add to cart
- Checkout

---

## 🔍 Monitor Requests

Mở Web Interface để xem tất cả requests:

```
http://127.0.0.1:4040
```

Features:
- ✅ See all HTTP requests/responses
- ✅ Replay requests
- ✅ View headers, body, timing
- ✅ Filter by status code

---

## ⚙️ CORS Configuration

Backend đã được cấu hình để accept requests từ:
- ✅ `http://localhost:*` (local development)
- ✅ `https://*.vercel.app` (Vercel deployment)
- ✅ `https://*.ngrok.io` (ngrok tunnel)
- ✅ `https://*.ngrok-free.app` (ngrok free tier)

File: `api-gateway/src/main/java/com/theblood/apigateway/config/SecurityConfig.java`

---

## 🛑 Stop Ngrok

Press `Ctrl+C` trong terminal đang chạy ngrok.

---

## 🔄 Restart Ngrok

**Lưu ý:** Free tier của ngrok sẽ tạo **random URL mỗi lần restart**.

Nếu restart ngrok:
1. Copy URL mới
2. Update lại `environment.prod.ts`
3. Redeploy frontend: `vercel --prod`

---

## ⚠️ Limitations (Free Tier)

- **1 tunnel only** - Không thể expose nhiều ports cùng lúc
- **Random URL** - URL thay đổi mỗi lần restart
- **40 requests/minute** - Có thể hit limit khi test
- **8 hours max** - Tunnel tự động đóng sau 8 giờ

---

## 💡 Pro Tips

### Tip 1: Keep Ngrok Running
Giữ terminal ngrok mở trong suốt quá trình development/demo.

### Tip 2: Use Ngrok Web Interface
Luôn mở `http://127.0.0.1:4040` để monitor requests và debug.

### Tip 3: Test Locally First
Trước khi dùng ngrok, test API locally:
```bash
curl http://localhost:8080/api/v1/products
```

### Tip 4: Check Backend Logs
Nếu có lỗi, check logs của API Gateway:
```bash
cd api-gateway
mvn spring-boot:run
# Watch the console output
```

---

## 🐛 Troubleshooting

### Error: "Failed to start tunnel"
```bash
# Check if port 8080 is in use
netstat -ano | findstr :8080

# Kill the process if needed
taskkill /PID <PID> /F
```

### CORS Errors
Backend đã được config, nhưng nếu vẫn gặp lỗi:
1. Restart API Gateway
2. Clear browser cache
3. Check ngrok URL có đúng không

### WebSocket Connection Failed
Đảm bảo dùng `wss://` (secure WebSocket):
```typescript
wsUrl: 'wss://abc123def456.ngrok.io/ws'
```

### Slow Response
Ngrok adds ~50-200ms latency. Đây là bình thường.

---

## 📚 Resources

- **Ngrok Dashboard**: https://dashboard.ngrok.com
- **Ngrok Docs**: https://ngrok.com/docs
- **Web Interface**: http://127.0.0.1:4040

---

## 🎯 Next Steps

1. ✅ Ngrok installed & configured
2. ✅ CORS updated for ngrok
3. ⏳ Start backend services
4. ⏳ Start ngrok tunnel
5. ⏳ Update frontend environment
6. ⏳ Deploy to Vercel
7. ⏳ Test end-to-end

---

**Happy Coding! 🚀**
