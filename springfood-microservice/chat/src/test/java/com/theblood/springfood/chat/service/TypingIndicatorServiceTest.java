package com.theblood.springfood.chat.service;

import com.theblood.springfood.chat.service.dto.TypingIndicatorEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TypingIndicatorService}.
 * Tests Redis-based typing indicator functionality.
 * 
 * Requirements: 9.3, 9.6
 */
@ExtendWith(MockitoExtension.class)
class TypingIndicatorServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatMetricsService metricsService;

    private TypingIndicatorService typingIndicatorService;

    private static final String CONVERSATION_ID = "conv-123";
    private static final String USER_ID = "user-456";
    private static final String DISPLAY_NAME = "John Doe";
    private static final String TYPING_KEY = "typing:" + CONVERSATION_ID;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        typingIndicatorService = new TypingIndicatorService(redisTemplate, messagingTemplate, metricsService);
    }

    @Test
    void startTyping_shouldAddUserIdToRedisSetWithTTL() {
        // Given
        when(setOperations.members(TYPING_KEY)).thenReturn(Set.of(USER_ID));

        // When
        typingIndicatorService.startTyping(CONVERSATION_ID, USER_ID, DISPLAY_NAME);

        // Then
        verify(setOperations).add(TYPING_KEY, USER_ID);
        verify(redisTemplate).expire(TYPING_KEY, 5L, TimeUnit.SECONDS);
        
        // Verify broadcast
        ArgumentCaptor<TypingIndicatorEvent> eventCaptor = ArgumentCaptor.forClass(TypingIndicatorEvent.class);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/conversation." + CONVERSATION_ID + "/typing"),
            eventCaptor.capture()
        );
        
        TypingIndicatorEvent event = eventCaptor.getValue();
        assertThat(event.getConversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(event.getTypingUserIds()).contains(USER_ID);
    }

    @Test
    void stopTyping_shouldRemoveUserIdFromRedisSet() {
        // Given
        when(setOperations.members(TYPING_KEY)).thenReturn(Set.of());

        // When
        typingIndicatorService.stopTyping(CONVERSATION_ID, USER_ID);

        // Then
        verify(setOperations).remove(TYPING_KEY, USER_ID);
        
        // Verify broadcast with empty set
        ArgumentCaptor<TypingIndicatorEvent> eventCaptor = ArgumentCaptor.forClass(TypingIndicatorEvent.class);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/conversation." + CONVERSATION_ID + "/typing"),
            eventCaptor.capture()
        );
        
        TypingIndicatorEvent event = eventCaptor.getValue();
        assertThat(event.getConversationId()).isEqualTo(CONVERSATION_ID);
        assertThat(event.getTypingUserIds()).isEmpty();
    }

    @Test
    void getTypingUsers_shouldReturnSetOfUserIds() {
        // Given
        Set<String> expectedUsers = Set.of("user-1", "user-2", "user-3");
        when(setOperations.members(TYPING_KEY)).thenReturn(expectedUsers);

        // When
        Set<String> actualUsers = typingIndicatorService.getTypingUsers(CONVERSATION_ID);

        // Then
        assertThat(actualUsers).isEqualTo(expectedUsers);
        verify(setOperations).members(TYPING_KEY);
    }

    @Test
    void getTypingUsers_shouldReturnEmptySetWhenRedisReturnsNull() {
        // Given
        when(setOperations.members(TYPING_KEY)).thenReturn(null);

        // When
        Set<String> actualUsers = typingIndicatorService.getTypingUsers(CONVERSATION_ID);

        // Then
        assertThat(actualUsers).isEmpty();
    }

    @Test
    void startTyping_shouldDegradeGracefullyWhenRedisUnavailable() {
        // Given
        when(setOperations.add(anyString(), anyString()))
            .thenThrow(new RedisConnectionFailureException("Redis connection failed"));

        // When/Then - should not throw exception
        assertThatCode(() -> 
            typingIndicatorService.startTyping(CONVERSATION_ID, USER_ID, DISPLAY_NAME)
        ).doesNotThrowAnyException();
        
        // Verify no broadcast when Redis fails
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(TypingIndicatorEvent.class));
    }

    @Test
    void stopTyping_shouldDegradeGracefullyWhenRedisUnavailable() {
        // Given
        when(setOperations.remove(anyString(), any()))
            .thenThrow(new RedisConnectionFailureException("Redis connection failed"));

        // When/Then - should not throw exception
        assertThatCode(() -> 
            typingIndicatorService.stopTyping(CONVERSATION_ID, USER_ID)
        ).doesNotThrowAnyException();
        
        // Verify no broadcast when Redis fails
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(TypingIndicatorEvent.class));
    }

    @Test
    void getTypingUsers_shouldReturnEmptySetWhenRedisUnavailable() {
        // Given
        when(setOperations.members(anyString()))
            .thenThrow(new RedisConnectionFailureException("Redis connection failed"));

        // When
        Set<String> actualUsers = typingIndicatorService.getTypingUsers(CONVERSATION_ID);

        // Then - should return empty set instead of throwing exception
        assertThat(actualUsers).isEmpty();
    }

    @Test
    void clearAllTypingForUser_shouldNotThrowException() {
        // When/Then - should not throw exception
        assertThatCode(() -> 
            typingIndicatorService.clearAllTypingForUser(USER_ID)
        ).doesNotThrowAnyException();
    }
}
