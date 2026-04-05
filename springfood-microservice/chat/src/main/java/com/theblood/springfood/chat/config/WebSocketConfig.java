package com.theblood.springfood.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * WebSocket Configuration for Chat Realtime Core
 * 
 * Configures STOMP over WebSocket with:
 * - STOMP endpoint at /ws with SockJS fallback
 * - Simple in-memory broker for /topic and /queue destinations
 * - Message size limits (64KB text, 512KB binary)
 * - Heartbeat intervals (10s client, 10s server)
 * - Thread pools (2x CPU cores for inbound/outbound channels)
 * 
 * Requirements: 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.websocket.allowed-origins:*}")
    private String allowedOrigins;

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    /**
     * TaskScheduler bean for WebSocket heartbeat mechanism.
     * Required by SimpleBrokerMessageHandler when heartbeat is configured.
     * 
     * @return Configured ThreadPoolTaskScheduler
     */
    @Bean
    public TaskScheduler messageBrokerTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    /**
     * Register STOMP endpoint at /ws with SockJS fallback
     * 
     * Requirement 15.1: Register STOMP endpoint at /ws with SockJS fallback enabled 
     * and allowed origins configured from application properties
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Native WebSocket endpoint (for Postman, mobile apps, etc.)
        registry
            .addEndpoint("/ws")
            .setAllowedOriginPatterns(allowedOrigins.split(","));
        
        // SockJS fallback endpoint (for browsers with SockJS client)
        registry
            .addEndpoint("/ws-sockjs")
            .setAllowedOriginPatterns(allowedOrigins.split(","))
            .withSockJS();
    }

    /**
     * Configure message broker with Simple Broker for /topic and /queue
     * 
     * Requirement 15.2: Enable Simple Broker (in-memory) with destination prefixes /topic and /queue
     * Requirement 15.3: Set application destination prefix to /app for client-to-server messages
     * Requirement 15.4: Set user destination prefix to /user and enable user destination broadcast
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Application destination prefix for client-to-server messages
        registry.setApplicationDestinationPrefixes("/app");

        // Enable Simple Broker (in-memory) for /topic (broadcast) and /queue (point-to-point)
        registry.enableSimpleBroker("/topic", "/queue")
            // Requirement 15.6: Set heartbeat intervals (10s client, 10s server)
            .setHeartbeatValue(new long[]{10000, 10000}) // [server-to-client, client-to-server]
            .setTaskScheduler(messageBrokerTaskScheduler()); // Required for heartbeat

        // User destination prefix for private messaging
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Configure client inbound channel with JWT authentication interceptor
     * 
     * Requirement 15.7: Configure thread pool (2x CPU cores) for inbound channel
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Register JWT authentication interceptor
        registration.interceptors(webSocketAuthInterceptor);

        // Configure thread pool: 2x CPU cores
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        registration.taskExecutor()
            .corePoolSize(corePoolSize)
            .maxPoolSize(corePoolSize)
            .queueCapacity(500);
    }

    /**
     * Configure client outbound channel thread pool
     * 
     * Requirement 15.7: Configure thread pool (2x CPU cores) for outbound channel
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // Configure thread pool: 2x CPU cores
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        registration.taskExecutor()
            .corePoolSize(corePoolSize)
            .maxPoolSize(corePoolSize)
            .queueCapacity(500);
    }

    /**
     * Configure WebSocket transport with message size limits and heartbeat
     * 
     * Requirement 15.5: Set maximum text message buffer size to 64KB and binary message buffer size to 512KB
     * Requirement 15.6: Set heartbeat intervals to detect dead connections
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
            // Requirement 15.5: Message size limits
            .setMessageSizeLimit(64 * 1024)        // 64KB for text messages
            .setSendBufferSizeLimit(512 * 1024)    // 512KB for binary messages
            .setSendTimeLimit(20 * 1000);          // 20 seconds send timeout
    }
}
