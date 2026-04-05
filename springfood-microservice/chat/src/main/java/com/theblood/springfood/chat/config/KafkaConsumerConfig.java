package com.theblood.springfood.chat.config;

import com.theblood.springfood.chat.service.dto.ChatMessageEvent;
import com.theblood.springfood.chat.service.dto.ReadReceiptEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.retry.annotation.EnableRetry;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Configuration for Chat Service.
 * Configures consumers for broadcasting messages and batch persistence.
 * 
 * Two consumer patterns:
 * 1. Broadcasting: Each instance has unique group ID to receive all messages
 * 2. Persistence: Shared group ID for load-balanced batch processing
 * 
 * Requirements: 16.6, 16.7
 */
@Configuration
@EnableRetry
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Common consumer configuration properties.
     * 
     * Configuration details:
     * - enable.auto.commit=false: Manual commit for at-least-once delivery
     * - JsonDeserializer with trusted packages for security
     */
    private Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Requirement 16.6: Disable auto-commit for manual offset management
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        // Configure JsonDeserializer to trust our event packages
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.theblood.springfood.chat.service.dto");
        
        return props;
    }

    /**
     * Consumer factory for ChatMessageEvent (Broadcasting).
     * Each instance will use a unique group ID to receive all messages.
     */
    @Bean
    public ConsumerFactory<String, ChatMessageEvent> chatMessageBroadcastConsumerFactory() {
        Map<String, Object> props = consumerConfigs();
        
        return new DefaultKafkaConsumerFactory<>(
            props,
            new StringDeserializer(),
            new JsonDeserializer<>(ChatMessageEvent.class, false)
        );
    }

    /**
     * Listener container factory for broadcasting messages to WebSocket clients.
     * 
     * Configuration:
     * - Single message processing (not batch)
     * - Manual acknowledgment after successful WebSocket delivery
     * - Each instance has unique consumer group ID (set in consumer class)
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessageEvent> 
            kafkaBroadcastListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessageEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(chatMessageBroadcastConsumerFactory());
        
        // Single message processing for real-time broadcasting
        factory.setBatchListener(false);
        
        // Manual acknowledgment for at-least-once delivery
        factory.getContainerProperties().setAckMode(
            org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL
        );
        
        return factory;
    }

    /**
     * Consumer factory for ChatMessageEvent (Persistence).
     * Shared group ID for load-balanced processing across instances.
     */
    @Bean
    public ConsumerFactory<String, ChatMessageEvent> chatMessagePersistenceConsumerFactory() {
        Map<String, Object> props = consumerConfigs();
        
        // Requirement 16.7: Set max.poll.records=100 for batch persistence
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        
        return new DefaultKafkaConsumerFactory<>(
            props,
            new StringDeserializer(),
            new JsonDeserializer<>(ChatMessageEvent.class, false)
        );
    }

    /**
     * Listener container factory for batch persistence to database.
     * 
     * Configuration:
     * - Batch processing enabled (max 100 messages or 5s timeout)
     * - Manual acknowledgment after successful database insert
     * - Shared consumer group "chat-persistence-group" for load balancing
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessageEvent> 
            kafkaBatchPersistenceListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessageEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(chatMessagePersistenceConsumerFactory());
        
        // Enable batch processing for efficient database insertion
        factory.setBatchListener(true);
        
        // Manual acknowledgment for at-least-once delivery
        factory.getContainerProperties().setAckMode(
            org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE
        );
        
        return factory;
    }

    /**
     * Consumer factory for ReadReceiptEvent.
     * Used for processing read receipts and updating unread counts.
     */
    @Bean
    public ConsumerFactory<String, ReadReceiptEvent> readReceiptConsumerFactory() {
        Map<String, Object> props = consumerConfigs();
        
        return new DefaultKafkaConsumerFactory<>(
            props,
            new StringDeserializer(),
            new JsonDeserializer<>(ReadReceiptEvent.class, false)
        );
    }

    /**
     * Listener container factory for read receipt processing.
     * 
     * Configuration:
     * - Single message processing
     * - Manual acknowledgment after successful processing
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReadReceiptEvent> 
            kafkaReadReceiptListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ReadReceiptEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(readReceiptConsumerFactory());
        
        // Single message processing
        factory.setBatchListener(false);
        
        // Manual acknowledgment
        factory.getContainerProperties().setAckMode(
            org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL
        );
        
        return factory;
    }
}
