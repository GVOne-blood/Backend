package com.theblood.springfood.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Đăng ký STOMP endpoints - điểm kết nối cho WebSocket clients
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")  // Endpoint URL: ws://localhost:8080/ws/chat
            .setAllowedOriginPatterns("*")  // CORS - cho phép tất cả origins (dev only)
            .withSockJS();  // Enable SockJS fallback
    }

    /**
     * Cấu hình Message Broker để route messages
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 1. Application Destination Prefix
        // Messages gửi từ client đến server sẽ có prefix "/app"
        // VD: client send to "/app/chat.sendMessage" -> @MessageMapping("/chat.sendMessage")
        config.setApplicationDestinationPrefixes("/app");

        // 2. Simple In-Memory Broker
        // Server sẽ broadcast messages đến clients subscribe các destinations có prefix này
        // /topic - cho broadcast (nhiều users)
        // /queue - cho private messages (1-1)
        config.enableSimpleBroker("/topic", "/queue");

        // 3. User Destination Prefix (cho private messaging)
        // Khi gửi đến "/user/queue/...", Spring tự động resolve thành /user/{username}/queue/...
        config.setUserDestinationPrefix("/user");
    }

    /**
     * (Optional) Cấu hình Message Size Limits
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
            .setMessageSizeLimit(128 * 1024)  // 128 KB
            .setSendBufferSizeLimit(512 * 1024)  // 512 KB
            .setSendTimeLimit(20 * 1000);  // 20 seconds
    }
}
