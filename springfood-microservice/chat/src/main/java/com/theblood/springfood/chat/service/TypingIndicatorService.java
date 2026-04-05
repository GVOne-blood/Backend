package com.theblood.springfood.chat.service;

import com.theblood.springfood.chat.service.dto.TypingIndicatorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing typing indicators using Redis.
 * Typing indicators are ephemeral and stored in Redis with automatic TTL expiration.
 * 
 * Requirements: 9.2, 9.3, 9.6
 */
@Service
public class TypingIndicatorService {

    private static final Logger LOG = LoggerFactory.getLogger(TypingIndicatorService.class);
    
    private static final long TYPING_TTL_SECONDS = 5;
    private static final String TYPING_KEY_PREFIX = "typing:";

    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMetricsService metricsService;

    public TypingIndicatorService(
        RedisTemplate<String, String> redisTemplate,
        SimpMessagingTemplate messagingTemplate,
        ChatMetricsService metricsService
    ) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.metricsService = metricsService;
    }

    /**
     * Start typing indicator for a user in a conversation.
     * Adds userId to Redis SET with TTL of 5 seconds and broadcasts to conversation.
     * 
     * @param conversationId The conversation ID
     * @param userId The user ID who is typing
     * @param displayName The display name of the user (for broadcast)
     */
    public void startTyping(String conversationId, String userId, String displayName) {
        String key = TYPING_KEY_PREFIX + conversationId;
        
        try {
            // Add userId to Redis SET with TTL
            redisTemplate.opsForSet().add(key, userId);
            redisTemplate.expire(key, TYPING_TTL_SECONDS, TimeUnit.SECONDS);
            
            // Get current typing users
            Set<String> typingUsers = getTypingUsers(conversationId);
            
            // Broadcast to conversation
            broadcastTypingIndicators(conversationId, typingUsers);
            
            LOG.debug("User {} started typing in conversation {}", userId, conversationId);
        } catch (RedisConnectionFailureException e) {
            // Graceful degradation - typing indicators are non-critical
            LOG.warn("Redis unavailable, skipping typing indicator for user {} in conversation {}", 
                userId, conversationId, e);
            
            // Increment Redis errors counter
            metricsService.incrementRedisErrors();
        }
    }

    /**
     * Stop typing indicator for a user in a conversation.
     * Removes userId from Redis SET and broadcasts updated list.
     * 
     * @param conversationId The conversation ID
     * @param userId The user ID who stopped typing
     */
    public void stopTyping(String conversationId, String userId) {
        String key = TYPING_KEY_PREFIX + conversationId;
        
        try {
            // Remove userId from Redis SET
            redisTemplate.opsForSet().remove(key, userId);
            
            // Get remaining typing users
            Set<String> typingUsers = getTypingUsers(conversationId);
            
            // Broadcast updated list
            broadcastTypingIndicators(conversationId, typingUsers);
            
            LOG.debug("User {} stopped typing in conversation {}", userId, conversationId);
        } catch (RedisConnectionFailureException e) {
            // Graceful degradation
            LOG.warn("Redis unavailable, skipping typing indicator cleanup for user {} in conversation {}", 
                userId, conversationId, e);
            
            // Increment Redis errors counter
            metricsService.incrementRedisErrors();
        }
    }

    /**
     * Get all users currently typing in a conversation.
     * 
     * @param conversationId The conversation ID
     * @return Set of user IDs currently typing
     */
    public Set<String> getTypingUsers(String conversationId) {
        String key = TYPING_KEY_PREFIX + conversationId;
        
        try {
            Set<String> members = redisTemplate.opsForSet().members(key);
            return members != null ? members : Collections.emptySet();
        } catch (RedisConnectionFailureException e) {
            // Graceful degradation
            LOG.warn("Redis unavailable, returning empty typing users for conversation {}", 
                conversationId, e);
            
            // Increment Redis errors counter
            metricsService.incrementRedisErrors();
            
            return Collections.emptySet();
        }
    }

    /**
     * Broadcast typing indicators to all participants in a conversation.
     * 
     * @param conversationId The conversation ID
     * @param typingUserIds Set of user IDs currently typing
     */
    private void broadcastTypingIndicators(String conversationId, Set<String> typingUserIds) {
        try {
            TypingIndicatorEvent event = new TypingIndicatorEvent(conversationId, typingUserIds);
            messagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId + "/typing",
                event
            );
            LOG.debug("Broadcast typing indicators for conversation {}: {}", conversationId, typingUserIds);
        } catch (Exception e) {
            LOG.error("Failed to broadcast typing indicators for conversation {}", conversationId, e);
        }
    }

    /**
     * Clear all typing indicators for a user across all conversations.
     * Called when user disconnects from WebSocket.
     * Scans all typing keys in Redis and removes the user from each set.
     * 
     * @param userId The user ID
     */
    public void clearAllTypingForUser(String userId) {
        try {
            // Scan for all typing keys in Redis
            Set<String> keys = redisTemplate.keys(TYPING_KEY_PREFIX + "*");
            
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    // Remove user from each typing set
                    Long removed = redisTemplate.opsForSet().remove(key, userId);
                    
                    if (removed != null && removed > 0) {
                        // Extract conversationId from key
                        String conversationId = key.substring(TYPING_KEY_PREFIX.length());
                        
                        // Get remaining typing users and broadcast
                        Set<String> typingUsers = redisTemplate.opsForSet().members(key);
                        if (typingUsers != null) {
                            broadcastTypingIndicators(conversationId, typingUsers);
                        }
                        
                        LOG.debug("Removed user {} from typing indicators in conversation {}", 
                            userId, conversationId);
                    }
                }
            }
            
            LOG.debug("Cleared all typing indicators for user {}", userId);
        } catch (RedisConnectionFailureException e) {
            // Graceful degradation
            LOG.warn("Redis unavailable, skipping typing indicator cleanup for user {}", userId, e);
            
            // Increment Redis errors counter
            metricsService.incrementRedisErrors();
        } catch (Exception e) {
            LOG.warn("Failed to clear typing indicators for user {}", userId, e);
        }
    }
}
