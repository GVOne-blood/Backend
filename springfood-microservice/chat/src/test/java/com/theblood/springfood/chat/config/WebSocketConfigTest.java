package com.theblood.springfood.chat.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * Unit tests for {@link WebSocketConfig}.
 * 
 * Tests WebSocket configuration including:
 * - STOMP endpoint registration at /ws
 * - SockJS fallback support
 * - Simple Broker configuration with correct prefixes
 * - Message size limits
 * 
 * Requirements: 15.1, 15.2, 15.5
 */
class WebSocketConfigTest {

    private WebSocketAuthInterceptor webSocketAuthInterceptor;
    private StompEndpointRegistry stompEndpointRegistry;
    private StompWebSocketEndpointRegistration stompWebSocketEndpointRegistration;
    private MessageBrokerRegistry messageBrokerRegistry;
    private ChannelRegistration channelRegistration;
    private WebSocketTransportRegistration webSocketTransportRegistration;
    private WebSocketConfig webSocketConfig;

    @BeforeEach
    void setup() {
        webSocketAuthInterceptor = mock(WebSocketAuthInterceptor.class);
        stompEndpointRegistry = mock(StompEndpointRegistry.class);
        stompWebSocketEndpointRegistration = mock(StompWebSocketEndpointRegistration.class);
        messageBrokerRegistry = mock(MessageBrokerRegistry.class, RETURNS_DEEP_STUBS);
        channelRegistration = mock(ChannelRegistration.class, RETURNS_DEEP_STUBS);
        webSocketTransportRegistration = mock(WebSocketTransportRegistration.class);
        
        webSocketConfig = new WebSocketConfig(webSocketAuthInterceptor);
        // Set default allowed origins
        ReflectionTestUtils.setField(webSocketConfig, "allowedOrigins", "*");
    }

    /**
     * Test STOMP endpoint registered at /ws
     * 
     * Requirement 15.1: STOMP endpoint at /ws with SockJS fallback enabled
     */
    @Test
    void shouldRegisterStompEndpointAtWs() {
        // Given
        when(stompEndpointRegistry.addEndpoint(anyString())).thenReturn(stompWebSocketEndpointRegistration);
        when(stompWebSocketEndpointRegistration.setAllowedOriginPatterns(any(String[].class)))
            .thenReturn(stompWebSocketEndpointRegistration);

        // When
        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        // Then
        verify(stompEndpointRegistry).addEndpoint("/ws");
    }

    /**
     * Test SockJS fallback enabled
     * 
     * Requirement 15.1: SockJS fallback support for browsers that don't support WebSocket
     */
    @Test
    void shouldEnableSockJsFallback() {
        // Given
        when(stompEndpointRegistry.addEndpoint(anyString())).thenReturn(stompWebSocketEndpointRegistration);
        when(stompWebSocketEndpointRegistration.setAllowedOriginPatterns(any(String[].class)))
            .thenReturn(stompWebSocketEndpointRegistration);

        // When
        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        // Then
        verify(stompWebSocketEndpointRegistration).withSockJS();
    }

    /**
     * Test allowed origins configuration
     * 
     * Requirement 15.1: Allowed origins configured from application properties
     */
    @Test
    void shouldConfigureAllowedOrigins() {
        // Given
        ReflectionTestUtils.setField(webSocketConfig, "allowedOrigins", "http://localhost:3000,http://localhost:4200");
        when(stompEndpointRegistry.addEndpoint(anyString())).thenReturn(stompWebSocketEndpointRegistration);
        when(stompWebSocketEndpointRegistration.setAllowedOriginPatterns(any(String[].class)))
            .thenReturn(stompWebSocketEndpointRegistration);

        // When
        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        // Then
        ArgumentCaptor<String[]> originsCaptor = ArgumentCaptor.forClass(String[].class);
        verify(stompWebSocketEndpointRegistration).setAllowedOriginPatterns(originsCaptor.capture());
        
        String[] capturedOrigins = originsCaptor.getValue();
        assertThat(capturedOrigins).hasSize(2);
        assertThat(capturedOrigins).contains("http://localhost:3000", "http://localhost:4200");
    }

    /**
     * Test Simple Broker configured with correct prefixes
     * 
     * Requirement 15.2: Enable Simple Broker with /topic and /queue prefixes
     * Requirement 15.3: Set application destination prefix to /app
     * Requirement 15.4: Set user destination prefix to /user
     */
    @Test
    void shouldConfigureSimpleBrokerWithCorrectPrefixes() {
        // When
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        // Then
        // Verify Simple Broker enabled with /topic and /queue
        verify(messageBrokerRegistry).enableSimpleBroker("/topic", "/queue");
        
        // Verify application destination prefix set to /app
        verify(messageBrokerRegistry).setApplicationDestinationPrefixes("/app");
        
        // Verify user destination prefix set to /user
        verify(messageBrokerRegistry).setUserDestinationPrefix("/user");
    }

    /**
     * Test message size limits configuration
     * 
     * Requirement 15.5: Set maximum text message buffer size to 64KB and binary message buffer size to 512KB
     */
    @Test
    void shouldConfigureMessageSizeLimits() {
        // Given
        when(webSocketTransportRegistration.setMessageSizeLimit(anyInt()))
            .thenReturn(webSocketTransportRegistration);
        when(webSocketTransportRegistration.setSendBufferSizeLimit(anyInt()))
            .thenReturn(webSocketTransportRegistration);
        when(webSocketTransportRegistration.setSendTimeLimit(anyInt()))
            .thenReturn(webSocketTransportRegistration);

        // When
        webSocketConfig.configureWebSocketTransport(webSocketTransportRegistration);

        // Then
        // Verify text message size limit: 64KB
        verify(webSocketTransportRegistration).setMessageSizeLimit(64 * 1024);
        
        // Verify binary message buffer size limit: 512KB
        verify(webSocketTransportRegistration).setSendBufferSizeLimit(512 * 1024);
        
        // Verify send timeout: 20 seconds
        verify(webSocketTransportRegistration).setSendTimeLimit(20 * 1000);
    }

    /**
     * Test JWT authentication interceptor registration
     * 
     * Requirement 15.7: Register JWT authentication interceptor on inbound channel
     */
    @Test
    void shouldRegisterAuthenticationInterceptor() {
        // When
        webSocketConfig.configureClientInboundChannel(channelRegistration);

        // Then
        verify(channelRegistration).interceptors(webSocketAuthInterceptor);
    }

    /**
     * Test wildcard allowed origins configuration
     * 
     * Requirement 15.1: Support wildcard for allowed origins in development
     */
    @Test
    void shouldSupportWildcardAllowedOrigins() {
        // Given
        ReflectionTestUtils.setField(webSocketConfig, "allowedOrigins", "*");
        when(stompEndpointRegistry.addEndpoint(anyString())).thenReturn(stompWebSocketEndpointRegistration);
        when(stompWebSocketEndpointRegistration.setAllowedOriginPatterns(any(String[].class)))
            .thenReturn(stompWebSocketEndpointRegistration);

        // When
        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        // Then
        ArgumentCaptor<String[]> originsCaptor = ArgumentCaptor.forClass(String[].class);
        verify(stompWebSocketEndpointRegistration).setAllowedOriginPatterns(originsCaptor.capture());
        
        String[] capturedOrigins = originsCaptor.getValue();
        assertThat(capturedOrigins).hasSize(1);
        assertThat(capturedOrigins[0]).isEqualTo("*");
    }
}
