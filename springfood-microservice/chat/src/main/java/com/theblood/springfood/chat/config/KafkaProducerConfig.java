package com.theblood.springfood.chat.config;

import com.theblood.springfood.chat.service.dto.ChatMessageEvent;
import com.theblood.springfood.chat.service.dto.ReadReceiptEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Producer Configuration for Chat Service.
 * Configures producers for publishing chat messages and read receipts to Kafka topics.
 * 
 * Requirements: 16.3, 16.4, 16.5
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Common producer configuration properties.
     * 
     * Configuration details:
     * - acks=1: Leader acknowledgment for balance between reliability and performance
     * - enable.idempotence=true: Prevents duplicate messages
     * - max.in.flight.requests.per.connection=5: Maintains ordering with idempotence
     */
    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Requirement 16.3: acks=all required when enable.idempotence=true
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        
        // Requirement 16.4: Enable idempotence to prevent duplicate messages
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // Requirement 16.5: Set max in-flight requests to maintain ordering with idempotence
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        
        return props;
    }

    /**
     * Producer factory for ChatMessageEvent.
     * Used to publish messages to "chat-messages" topic.
     */
    @Bean
    public ProducerFactory<String, ChatMessageEvent> chatMessageProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * KafkaTemplate for publishing ChatMessageEvent.
     * Injected into controllers and services to send chat messages to Kafka.
     */
    @Bean
    public KafkaTemplate<String, ChatMessageEvent> chatMessageKafkaTemplate() {
        return new KafkaTemplate<>(chatMessageProducerFactory());
    }

    /**
     * Producer factory for ReadReceiptEvent.
     * Used to publish read receipts to "chat-read-receipts" topic.
     */
    @Bean
    public ProducerFactory<String, ReadReceiptEvent> readReceiptProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * KafkaTemplate for publishing ReadReceiptEvent.
     * Injected into controllers and services to send read receipts to Kafka.
     */
    @Bean
    public KafkaTemplate<String, ReadReceiptEvent> readReceiptKafkaTemplate() {
        return new KafkaTemplate<>(readReceiptProducerFactory());
    }
}
