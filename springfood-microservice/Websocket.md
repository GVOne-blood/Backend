<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

# WebSocket Toàn Diện từ A đến Z

## Giới thiệu

Tài liệu WebSocket Toàn Diện từ A đến Z được xây dựng nhằm hệ thống hóa kiến thức WebSocket trong Spring Boot theo góc nhìn thực tế của một dự án backend hiện đại. Nội dung không chỉ dừng ở việc "dùng được WebSocket", mà tập trung làm rõ luồng xử lý bên trong, vai trò của STOMP, Message Broker, Interceptor, User Destination và Security.

Thông qua tài liệu này, người đọc có thể hiểu rõ cách WebSocket hoạt động từ lúc client kết nối đến khi message được xử lý, bảo mật và phân phối, từ đó tự tin áp dụng WebSocket cho các bài toán realtime như chat, thông báo, hay đồng bộ dữ liệu đa người dùng.

## 1. What is websocket?

WebSocket là giao thức cho phép client và server giao tiếp realtime, hai chiều, liên tục trên một kết nối duy nhất, khác hoàn toàn mô hình request–response của HTTP.

## 2. STOMP

[https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html](https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html)

### Lý thuyết: STOMP là gì?

STOMP (Simple Text Oriented Messaging Protocol) là một giao thức nhắn tin đơn giản, thường dùng trên WebSocket, giúp client và server giao tiếp theo mô hình publish–subscribe.

### Bản chất

- WebSocket chỉ truyền dữ liệu thô (text/binary)
- STOMP định nghĩa cấu trúc \& ý nghĩa message
- Làm cho WebSocket giống hệ thống message queue/broker


### STOMP cung cấp những gì?

- **SEND**: gửi message
- **SUBSCRIBE**: đăng ký nhận message
- **DESTINATION**: gửi/nhận theo địa chỉ (/topic, /queue)
- Publish – Subscribe rõ ràng
- Frame text, dễ debug


### Kiến trúc

- Client không nói chuyện trực tiếp
- Gửi message tới destination
- Broker định tuyến message tới các client đã subscribe


### Vì sao dùng STOMP?

- Không cần tự thiết kế protocol
- Chuẩn hóa cách nhắn tin
- Dễ tích hợp (Spring Boot, SockJS, JS client)

👉 **Một câu kết**: STOMP là giao thức nhắn tin giúp WebSocket có luật giao tiếp rõ ràng: gửi ở đâu, ai nhận, và nhận bằng cách nào.

## 3. Websocket Transform

