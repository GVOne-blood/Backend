package com.theblood.springfood.chat.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Topic Configuration for Chat Service.
 * Creates Kafka topics for chat messages and read receipts.
 * 
 * Requirements: 16.1, 16.2
 */
@Configuration
public class ChatKafkaTopicConfig {

    /**
     * Topic name for chat messages.
     * Used for distributing messages across service instances.
     */
    public static final String CHAT_MESSAGES_TOPIC = "chat-messages";

    /**
     * Topic name for read receipts.
     * Used for processing read status updates.
     */
    public static final String CHAT_READ_RECEIPTS_TOPIC = "chat-read-receipts";

    /**
     * Create "chat-messages" topic.
     * 
     * Configuration:
     * - 3 partitions: Allows parallel processing across conversations
     * - Replication factor 1: Single broker setup (as per infrastructure constraints)
     * 
     * Requirement 16.1: Create chat-messages topic with 3 partitions
     */
    @Bean
    public NewTopic chatMessagesTopic() {
        return TopicBuilder.name(CHAT_MESSAGES_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    /**
     * Create "chat-read-receipts" topic.
     * 
     * Configuration:
     * - 3 partitions: Allows parallel processing of read receipts
     * - Replication factor 1: Single broker setup (as per infrastructure constraints)
     * 
     * Requirement 16.2: Create chat-read-receipts topic with 3 partitions
     */
    @Bean
    public NewTopic chatReadReceiptsTopic() {
        return TopicBuilder.name(CHAT_READ_RECEIPTS_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
