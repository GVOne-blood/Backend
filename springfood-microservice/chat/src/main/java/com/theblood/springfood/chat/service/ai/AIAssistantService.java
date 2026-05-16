package com.theblood.springfood.chat.service.ai;

import com.theblood.springfood.chat.service.dto.AIMessageResponse;
import com.theblood.springfood.chat.service.dto.ProductCard;
import com.theblood.springfood.chat.service.dto.ShopCard;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI Assistant Service Interface
 * 
 * Provides AI-powered chat assistance using Gemini model + RAG.
 */
public interface AIAssistantService {

    /**
     * Send a message to AI assistant and get enriched response
     * with product/shop cards from knowledge base
     */
    AIMessageResponse chat(String conversationId, String userId, String message);

    /**
     * Stream AI response for real-time chat experience (text-only)
     */
    Flux<String> chatStream(String conversationId, String userId, String message);

    /**
     * Search vector store and fetch product/shop cards relevant to the message.
     * Dùng kèm streaming chat: stream xong text rồi push cards qua kênh riêng để UI render thẻ.
     */
    CardSearchResult searchCards(String message);

    /**
     * Clear conversation history
     */
    void clearHistory(String conversationId);

    /**
     * Container chứa product + shop cards từ RAG search.
     */
    record CardSearchResult(List<ProductCard> products, List<ShopCard> shops) {
        public boolean isEmpty() {
            return (products == null || products.isEmpty())
                && (shops == null || shops.isEmpty());
        }
    }
}
