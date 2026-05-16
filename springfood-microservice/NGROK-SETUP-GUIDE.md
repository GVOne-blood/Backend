# Ngrok Setup Guide - Expose Localhost to Internet

## 📚 Ngrok là gì?

**Ngrok** là một công cụ cho phép bạn **expose local server** (localhost) ra internet thông qua một **secure tunnel**. 

### Cách hoạt động

```
┌─────────────────────────────────────────────────────────────┐
│                    NGROK ARCHITECTURE                        │
└─────────────────────────────────────────────────────────────┘

Internet Client (Vercel)
         │
         │ HTTPS Request
         ▼
┌─────────────────────┐
│  Ngrok Cloud Server │  ← Public URL: https://abc123.ngrok.io
│  (ngrok.com)        │
└──────────┬──────────┘
           │
           │ Secure Tunnel (TLS)
           │
           ▼
┌─────────────────────┐
│  Ngrok Client       │  ← Running on your PC
│  (Your Computer)    │
└──────────┬──────────┘
           │
           │ Forward to localhost
           ▼
┌─────────────────────┐
│  Your Backend       │  ← http://localhost:8080
│  (API Gateway)      │
└─────────────────────┘
```

### Tại sao cần Ngrok?

**Vấn đề:**
- Frontend deploy trên Vercel (cloud)
- Backend chạy trên localhost (máy bạn)
- Vercel không thể truy cập localhost của bạn

