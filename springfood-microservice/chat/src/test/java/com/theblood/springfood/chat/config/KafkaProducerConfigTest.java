package com.theblood.springfood.chat.config;

import com.theblood.springfood.chat.service.dto.ChatMessageEvent;
import com.theblood.springfood.chat.service.dto.ReadReceiptEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Kafka Producer Configuration.
 * 
 * Tests verify:
 * - Producer factory configured with correct serializers
 * - acks=1 configuration (Requirement 16.3)
 * - Idempotence enabled (Requirement 16.4)
 * - KafkaTemplates are properly created
 */
@SpringBootTest
@ActiveProfiles("test")
class KafkaProducerConfigTest {

    @Autowired
    private ProducerFactory<String, ChatMessageEvent> chatMessageProducerFactory;

    @Autowired
    private ProducerFactory<String, ReadReceiptEvent> readReceiptProducerFactory;

    @Autowired
    private KafkaTemplate<String, ChatMessageEvent> chatMessageKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, ReadReceiptEvent> readReceiptKafkaTemplate;

    /**
     * Test that producer factory is configured with correct serializers.
     * Requirement 16.3: Verify JsonSerializer for values and StringSerializer for keys
     */
    @Test
    void testProducerFactoryConfiguredWithCorrectSerializers() {
        // Given: Chat message producer factory
        var config = chatMessageProducerFactory.getConfigurationProperties();

        // Then: Should use StringSerializer for keys
        assertThat(config.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG))
            .isEqualTo(StringSerializer.class);

        // And: Should use JsonSerializer for values
        assertThat(config.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG))
            .isEqualTo(JsonSerializer.class);
    }

    /**
     * Test that acks=1 is configured for leader acknowledgment.
     * Requirement 16.3: Balance between reliability and performance
     */
    @Test
    void testAcksConfigurationSetToOne() {
        // Given: Chat message producer factory
        var config = chatMessageProducerFactory.getConfigurationProperties();

        // Then: acks should be set to "1" (leader acknowledgment)
        assertThat(config.get(ProducerConfig.ACKS_CONFIG))
            .isEqualTo("1");
    }

    /**
     * Test that idempotence is enabled to prevent duplicate messages.
     * Requirement 16.4: Enable idempotence
     */
    @Test
    void testIdempotenceEnabled() {
        // Given: Chat message producer factory
        var config = chatMessageProducerFactory.getConfigurationProperties();

        // Then: Idempotence should be enabled
        assertThat(config.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG))
            .isEqualTo(true);
    }

    /**
     * Test that max in-flight requests is set to 5 for ordering with idempotence.
     * Requirement 16.5: Maintain ordering with idempotence
     */
    @Test
    void testMaxInFlightRequestsSetToFive() {
        // Given: Chat message producer factory
        var config = chatMessageProducerFactory.getConfigurationProperties();

        // Then: Max in-flight requests should be 5
        assertThat(config.get(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION))
            .isEqualTo(5);
    }

    /**
     * Test that ChatMessageEvent KafkaTemplate is properly created.
     */
    @Test
    void testChatMessageKafkaTemplateCreated() {
        // Then: KafkaTemplate should not be null
        assertThat(chatMessageKafkaTemplate).isNotNull();

        // And: Should use the correct producer factory
        assertThat(chatMessageKafkaTemplate.getProducerFactory())
            .isEqualTo(chatMessageProducerFactory);
    }

    /**
     * Test that ReadReceiptEvent KafkaTemplate is properly created.
     */
    @Test
    void testReadReceiptKafkaTemplateCreated() {
        // Then: KafkaTemplate should not be null
        assertThat(readReceiptKafkaTemplate).isNotNull();

        // And: Should use the correct producer factory
        assertThat(readReceiptKafkaTemplate.getProducerFactory())
            .isEqualTo(readReceiptProducerFactory);
    }

    /**
     * Test that both producer factories have the same configuration.
     */
    @Test
    void testBothProducerFactoriesHaveSameConfiguration() {
        // Given: Both producer factories
        var chatConfig = chatMessageProducerFactory.getConfigurationProperties();
        var receiptConfig = readReceiptProducerFactory.getConfigurationProperties();

        // Then: Both should have same acks configuration
        assertThat(chatConfig.get(ProducerConfig.ACKS_CONFIG))
            .isEqualTo(receiptConfig.get(ProducerConfig.ACKS_CONFIG));

        // And: Both should have same idempotence configuration
        assertThat(chatConfig.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG))
            .isEqualTo(receiptConfig.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));

        // And: Both should have same max in-flight requests
        assertThat(chatConfig.get(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION))
            .isEqualTo(receiptConfig.get(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION));
    }
}
