package com.theblood.springfood.chat.service.ai;

import reactor.core.publisher.Flux;

/**
 * AI Assistant Service Interface
 * 
 * Provides AI-powered chat assistance using Gemini model.
 */
public interface AIAssistantService {

    /**
     * Send a message to AI assistant and get response
     * 
     * @param conversationId Conversation ID for context tracking
     * @param userId User ID making the request
     * @param message User message
     * @return AI response
     */
    String chat(String conversationId, String userId, String message);

    /**
     * Stream AI response for real-time chat experience
     * 
     * @param conversationId Conversation ID for context tracking
     * @param userId User ID making the request
     * @param message User message
     * @return Flux of AI response chunks
     */
    Flux<String> chatStream(String conversationId, String userId, String message);

    /**
     * Clear conversation history for a specific conversation
     * 
     * @param conversationId Conversation ID to clear
     */
    void clearHistory(String conversationId);
}
