package com.theblood.springfood.chat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuration for Google AI Gemini using direct REST API.
 * This bypasses Vertex AI and uses the simpler Google AI API with API key.
 */
@Configuration
public class AIConfig {

    private static final Logger log = LoggerFactory.getLogger(AIConfig.class);

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.ai.google.ai.gemini.api-key")
    public ChatModel googleAiChatModel(
        @Value("${spring.ai.google.ai.gemini.api-key}") String apiKey,
        @Value("${spring.ai.google.ai.gemini.model:gemini-2.5-flash-lite}") String model
    ) {
        log.info("Creating Google AI ChatModel with model: {}", model);
        return new GoogleAiRestChatModel(apiKey, model);
    }

    /**
     * Provide ChatClient.Builder bean for dependency injection.
     * This is normally provided by Spring AI autoconfiguration, but we need to create it manually
     * since we're using a custom ChatModel.
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }

    /**
     * Provide EmbeddingModel bean for PgVector store.
     * Uses Google AI text-embedding-004 model.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.ai.google.ai.gemini.api-key")
    public EmbeddingModel googleAiEmbeddingModel(
        @Value("${spring.ai.google.ai.gemini.api-key}") String apiKey
    ) {
        log.info("Creating Google AI EmbeddingModel with text-embedding-004");
        return new GoogleAiRestEmbeddingModel(apiKey);
    }

    /**
     * Simple ChatModel implementation using Google AI REST API
     */
    private static class GoogleAiRestChatModel implements ChatModel {
        
        private static final String API_URL_TEMPLATE = 
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
        
        private final String apiKey;
        private final String model;
        private final RestTemplate restTemplate;

        public GoogleAiRestChatModel(String apiKey, String model) {
            this.apiKey = apiKey;
            this.model = model;
            this.restTemplate = new RestTemplate();
        }

        @Override
        public ChatResponse call(Prompt prompt) {
            String url = String.format(API_URL_TEMPLATE, model, apiKey);
            
            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = prompt.getInstructions().stream()
                .map(this::messageToContent)
                .collect(Collectors.toList());
            requestBody.put("contents", contents);
            
            // Make API call
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
                
                // Parse response
                String text = extractTextFromResponse(response);
                Generation generation = new Generation(new AssistantMessage(text));
                return new ChatResponse(List.of(generation));
                
            } catch (Exception e) {
                log.error("Error calling Google AI API", e);
                throw new RuntimeException("Failed to call Google AI API: " + e.getMessage(), e);
            }
        }

        @Override
        public Flux<ChatResponse> stream(Prompt prompt) {
            // For simplicity, just call the non-streaming version
            // TODO: Implement proper streaming using SSE
            return Flux.just(call(prompt));
        }

        @Override
        public ChatOptions getDefaultOptions() {
            return null;
        }

        private Map<String, Object> messageToContent(Message message) {
            Map<String, Object> content = new HashMap<>();
            String role = message instanceof UserMessage ? "user" : "model";
            content.put("role", role);
            
            Map<String, String> part = new HashMap<>();
            part.put("text", message.getText());
            content.put("parts", List.of(part));
            
            return content;
        }

        @SuppressWarnings("unchecked")
        private String extractTextFromResponse(Map<String, Object> response) {
            try {
                List<Map<String, Object>> candidates = 
                    (List<Map<String, Object>>) response.get("candidates");
                Map<String, Object> content = 
                    (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = 
                    (List<Map<String, Object>>) content.get("parts");
                return (String) parts.get(0).get("text");
            } catch (Exception e) {
                log.error("Error parsing Google AI response: {}", response, e);
                return "Error: Unable to parse AI response";
            }
        }
    }

    /**
     * Simple EmbeddingModel implementation using Google AI REST API
     */
    private static class GoogleAiRestEmbeddingModel implements EmbeddingModel {
        
        private static final String EMBEDDING_API_URL_TEMPLATE = 
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent?key=%s";
        
        private final String apiKey;
        private final RestTemplate restTemplate;

        public GoogleAiRestEmbeddingModel(String apiKey) {
            this.apiKey = apiKey;
            this.restTemplate = new RestTemplate();
        }

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            String url = String.format(EMBEDDING_API_URL_TEMPLATE, apiKey);
            
            // Build request body for each text
            List<org.springframework.ai.embedding.Embedding> embeddings = request.getInstructions().stream()
                .map(text -> {
                    // Format đúng theo Google AI v1beta:
                    // { "content": { "parts": [{ "text": "..." }] }, "output_dimensionality": 768 }
                    Map<String, Object> requestBody = new HashMap<>();
                    Map<String, Object> content = new HashMap<>();
                    Map<String, String> part = new HashMap<>();
                    part.put("text", text);
                    content.put("parts", List.of(part));
                    requestBody.put("content", content);
                    // gemini-embedding-001 default ra 3072 dim, ép về 768 để khớp với DB schema
                    requestBody.put("output_dimensionality", 768);
                    
                    // Make API call
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(requestBody, headers);
                    
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> response = restTemplate.postForObject(url, httpRequest, Map.class);
                        
                        // Parse response
                        @SuppressWarnings("unchecked")
                        Map<String, Object> embedding = (Map<String, Object>) response.get("embedding");
                        @SuppressWarnings("unchecked")
                        List<Double> values = (List<Double>) embedding.get("values");
                        
                        // Convert to float array
                        float[] floatArray = new float[values.size()];
                        for (int i = 0; i < values.size(); i++) {
                            floatArray[i] = values.get(i).floatValue();
                        }
                        
                        return new org.springframework.ai.embedding.Embedding(floatArray, 0);
                        
                    } catch (Exception e) {
                        log.error("Error calling Google AI Embedding API", e);
                        throw new RuntimeException("Failed to call Google AI Embedding API: " + e.getMessage(), e);
                    }
                })
                .collect(Collectors.toList());
            
            return new EmbeddingResponse(embeddings);
        }

        @Override
        public float[] embed(org.springframework.ai.document.Document document) {
            // Embed document text
            EmbeddingRequest request = new EmbeddingRequest(List.of(document.getText()), null);
            EmbeddingResponse response = call(request);
            return response.getResults().get(0).getOutput();
        }

        @Override
        public int dimensions() {
            return 768; // text-embedding-004 produces 768-dimensional embeddings
        }
    }
}
