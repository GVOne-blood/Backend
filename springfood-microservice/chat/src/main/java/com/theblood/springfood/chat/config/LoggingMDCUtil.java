package com.theblood.springfood.chat.config;

import org.slf4j.MDC;

/**
 * Utility class for managing MDC (Mapped Diagnostic Context) for structured logging.
 * 
 * MDC allows adding contextual information (correlation IDs) to log messages,
 * making it easier to trace requests across the system.
 * 
 * Key correlation IDs:
 * - conversationId: Identifies the conversation context
 * - messageId: Identifies the specific message
 * - userId: Identifies the user performing the action
 * - sessionId: Identifies the WebSocket session
 * 
 * Usage:
 * <pre>
 * LoggingMDCUtil.setConversationId(conversationId);
 * LoggingMDCUtil.setMessageId(messageId);
 * try {
 *     // Your code here - all logs will include MDC context
 * } finally {
 *     LoggingMDCUtil.clear();
 * }
 * </pre>
 */
public class LoggingMDCUtil {

    // MDC key constants
    public static final String CONVERSATION_ID = "conversationId";
    public static final String MESSAGE_ID = "messageId";
    public static final String USER_ID = "userId";
    public static final String SESSION_ID = "sessionId";
    public static final String CLIENT_MESSAGE_ID = "clientMessageId";
    public static final String BATCH_SIZE = "batchSize";
    public static final String RETRY_ATTEMPT = "retryAttempt";

    private LoggingMDCUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Set conversation ID in MDC context.
     * 
     * @param conversationId The conversation ID
     */
    public static void setConversationId(String conversationId) {
        if (conversationId != null) {
            MDC.put(CONVERSATION_ID, conversationId);
        }
    }

    /**
     * Set message ID in MDC context.
     * 
     * @param messageId The message ID
     */
    public static void setMessageId(String messageId) {
        if (messageId != null) {
            MDC.put(MESSAGE_ID, messageId);
        }
    }

    /**
     * Set user ID in MDC context.
     * 
     * @param userId The user ID
     */
    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put(USER_ID, userId);
        }
    }

    /**
     * Set session ID in MDC context.
     * 
     * @param sessionId The WebSocket session ID
     */
    public static void setSessionId(String sessionId) {
        if (sessionId != null) {
            MDC.put(SESSION_ID, sessionId);
        }
    }

    /**
     * Set client message ID in MDC context.
     * 
     * @param clientMessageId The client-generated message ID
     */
    public static void setClientMessageId(String clientMessageId) {
        if (clientMessageId != null) {
            MDC.put(CLIENT_MESSAGE_ID, clientMessageId);
        }
    }

    /**
     * Set batch size in MDC context (for batch processing).
     * 
     * @param batchSize The batch size
     */
    public static void setBatchSize(int batchSize) {
        MDC.put(BATCH_SIZE, String.valueOf(batchSize));
    }

    /**
     * Set retry attempt number in MDC context.
     * 
     * @param attempt The retry attempt number
     */
    public static void setRetryAttempt(int attempt) {
        MDC.put(RETRY_ATTEMPT, String.valueOf(attempt));
    }

    /**
     * Set all message-related context in MDC.
     * 
     * @param conversationId The conversation ID
     * @param messageId The message ID
     * @param userId The user ID
     */
    public static void setMessageContext(String conversationId, String messageId, String userId) {
        setConversationId(conversationId);
        setMessageId(messageId);
        setUserId(userId);
    }

    /**
     * Clear all MDC context.
     * Should be called in finally blocks to prevent memory leaks.
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * Clear specific MDC key.
     * 
     * @param key The MDC key to clear
     */
    public static void remove(String key) {
        MDC.remove(key);
    }
}