**Giải pháp:**
- Ngrok tạo một public URL (https://abc123.ngrok.io)
- URL này forward requests về localhost:8080
- Vercel có thể call được backend

---

## 🚀 Installation

### Windows (Chocolatey)
```bash
choco install ngrok
```

### Windows (Manual)
1. Download: https://ngrok.com/download
2. Extract `ngrok.exe` to `C:\ngrok\`
3. Add to PATH: `setx PATH "%PATH%;C:\ngrok"`

### Verify Installation
```bash
ngrok version
# Output: ngrok version 3.x.x
```

---

## 🔑 Authentication (Required)

### 1. Create Free Account
- Visit: https://dashboard.ngrok.com/signup
- Sign up (free tier: 1 online tunnel, 40 connections/min)

### 2. Get Auth Token
- Dashboard: https://dashboard.ngrok.com/get-started/your-authtoken
- Copy your authtoken

### 3. Configure Ngrok
```bash
ngrok config add-authtoken YOUR_AUTH_TOKEN_HERE
```

Example:
```bash
ngrok config add-authtoken 2abc123def456ghi789jkl0mno1pqr2stu3vwx
```

This saves token to: `C:\Users\ADMIN\.ngrok2\ngrok.yml`

---

## 💻 Basic Usage

### Expose HTTP Server
```bash
# Expose port 8080 (API Gateway)
ngrok http 8080
```

**Output:**
```
ngrok                                                                    

Session Status                online
Account                       your-email@gmail.com (Plan: Free)
Version                       3.5.0
Region                        Asia Pacific (ap)
Latency                       45ms
Web Interface                 http://127.0.0.1:4040
Forwarding                    https://abc123def456.ngrok.io -> http://localhost:8080

Connections                   ttl     opn     rt1     rt5     p50     p90
                              0       0       0.00    0.00    0.00    0.00
```

**Important URLs:**
- **Public URL**: `https://abc123def456.ngrok.io` ← Use this in frontend
- **Web Interface**: `http://127.0.0.1:4040` ← Monitor requests

### Expose Multiple Ports (Paid Plan)
```bash
# Terminal 1: API Gateway
ngrok http 8080

# Terminal 2: Chat WebSocket (requires paid plan)
ngrok http 8086
```

---

## 🎯 SpringFood Setup

### Step 1: Start Backend Services
```bash
cd springfood-microservice

# Start infrastructure
docker-compose up -d

# Start API Gateway
cd api-gateway
mvn spring-boot:run

# Verify: http://localhost:8080/actuator/health
```

### Step 2: Start Ngrok Tunnel
```bash
ngrok http 8080
```

**Copy the HTTPS URL:**
```
Forwarding: https://abc123def456.ngrok.io -> http://localhost:8080
```

### Step 3: Update Frontend Environment
```typescript
// springfood/src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://abc123def456.ngrok.io/api/v1', // ← Paste ngrok URL
  wsUrl: 'wss://abc123def456.ngrok.io/ws',
};
```

### Step 4: Update Backend CORS (Important!)
```java
// api-gateway/src/main/resources/application.yml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "http://localhost:4200"
              - "https://your-vercel-app.vercel.app"
              - "https://*.ngrok.io"  # ← Add this
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
```

### Step 5: Deploy Frontend to Vercel
```bash
cd springfood
npm run build
vercel --prod
```

### Step 6: Test
```bash
# From Vercel frontend
curl https://abc123def456.ngrok.io/api/v1/products
```

---

## 🔧 Advanced Features

### Custom Subdomain (Paid Plan)
```bash
ngrok http 8080 --subdomain=springfood-api
# URL: https://springfood-api.ngrok.io
```

### Configuration File
Create `ngrok.yml`:
```yaml
version: "2"
authtoken: YOUR_AUTH_TOKEN

tunnels:
  api-gateway:
    proto: http
    addr: 8080
    subdomain: springfood-api  # Paid plan only
    
  chat-websocket:
    proto: http
    addr: 8086
    subdomain: springfood-chat  # Paid plan only
```

Start all tunnels:
```bash
ngrok start --all --config ngrok.yml
```

### Request Inspection
Visit: http://127.0.0.1:4040

Features:
- See all HTTP requests/responses
- Replay requests
- Filter by status code
- View headers, body, timing

---

## 📊 Free vs Paid Plans

### Free Plan
- ✅ 1 online tunnel
- ✅ HTTPS/HTTP tunnels
- ✅ 40 connections/minute
- ✅ Random subdomain
- ❌ No custom subdomain
- ❌ No reserved domains
- ❌ No IP whitelisting

### Paid Plans (Starting $8/month)
- ✅ Multiple tunnels
- ✅ Custom subdomains
- ✅ Reserved domains
- ✅ IP whitelisting
- ✅ Higher connection limits
- ✅ More regions

---

## ⚠️ Limitations & Considerations

### Free Tier Limits
- **1 tunnel only** - Can't expose both API Gateway + WebSocket
- **Random URL** - Changes every restart
- **40 req/min** - May hit limit during testing
- **8 hours max** - Tunnel auto-closes after 8h

### Security Concerns
- ⚠️ **Public URL** - Anyone with URL can access
- ⚠️ **No authentication** - Ngrok doesn't add auth
- ⚠️ **Temporary** - Not for production

### Solutions
1. **Add API authentication** - JWT tokens
2. **IP whitelisting** (paid plan)
3. **Short-lived tunnels** - Restart when needed
4. **Use for demo only** - Not production

---

## 🛠️ Troubleshooting

### Error: "Failed to start tunnel"
```bash
# Check if port is already in use
netstat -ano | findstr :8080

# Kill process
taskkill /PID <PID> /F
```

### Error: "Account limit reached"
- Free plan: 1 tunnel only
- Solution: Stop other tunnels or upgrade

### CORS Errors
```java
// Add ngrok domain to CORS config
allowedOrigins:
  - "https://*.ngrok.io"
```

### WebSocket Connection Failed
```typescript
// Use WSS (secure WebSocket)
wsUrl: 'wss://abc123def456.ngrok.io/ws'
```

### Slow Response Times
- Ngrok adds ~50-200ms latency
- Use closest region: `ngrok http 8080 --region=ap` (Asia Pacific)

---

## 🎓 Best Practices

### 1. Use Environment Variables
```typescript
// Don't hardcode ngrok URL
export const environment = {
  apiUrl: process.env['API_URL'] || 'http://localhost:8080/api/v1'
};
```

### 2. Monitor Requests
- Always keep http://127.0.0.1:4040 open
- Check for errors, slow requests

### 3. Restart Tunnel Regularly
- Free tier: URL changes on restart
- Update frontend environment after restart

### 4. Use HTTPS Only
```bash
# Ngrok provides HTTPS by default
ngrok http 8080
# ✅ https://abc123.ngrok.io
# ❌ http://abc123.ngrok.io (redirects to HTTPS)
```

### 5. Add Request Logging
```java
// Log all incoming requests
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Request: {} {}", 
            exchange.getRequest().getMethod(),
            exchange.getRequest().getURI()
        );
        return chain.filter(exchange);
    }
}
```

---

## 🚀 Quick Start Checklist

- [ ] Install ngrok
- [ ] Create account & get authtoken
- [ ] Configure authtoken: `ngrok config add-authtoken TOKEN`
- [ ] Start backend: `mvn spring-boot:run`
- [ ] Start ngrok: `ngrok http 8080`
- [ ] Copy public URL
- [ ] Update frontend environment
- [ ] Update backend CORS
- [ ] Test API call
- [ ] Deploy frontend to Vercel
- [ ] Test from Vercel

---

## 📚 Resources

- Official Docs: https://ngrok.com/docs
- Dashboard: https://dashboard.ngrok.com
- Pricing: https://ngrok.com/pricing
- Status: https://status.ngrok.com

---

## 🎯 Alternative Solutions

### For Production
1. **Deploy Backend to Cloud**
   - Railway: https://railway.app
   - Render: https://render.com
   - Fly.io: https://fly.io

2. **Cloudflare Tunnel** (Free)
   ```bash
   cloudflared tunnel --url http://localhost:8080
   ```

3. **Tailscale** (VPN-based)
   - Private network
   - No public exposure

### For Development
- **Ngrok** - Best for quick demos
- **LocalTunnel** - Open source alternative
- **Serveo** - SSH-based tunneling

---

## 💡 Pro Tips

1. **Save Ngrok URL to file**
   ```bash
   ngrok http 8080 > ngrok.log &
   grep -o 'https://[^[:space:]]*' ngrok.log
   ```

2. **Auto-update frontend**
   ```bash
   # Script to update environment.ts with ngrok URL
   NGROK_URL=$(curl -s http://localhost:4040/api/tunnels | jq -r '.tunnels[0].public_url')
   echo "export const environment = { apiUrl: '$NGROK_URL/api/v1' }" > src/environments/environment.ts
   ```

3. **Use ngrok with Docker**
   ```yaml
   # docker-compose.yml
   ngrok:
     image: ngrok/ngrok:latest
     command: http api-gateway:8080
     environment:
       NGROK_AUTHTOKEN: ${NGROK_AUTHTOKEN}
     ports:
       - "4040:4040"
   ```

---

**Happy Tunneling! 🚇**
