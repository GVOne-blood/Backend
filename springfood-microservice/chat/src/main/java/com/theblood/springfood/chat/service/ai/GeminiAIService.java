package com.theblood.springfood.chat.service.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.chat.service.dto.AIMessageResponse;
import com.theblood.springfood.chat.service.dto.ProductCard;
import com.theblood.springfood.chat.service.dto.ShopCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeminiAIService implements AIAssistantService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIService.class);

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final VectorStore vectorStore;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;

    public GeminiAIService(
        ChatClient.Builder chatClientBuilder,
        ChatMemory chatMemory,
        VectorStore vectorStore,
        ApplicationContext applicationContext,
        ResourceLoader resourceLoader,
        RestTemplate restTemplate,
        ObjectMapper objectMapper) throws IOException {

        this.chatMemory = chatMemory;
        this.vectorStore = vectorStore;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        Resource resource = resourceLoader.getResource("classpath:ai/gemini_system_prompt.txt");
        this.systemPrompt = resource.getContentAsString(StandardCharsets.UTF_8);
        log.info("Loaded AI system prompt: {} characters", systemPrompt.length());

        this.chatClient = chatClientBuilder
            .defaultSystem(systemPrompt)
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(chatMemory).build()
            )
            .build();
    }

    @Override
    public AIMessageResponse chat(String conversationId, String userId, String message) {
        log.debug("AI chat - conversationId: {}, userId: {}", conversationId, userId);
        long startTime = System.currentTimeMillis();

        // 1. Search vector store for relevant documents
        List<Document> documents = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(message)
                .topK(5)
                .similarityThreshold(0.5)
                .build()
        );

        // 2. Build context + extract product/shop IDs
        StringBuilder context = new StringBuilder();
        Set<String> productIds = new LinkedHashSet<>();
        Set<String> shopIds = new LinkedHashSet<>();

        if (documents != null && !documents.isEmpty()) {
            context.append("Thông tin từ hệ thống:\n\n");
            for (int i = 0; i < documents.size(); i++) {
                Document doc = documents.get(i);
                Map<String, Object> meta = doc.getMetadata();

                context.append("[").append(i + 1).append("] ");
                context.append(doc.getText()).append("\n\n");

                String sourceType = (String) meta.get("source_type");
                String sourceId = (String) meta.get("source_id");
                if ("PRODUCT".equals(sourceType) && sourceId != null) {
                    productIds.add(sourceId);
                } else if ("SHOP".equals(sourceType) && sourceId != null) {
                    shopIds.add(sourceId);
                }
            }
        }

        // 3. Build enriched prompt with context
        String userPrompt = context.length() > 0
            ? context + "Câu hỏi của khách hàng:\n" + message
            : message;

        // 4. Call Gemini
        String response = chatClient.prompt()
            .user(userPrompt)
            .advisors(advisor -> advisor
                .param("conversationId", conversationId)
            )
            .call()
            .content();

        log.debug("AI response in {}ms", System.currentTimeMillis() - startTime);

        // 5. Fetch product details
        List<ProductCard> productCards = productIds.isEmpty() ? null : fetchProductCards(productIds);
        List<ShopCard> shopCards = shopIds.isEmpty() ? null : fetchShopCards(shopIds, documents);

        return AIMessageResponse.of(conversationId, message, response, productCards, shopCards);
    }

    @Override
    public Flux<String> chatStream(String conversationId, String userId, String message) {
        log.debug("AI stream - conversationId: {}, userId: {}", conversationId, userId);

        // Search vector store for context (optional - không block nếu fail)
        StringBuilder context = new StringBuilder();
        try {
            List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                    .query(message)
                    .topK(3)
                    .similarityThreshold(0.5)
                    .build()
            );

            if (documents != null && !documents.isEmpty()) {
                context.append("Thông tin từ hệ thống:\n\n");
                for (int i = 0; i < documents.size(); i++) {
                    context.append("[").append(i + 1).append("] ");
                    context.append(documents.get(i).getText()).append("\n\n");
                }
                log.debug("Found {} relevant documents from vector store", documents.size());
            }
        } catch (Exception e) {
            log.warn("Vector store search failed, continuing without RAG context: {}", e.getMessage());
        }

        String userPrompt = context.length() > 0
            ? context + "Câu hỏi của khách hàng:\n" + message
            : message;

        log.info("Calling Gemini with prompt length: {} chars", userPrompt.length());

        return chatClient.prompt()
            .user(userPrompt)
            .advisors(advisor -> advisor
                .param("conversationId", conversationId)
            )
            .stream()
            .content()
            .doOnError(err -> log.error("Gemini stream error: {}", err.getMessage(), err))
            .doOnComplete(() -> log.info("Gemini stream completed for conversation: {}", conversationId));
    }

    @Override
    public void clearHistory(String conversationId) {
        chatMemory.clear(conversationId);
        log.info("Cleared AI history for conversationId: {}", conversationId);
    }

    /**
     * Search vector store và fetch metadata sản phẩm/shop liên quan đến câu hỏi.
     * Tách riêng để controller streaming có thể gọi sau khi stream xong text →
     * push cards qua kênh /user/queue/ai-assistant/cards.
     */
    @Override
    public CardSearchResult searchCards(String message) {
        try {
            List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                    .query(message)
                    .topK(5)
                    .similarityThreshold(0.5)
                    .build()
            );
            if (documents == null || documents.isEmpty()) {
                return new CardSearchResult(null, null);
            }
            Set<String> productIds = new LinkedHashSet<>();
            Set<String> shopIds = new LinkedHashSet<>();
            for (Document doc : documents) {
                Map<String, Object> meta = doc.getMetadata();
                String sourceType = (String) meta.get("source_type");
                String sourceId = (String) meta.get("source_id");
                if (sourceId == null) continue;
                if ("PRODUCT".equals(sourceType)) productIds.add(sourceId);
                else if ("SHOP".equals(sourceType)) shopIds.add(sourceId);
            }
            List<ProductCard> productCards = productIds.isEmpty() ? null : fetchProductCards(productIds);
            List<ShopCard> shopCards = shopIds.isEmpty() ? null : fetchShopCards(shopIds, documents);
            return new CardSearchResult(productCards, shopCards);
        } catch (Exception e) {
            log.warn("searchCards failed for message='{}': {}", message, e.getMessage());
            return new CardSearchResult(null, null);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<ProductCard> fetchProductCards(Set<String> productIds) {
        List<ProductCard> cards = new ArrayList<>();
        for (String id : productIds) {
            try {
                ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    "http://product-service/products/" + id,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
                );
                Map<String, Object> body = resp.getBody();
                if (body == null) continue;
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                if (data == null) continue;

                ProductCard card = new ProductCard();
                card.setId(data.get("id") != null ? data.get("id").toString() : id);
                card.setName((String) data.get("name"));
                if (data.get("price") != null) {
                    card.setPrice(new BigDecimal(data.get("price").toString()));
                }
                card.setDescription((String) data.get("description"));
                if (data.get("averageRating") != null) {
                    card.setAverageRating(((Number) data.get("averageRating")).doubleValue());
                }
                // Parse images JSON string to get first image
                String imagesJson = (String) data.get("images");
                if (imagesJson != null && !imagesJson.isBlank()) {
                    try {
                        List<String> urls = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
                        if (!urls.isEmpty()) card.setImage(urls.get(0));
                    } catch (Exception e) {
                        card.setImage(imagesJson.replaceAll("[\\[\\]\"]", ""));
                    }
                }
                cards.add(card);
            } catch (Exception e) {
                log.warn("Failed to fetch product {}: {}", id, e.getMessage());
            }
        }
        return cards.isEmpty() ? null : cards;
    }

    private List<ShopCard> fetchShopCards(Set<String> shopIds, List<Document> documents) {
        // Build shop info from RAG document metadata (no dedicated shop detail endpoint)
        Map<String, ShopCard> shopMap = new LinkedHashMap<>();
        for (String id : shopIds) {
            shopMap.put(id, new ShopCard(id, null, null, null, null, null));
        }
        // Enrich from document metadata
        if (documents != null) {
            for (Document doc : documents) {
                Map<String, Object> meta = doc.getMetadata();
                if ("SHOP".equals(meta.get("source_type"))) {
                    String sid = (String) meta.get("source_id");
                    ShopCard card = shopMap.get(sid);
                    if (card != null) {
                        if (card.getName() == null) card.setName((String) meta.get("title"));
                        if (meta.get("total_products") != null)
                            card.setTotalProducts(((Number) meta.get("total_products")).intValue());
                        if (meta.get("total_sold") != null)
                            card.setTotalSold(((Number) meta.get("total_sold")).intValue());
                    }
                }
            }
        }
        List<ShopCard> result = shopMap.values().stream()
            .filter(s -> s.getName() != null)
            .collect(Collectors.toList());
        return result.isEmpty() ? null : result;
    }
}