[https://docs.spring.io/spring-framework/reference/web/websocket/stomp/server-config.html](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/server-config.html)

WebSocket Transport là cấu hình kiểm soát cách WebSocket và STOMP truyền dữ liệu: buffer bao nhiêu, timeout thế nào, message lớn cỡ nào.

## 4. Flow of Message

[https://docs.spring.io/spring-framework/reference/web/websocket/stomp/message-flow.html](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/message-flow.html)

### Các khái niệm nền tảng:

#### 🔹 Message

Một gói tin gồm:

- payload (nội dung)
- headers (destination, session, user…)


#### 🔹 MessageHandler

Thằng xử lý message:

- Controller (@MessageMapping)
- Message broker
- Interceptor…


#### 🔹 MessageChannel

Đường ống truyền message. Giúp các thành phần không phụ thuộc trực tiếp nhau.

#### 🔹 SubscribableChannel

Channel cho phép nhiều handler cùng nghe.

### 3 Message Channel quan trọng nhất

#### 1️⃣ clientInboundChannel

📥 Tin đi từ client → server

Dùng để:

- Auth
- Validate
- Route tới controller hoặc broker


#### 2️⃣ brokerChannel

📤 Tin từ code server → message broker

Dùng khi:

- Controller gửi message
- HTTP request trigger push realtime


#### 3️⃣ clientOutboundChannel

📤 Tin từ server → client

Dùng để:

- Gửi MESSAGE frame cho client đã subscribe


### Luồng xử lý message chi tiết

#### Bước 1 – Client gửi message

Đầu tiên, WebSocket client sẽ gửi STOMP message SEND lên server. Trong ví dụ này, client có thể gửi message tới hai destination khác nhau:

- /app/a
- /topic/a


#### Bước 2 – Request Channel

Tất cả message từ client gửi lên đều đi vào request channel (request channel chính là clientInboundChannel). Đây là cổng vào duy nhất của server.

#### Bước 3 – Phân luồng theo prefix

Sau đó, Spring dựa vào prefix của destination để quyết định xử lý.

**👉 Trường hợp 1: /app**

Nếu destination bắt đầu bằng /app:

- Message sẽ được đưa vào SimpAnnotationMethodMessageHandler
- Và được map tới các method có @MessageMapping trong controller

Hiểu đơn giản: 👉 /app = đưa message vào code backend để xử lý logic.

**👉 Trường hợp 2: /topic**

Nếu destination bắt đầu bằng /topic:

- Message không đi qua controller
- Mà được xử lý trực tiếp bởi SimpleBrokerMessageHandler

Hiểu đơn giản: 👉 /topic = publish message cho các client đã subscribe.

#### Bước 4 – Broker Channel

Khi controller xử lý xong message /app, kết quả trả về sẽ được đưa vào broker channel. Broker channel là kênh để server-side code gửi message vào message broker.

#### Bước 5 – Message Broker

Message broker sẽ:

- Tìm tất cả client đã SUBSCRIBE đúng destination
- Chuẩn bị message để gửi cho họ


#### Bước 6 – Response Channel

Cuối cùng, broker gửi message qua response channel (tức clientOutboundChannel):

- Message được encode thành STOMP MESSAGE frame
- Gửi ngược lại cho client qua WebSocket


## 5. Annotated Controllers

[https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-annotations.html](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-annotations.html)

### @MessageMapping

Dùng khi client gửi message lên server. Tương đương @PostMapping trong REST. Có xử lý logic, DB, rồi gửi lại cho client. Message đi qua broker. Dùng cho: chat realtime, gửi dữ liệu, xử lý nghiệp vụ.

```java
@Controller
public class ChatController {
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public String handleChat(String message) {
        return "Server nhận: " + message;
    }
}
```


### @SubscribeMapping

Dùng khi client vừa subscribe. Server trả dữ liệu 1 lần. Không broadcast, không lưu subscription. Không qua broker. Dùng cho: load dữ liệu ban đầu (history, list).

```java
@Controller
public class HistoryController {
    @SubscribeMapping("/history")
    public List<String> history() {
        return List.of("Tin nhắn 1", "Tin nhắn 2");
    }
}
```


### @SendTo

Gửi message cho tất cả client đang subscribe. Broadcast.

```java
@MessageMapping("/notify")
@SendTo("/topic/notifications")
public String notifyAll(String msg) {
    return "Thông báo: " + msg;
}
```


### @SendToUser

Gửi message cho 1 user cụ thể. Thường dùng /queue/*.

```java
@MessageMapping("/private")
@SendToUser("/queue/reply")
public String privateMessage(String msg) {
    return "Riêng tư: " + msg;
}
```


### @MessageExceptionHandler

Bắt lỗi trong WebSocket. Giống @ExceptionHandler của REST.

## 6. Sending Messages

[https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-send.html](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-send.html)

### Bình thường trong WebSocket:

- Client chủ động gửi message lên server
- Server nhận trong @MessageMapping
- Xử lý xong rồi trả kết quả lại cho client

👉 Luồng này là client → server → client

### Nhưng trong thực tế thì khác

Không phải lúc nào client cũng là người bắt đầu câu chuyện. Rất nhiều tình huống:

- Client không gửi gì
- Nhưng server lại có dữ liệu mới
- Và server cần đẩy ngược dữ liệu đó về client ngay lập tức

👉 Đây gọi là server chủ động push dữ liệu

### Ví dụ dễ hình dung

- Có người vừa đăng ký tài khoản
- Admin vừa duyệt đơn
- Một job nền chạy xong
- Một API HTTP được gọi
- Kafka / Database phát sinh event

⟶ Client không hề SEND gì qua WebSocket
⟶ Nhưng UI vẫn cần cập nhật realtime

### Lúc này WebSocket dùng để làm gì?

- Server chủ động bắn message
- Không cần @MessageMapping
- Không cần client yêu cầu trước
- Client chỉ cần SUBSCRIBE và ngồi chờ


### Cách Spring làm việc này

- Server dùng SimpMessagingTemplate
- Gửi message trực tiếp vào broker
- Broker phân phối cho các client đang subscribe

👉 Luồng lúc này là:

```
Server event xảy ra
→ SimpMessagingTemplate
→ Broker
→ WebSocket
→ Client
```


### Một câu chốt để nhớ

- **@MessageMapping**: client nói trước
- **SimpMessagingTemplate**: server chủ động nói trước


## 7. Simple Broker

[https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-simple-broker.html](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-simple-broker.html)

### Simple Broker là gì?

- Broker có sẵn trong Spring
- Chạy trong memory
- Nhận SUBSCRIBE và broadcast message cho client


### Cấu hình

```java
registry.enableSimpleBroker("/topic", "/queue");
```


## 8. External Broker

[https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-broker-relay.html](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-broker-relay.html)

### External Broker là gì?

- Broker chạy bên ngoài app (RabbitMQ, ActiveMQ…)
- Spring không xử lý message
- Spring chỉ chuyển tiếp (relay) message


### Vì sao dùng?

- Simple Broker không scale
- Không hỗ trợ đủ STOMP (ack, receipt…)
- Không dùng cho nhiều server


### External Broker giải quyết gì?

- Scale nhiều instance
- Cluster
- Chat lớn, notification nhiều user


### Spring làm gì?

- Spring mở kết nối TCP tới broker
- Gửi message lên broker
- Nhận message từ broker
- Đẩy lại cho client qua WebSocket

👉 Spring chỉ là cầu nối

### Cấu hình ngắn gọn

```java
registry.enableStompBrokerRelay("/topic", "/queue");
registry.setApplicationDestinationPrefixes("/app");
```

👉 **Một câu chốt**: External Broker = broker xịn, chạy riêng, dùng cho hệ lớn \& realtime thật

## 9. Connecting to a Broker

[https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-broker-relay-configure.html](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-broker-relay-configure.html)

### Connecting to a Broker (STOMP Broker Relay) là gì?

Khi dùng External Broker (RabbitMQ, ActiveMQ) thì Spring không xử lý message, mà chỉ kết nối \& chuyển tiếp.

### Có mấy kết nối TCP?

#### 1. System connection (1 cái duy nhất)

Do server Spring mở. Dùng cho:

- Message server tự gửi (SimpMessagingTemplate)

Có:

- systemLogin
- systemPasscode
- Có heartbeat
- Tự động reconnect nếu broker chết


#### 2. Client connection (mỗi client 1 cái)

Mỗi WebSocket client → 1 TCP connection riêng tới broker

Dùng:

- clientLogin
- clientPasscode
- Client không cần gửi login/pass
- Bảo mật dựa vào HTTP auth khi handshake


### Client cần làm gì?

👉 KHÔNG cần set login/pass STOMP

Chỉ cần:

- HTTP auth
- SUBSCRIBE / SEND bình thường


### Heartbeat để làm gì?

- Giữ kết nối sống
- Phát hiện broker chết
- Spring tự reconnect mỗi 5s


## 10. Authentication

[https://docs.spring.io/spring-framework/reference/web/websocket/stomp/authentication.html](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/authentication.html)

### Xác thực trong STOMP over WebSocket (hiểu nhanh)

#### 1. WebSocket bắt đầu từ đâu?

Luôn bắt đầu bằng HTTP:

- WebSocket handshake
- Hoặc SockJS dùng HTTP fallback


#### 2. User được xác thực lúc nào?

Ngay từ HTTP. Thường qua:

- Login
- Session
- Cookie
- Spring Security

➡️ User đã có sẵn trong HttpServletRequest\#getUserPrincipal()

#### 3. Spring làm gì tiếp theo?

Spring:

- Gắn user đó vào WebSocket session
- Gắn tiếp vào mọi STOMP message

Bạn có thể lấy user qua:

```java
Principal principal
```


#### 4. Có cần dùng login/passcode của STOMP không?

KHÔNG

Với STOMP over WebSocket:

- Spring bỏ qua login/passcode trong CONNECT frame
- Tin vào HTTP authentication


#### 5. Tóm lại 1 câu

STOMP over WebSocket dùng xác thực HTTP, không dùng xác thực STOMP

#### 6. Nhớ nhanh

- Auth = HTTP
- Identity = Principal
- STOMP login/passcode = không dùng (mặc định)


## 11. Token Authentication

[https://docs.spring.io/spring-framework/reference/web/websocket/stomp/authentication-token-based.html](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/authentication-token-based.html)

### Token Authentication (JWT) trong STOMP WebSocket là gì?

#### Vấn đề

WebSocket bắt đầu từ HTTP. Browser:

- Không gửi được custom header khi handshake
- SockJS cũng không gửi được header
- Cookie/session không phải lúc nào cũng dùng được (mobile, stateless)


#### Giải pháp thực tế

👉 Xác thực ở tầng STOMP thay vì HTTP

- Client gửi JWT trong STOMP CONNECT
- Server tự xử lý token


### Cách làm (2 bước)

#### 1. Client gửi token khi CONNECT

Gửi trong STOMP header (vd: Authorization)

#### 2. Server xác thực bằng ChannelInterceptor

```java
@Override
public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                // Lấy JWT từ header
                // Verify JWT
                // accessor.setUser(principal)
            }
            return message;
        }
    });
}
```

➡️ Chỉ cần set User một lần ở CONNECT
➡️ Spring tự gắn user cho mọi message sau

### Lưu ý quan trọng

- STOMP login/passcode không dùng cho JWT
- JWT nằm trong custom STOMP header
- Interceptor phải chạy trước Spring Security: @Order(Ordered.HIGHEST_PRECEDENCE + 99)


### Khi nào dùng cách này?

- Không dùng cookie
- Mobile app
- SPA + JWT
- Stateless backend

👉 **Một câu chốt để nhớ**: JWT + WebSocket = xác thực ở CONNECT bằng ChannelInterceptor

## 12. User Destinations

[https://docs.spring.io/spring-framework/reference/web/websocket/stomp/user-destination.html](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/user-destination.html)

### User Destinations là gì?

- Dùng khi gửi message cho 1 user cụ thể
- Dùng prefix /user
- Client subscribe chung 1 path
- Spring tự tách ra path riêng cho từng user


### Client làm gì?

```
SUBSCRIBE /user/queue/position-updates
```

- Client không cần biết username
- Không sợ đụng message với user khác


### Spring làm gì bên trong?

UserDestinationMessageHandler:

```
/user/queue/xxx
→ /queue/xxx-user123
```

Mỗi user → 1 destination riêng

### Gửi message cho user bằng annotation

```java
@MessageMapping("/trade")
@SendToUser("/queue/position-updates")
public TradeResult trade(...) {
    return result;
}
```

Tự gửi cho user đang gửi message

### Nhiều session thì sao?

Mặc định: gửi cho tất cả session của user

Chỉ gửi cho session hiện tại:

```java
@SendToUser(destinations="/queue/errors", broadcast=false)
```


### Gửi từ Service (không qua controller)

```java
messagingTemplate.convertAndSendToUser(
    username,
    "/queue/position-updates",
    data
);
```


### Không login có dùng được không?

Có, nhưng:

- Chỉ gửi cho session hiện tại
- Giống broadcast=false


## 13. Interception

[https://docs.spring.io/spring-framework/reference/web/websocket/stomp/interceptors.html](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/interceptors.html)

### Interception trong WebSocket là gì?

Interceptor = chốt kiểm soát:

- Chặn và xem mọi STOMP message
- Không chỉ sự kiện CONNECT / DISCONNECT
- Áp dụng cho từng message đi qua channel


### Khác gì với Event?

- Event: báo mốc (connect, disconnect)
- Interceptor: chặn từng message cụ thể

👉 Muốn kiểm soát chi tiết → Interceptor

### ChannelInterceptor dùng khi nào?

- Check JWT / quyền
- Log message
- Block message trái phép
- Modify header / payload


### Intercept inbound message (từ client)

```java
configureClientInboundChannel(...)
```

Chặn message client → server

### preSend() là gì?

Chạy trước khi message được xử lý. Có thể:

- Đọc header
- Chặn message (return null)
- Gắn user


### Dùng StompHeaderAccessor để làm gì?

Lấy:

- Command: CONNECT, SEND, SUBSCRIBE, DISCONNECT
- Destination
- Header


### ExecutorChannelInterceptor là gì?

Giống ChannelInterceptor, nhưng chạy trong thread xử lý message.

Dùng khi cần:

- Transaction
- Context thread-local


### Lưu ý quan trọng

- DISCONNECT có thể bắn nhiều lần
- Code interceptor phải idempotent
- Không xử lý trùng

👉 **Một câu chốt**: Interceptor = nơi kiểm soát mọi message WebSocket trước khi xử lý

