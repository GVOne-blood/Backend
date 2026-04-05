package com.theblood.springfood.chat.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class GeminiAIService implements AIAssistantService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIService.class);

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public GeminiAIService(
        ChatClient.Builder chatClientBuilder,
        ChatMemory chatMemory,
        VectorStore vectorStore,
        ApplicationContext applicationContext,
        ResourceLoader resourceLoader) throws IOException {

        this.chatMemory = chatMemory;

        Resource resource = resourceLoader.getResource("classpath:ai/gemini_system_prompt.txt");
        String systemPrompt = resource.getContentAsString(StandardCharsets.UTF_8);
        log.info("Loaded AI system prompt: {} characters", systemPrompt.length());

        // Spring AI 1.0.0 GA auto-discovers Function beans as tools
        this.chatClient = chatClientBuilder
            .defaultSystem(systemPrompt)
            .defaultAdvisors(
                // Chat memory — load/save history theo conversationId vào JDBC
                MessageChatMemoryAdvisor.builder(chatMemory).build()
                // TODO: Add RAG advisor when implementing vector search
            )
            .build();
    }

    @Override
    public String chat(String conversationId, String userId, String message) {
        log.debug("AI chat - conversationId: {}, userId: {}", conversationId, userId);
        long startTime = System.currentTimeMillis();

        String response = chatClient.prompt()
            .user(message)
            .advisors(advisor -> advisor
                .param("conversationId", conversationId)
            )
            .call()
            .content();

        log.debug("AI response in {}ms", System.currentTimeMillis() - startTime);
        return response;
    }

    @Override
    public Flux<String> chatStream(String conversationId, String userId, String message) {
        log.debug("AI stream - conversationId: {}, userId: {}", conversationId, userId);

        return chatClient.prompt()
            .user(message)
            .advisors(advisor -> advisor
                .param("conversationId", conversationId)
            )
            .stream()
            .content();
    }

    @Override
    public void clearHistory(String conversationId) {
        chatMemory.clear(conversationId);
        log.info("Cleared AI history for conversationId: {}", conversationId);
    }
}
