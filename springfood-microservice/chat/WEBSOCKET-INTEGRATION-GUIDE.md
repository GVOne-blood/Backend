# WebSocket Integration Guide - SpringFood Chat Service

## 🎯 Tổng quan

Chat service đã được tích hợp đầy đủ WebSocket với STOMP protocol để hỗ trợ:
- ✅ Real-time AI chat với streaming response
- ✅ JWT authentication
- ✅ Auto-reconnect
- ✅ Heartbeat monitoring
- ✅ Error handling

## 📡 Backend Configuration

### WebSocket Endpoints

**Native WebSocket:**
```
ws://localhost:9098/ws
```

**SockJS Fallback:**
```
ws://localhost:9098/ws-sockjs
```

### STOMP Destinations

**Client → Server (Application Prefix: `/app`):**
```
/app/ai-assistant/chat     → Send AI chat message
/app/chat.send             → Send regular chat message
/app/chat.typing           → Send typing indicator
/app/chat.read             → Send read receipt
```

**Server → Client (User Queue: `/user/queue`):**
```
/user/queue/ai-assistant/response  → AI response chunks (streaming)
/user/queue/ai-assistant/complete  → AI response complete signal
/user/queue/ai-assistant/error     → AI error messages
/user/queue/messages               → Regular chat messages
/user/queue/errors                 → Error notifications
```

### Configuration (application-dev.yml)

```yaml
spring:
  websocket:
    allowed-origins: "http://localhost:4200,http://localhost:8080,http://127.0.0.1:4200"

server:
  port: 9098
```

### Authentication

WebSocket requires JWT token in connection headers:

```javascript
{
  Authorization: "Bearer <jwt-token>"
}
```

Token is validated during STOMP CONNECT frame by `WebSocketAuthInterceptor`.

## 🎨 Frontend Integration (Angular 19)

### 1. Install Dependencies

```bash
npm install @stomp/rx-stomp @stomp/stompjs
```

### 2. WebSocket Service (Already Implemented)

Location: `src/app/services/websocket.service.ts`

**Key Features:**
- ✅ Signal-based reactive state
- ✅ Auto JWT token extraction from cookies/localStorage
- ✅ Auto-reconnect with 5s delay
- ✅ Heartbeat (10s client ↔ 10s server)
- ✅ Computed signals for connection state
- ✅ RxJS to Signal conversion with `toSignal()`

**Usage:**

```typescript
import { inject } from '@angular/core';
import { WebSocketService } from './services/websocket.service';

export class ChatComponent {
  private wsService = inject(WebSocketService);
  
  // Computed signals
  isConnected = computed(() => this.wsService.isConnected());
  isTyping = computed(() => this.wsService.isTyping());
  
  constructor() {
    // Connect to WebSocket
    this.wsService.connect();
    
    // Listen for AI chunks
    effect(() => {
      const chunk = this.wsService.aiChunk();
      if (chunk) {
        console.log('AI chunk:', chunk);
      }
    });
    
    // Listen for completion
    effect(() => {
      const complete = this.wsService.aiComplete();
      if (complete) {
        console.log('AI complete');
      }
    });
  }
  
  sendMessage(message: string): void {
    this.wsService.sendAIMessage(message);
  }
}
```

### 3. Chat Modal Component (Already Implemented)

Location: `src/app/components/chat-modal/chat-modal.component.ts`

**Features:**
- ✅ Auto-connect on init
- ✅ Real-time streaming response
- ✅ Authentication check
- ✅ Error handling
- ✅ Message history

## 🧪 Testing

### 1. Start Backend Services

```bash
# Terminal 1: Start Eureka (optional)
cd eureka-server
mvn spring-boot:run

# Terminal 2: Start Chat Service
cd chat
mvn spring-boot:run

# Wait for: "Started ChatApp in X seconds"
```

### 2. Verify WebSocket Endpoint

```bash
# Check if WebSocket endpoint is accessible
curl -i -N \
  -H "Connection: Upgrade" \
  -H "Upgrade: websocket" \
  -H "Sec-WebSocket-Version: 13" \
  -H "Sec-WebSocket-Key: SGVsbG8sIHdvcmxkIQ==" \
  http://localhost:9098/ws
```

Expected response: `HTTP/1.1 101 Switching Protocols`

### 3. Start Frontend

```bash
cd springfood-frontend/springfood
npm start

# Runs on http://localhost:4200
```

### 4. Test Chat Flow

1. **Login** to get JWT token
2. **Open chat bubble** (bottom-right corner)
3. **Send message**: "Xin chào"
4. **Check browser console**:

```
[WebSocket] Connecting to SpringFood AI Assistant...
[WebSocket] Authenticating with JWT token
[WebSocket] ✅ Connected to SpringFood AI Assistant
[Chat] 📤 Sending message to AI: { length: 8, connected: true }
[Chat] 📨 Received AI chunk: Xin chào! Tôi có thể giúp gì...
[Chat] ✅ AI response complete
```

### 5. Test with HTML Client

Use test file: `chat/test-resources/websocket-test-client.html`

