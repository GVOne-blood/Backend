package com.theblood.springfood.chat.config;

import org.springframework.context.annotation.Configuration;

/**
 * WebSocket Authentication Configuration
 * 
 * This configuration is now handled by WebSocketAuthInterceptor and WebSocketConfig.
 * Kept for backward compatibility but functionality moved to dedicated components.
 * 
 * @deprecated Use WebSocketAuthInterceptor and WebSocketConfig instead
 */
@Configuration
@Deprecated
public class WebSocketAuthConfig {
    // Authentication logic moved to WebSocketAuthInterceptor
    // Configuration moved to WebSocketConfig
}