```html
<!DOCTYPE html>
<html>
<head>
  <title>SpringFood WebSocket Test</title>
  <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.0.0/bundles/stomp.umd.min.js"></script>
</head>
<body>
  <h1>SpringFood AI Chat Test</h1>
  
  <div>
    <label>JWT Token:</label>
    <input id="token" type="text" size="100" placeholder="Paste JWT token here" />
  </div>
  
  <div>
    <button onclick="connect()">Connect</button>
    <button onclick="disconnect()">Disconnect</button>
    <span id="status">Disconnected</span>
  </div>
  
  <div>
    <input id="message" type="text" placeholder="Type message..." />
    <button onclick="sendMessage()">Send</button>
  </div>
  
  <div id="messages" style="border: 1px solid #ccc; height: 400px; overflow-y: scroll;"></div>
  
  <script>
    let stompClient = null;
    
    function connect() {
      const token = document.getElementById('token').value;
      if (!token) {
        alert('Please enter JWT token');
        return;
      }
      
      stompClient = new StompJs.Client({
        brokerURL: 'ws://localhost:9098/ws',
        connectHeaders: {
          Authorization: `Bearer ${token}`
        },
        debug: (str) => console.log(str),
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000
      });
      
      stompClient.onConnect = (frame) => {
        console.log('Connected:', frame);
        document.getElementById('status').textContent = 'Connected';
        
        // Subscribe to AI responses
        stompClient.subscribe('/user/queue/ai-assistant/response', (message) => {
          appendMessage('AI (chunk)', message.body);
        });
        
        stompClient.subscribe('/user/queue/ai-assistant/complete', (message) => {
          appendMessage('System', 'AI response complete');
        });
        
        stompClient.subscribe('/user/queue/ai-assistant/error', (message) => {
          appendMessage('Error', message.body);
        });
      };
      
      stompClient.onStompError = (frame) => {
        console.error('STOMP error:', frame);
        document.getElementById('status').textContent = 'Error: ' + frame.headers.message;
      };
      
      stompClient.activate();
    }
    
    function disconnect() {
      if (stompClient) {
        stompClient.deactivate();
        document.getElementById('status').textContent = 'Disconnected';
      }
    }
    
    function sendMessage() {
      const message = document.getElementById('message').value;
      if (!message || !stompClient || !stompClient.connected) {
        alert('Not connected or empty message');
        return;
      }
      
      stompClient.publish({
        destination: '/app/ai-assistant/chat',
        body: JSON.stringify({
          message: message
        })
      });
      
      appendMessage('You', message);
      document.getElementById('message').value = '';
    }
    
    function appendMessage(sender, content) {
      const div = document.createElement('div');
      div.innerHTML = `<strong>${sender}:</strong> ${content}`;
      document.getElementById('messages').appendChild(div);
      document.getElementById('messages').scrollTop = document.getElementById('messages').scrollHeight;
    }
  </script>
</body>
</html>
```

## 🔧 Troubleshooting

### Issue 1: Connection Refused

**Symptom:**
```
[WebSocket] ❌ Connection error: Error: Connection refused
```

**Solution:**
1. Check if chat service is running: `curl http://localhost:9098/actuator/health`
2. Check if port 9098 is available: `netstat -an | findstr 9098`
3. Check firewall settings

### Issue 2: Authentication Failed

**Symptom:**
```
[WebSocket] ❌ STOMP error: Authentication failed
```

**Solution:**
1. Verify JWT token is valid: Check expiration
2. Check token is in cookies or localStorage
3. Login again to get fresh token

### Issue 3: No Response from AI

**Symptom:**
- Message sent successfully
- No AI response received

**Solution:**
1. Check Gemini API key in `.env`: `GEMINI_API_KEY=your-key`
2. Check backend logs for errors
3. Verify Gemini API quota: https://aistudio.google.com/app/apikey

### Issue 4: CORS Error

**Symptom:**
```
Access to XMLHttpRequest blocked by CORS policy
```

**Solution:**
Update `application-dev.yml`:
```yaml
spring:
  websocket:
    allowed-origins: "http://localhost:4200,http://your-domain.com"
```

## 📊 Monitoring

### Backend Logs

```bash
# Watch chat service logs
tail -f chat/logs/spring.log

# Filter WebSocket logs
tail -f chat/logs/spring.log | grep WebSocket
```

### Frontend Console

```javascript
// Enable verbose logging
localStorage.setItem('debug', 'stomp:*');
```

### Metrics

Chat service exposes metrics at:
```
http://localhost:9098/actuator/metrics
```

Key metrics:
- `websocket.connections.active` - Active WebSocket connections
- `ai.chat.requests` - AI chat requests count
- `ai.chat.duration` - AI response time

## 🚀 Production Checklist

- [ ] Use secure WebSocket (wss://)
- [ ] Configure proper CORS origins
- [ ] Enable rate limiting
- [ ] Set up monitoring/alerting
- [ ] Configure load balancer for WebSocket
- [ ] Use Redis for session storage (multi-instance)
- [ ] Enable SSL/TLS
- [ ] Set up CDN for static assets
- [ ] Configure proper logging
- [ ] Set up backup/recovery

## 📚 References

- [Spring WebSocket Documentation](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [STOMP Protocol](https://stomp.github.io/)
- [RxStomp Documentation](https://stomp-js.github.io/guide/rx-stomp/)
- [Angular Signals Guide](https://angular.dev/guide/signals)
