package com.theblood.springfood.chat.config;

import com.theblood.springfood.chat.service.dto.ChatMessageEvent;
import com.theblood.springfood.chat.service.dto.ReadReceiptEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for KafkaConsumerConfig.
 * Validates consumer configuration for broadcasting and batch persistence.
 * 
 * Requirements: 16.6, 16.7
 */
@SpringBootTest
@ActiveProfiles("test")
class KafkaConsumerConfigTest {

    @Autowired
    private ConsumerFactory<String, ChatMessageEvent> chatMessageBroadcastConsumerFactory;

    @Autowired
    private ConsumerFactory<String, ChatMessageEvent> chatMessagePersistenceConsumerFactory;

    @Autowired
    private ConsumerFactory<String, ReadReceiptEvent> readReceiptConsumerFactory;

    @Autowired
    private ConcurrentKafkaListenerContainerFactory<String, ChatMessageEvent> kafkaBroadcastListenerContainerFactory;

    @Autowired
    private ConcurrentKafkaListenerContainerFactory<String, ChatMessageEvent> kafkaBatchPersistenceListenerContainerFactory;

    @Autowired
    private ConcurrentKafkaListenerContainerFactory<String, ReadReceiptEvent> kafkaReadReceiptListenerContainerFactory;

    /**
     * Test that consumer factory is configured with correct deserializers.
     * Requirement 16.6
     */
    @Test
    void consumerFactoryConfiguredWithCorrectDeserializers() {
        // Given/When: Consumer factory is created
        var configs = chatMessageBroadcastConsumerFactory.getConfigurationProperties();

        // Then: Should have correct deserializer classes
        assertThat(configs.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG))
            .isEqualTo(org.apache.kafka.common.serialization.StringDeserializer.class);
        assertThat(configs.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG))
            .isEqualTo(org.springframework.kafka.support.serializer.JsonDeserializer.class);
    }

    /**
     * Test that auto-commit is disabled for manual offset management.
     * Requirement 16.6
     */
    @Test
    void autoCommitDisabled() {
        // Given/When: Consumer factory is created
        var configs = chatMessageBroadcastConsumerFactory.getConfigurationProperties();

        // Then: Auto-commit should be disabled
        assertThat(configs.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG))
            .isEqualTo(false);
    }

    /**
     * Test that max.poll.records is set to 100 for persistence consumer.
     * Requirement 16.7
     */
    @Test
    void persistenceConsumerMaxPollRecordsSetTo100() {
        // Given/When: Persistence consumer factory is created
        var configs = chatMessagePersistenceConsumerFactory.getConfigurationProperties();

        // Then: Max poll records should be 100
        assertThat(configs.get(ConsumerConfig.MAX_POLL_RECORDS_CONFIG))
            .isEqualTo(100);
    }

    /**
     * Test that broadcast listener factory is configured for single message processing.
     * Requirement 16.6
     */
    @Test
    void broadcastListenerFactoryConfiguredForSingleMessages() {
        // Given/When: Broadcast listener factory is created
        
        // Then: Should not be batch listener
        assertThat(kafkaBroadcastListenerContainerFactory.isBatchListener())
            .isFalse();
        
        // And: Should have manual acknowledgment mode
        assertThat(kafkaBroadcastListenerContainerFactory.getContainerProperties().getAckMode())
            .isEqualTo(ContainerProperties.AckMode.MANUAL);
    }

    /**
     * Test that batch persistence listener factory is configured for batch processing.
     * Requirement 16.7
     */
    @Test
    void batchPersistenceListenerFactoryConfiguredForBatches() {
        // Given/When: Batch persistence listener factory is created
        
        // Then: Should be batch listener
        assertThat(kafkaBatchPersistenceListenerContainerFactory.isBatchListener())
            .isTrue();
        
        // And: Should have manual immediate acknowledgment mode
        assertThat(kafkaBatchPersistenceListenerContainerFactory.getContainerProperties().getAckMode())
            .isEqualTo(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    }

    /**
     * Test that read receipt listener factory is configured correctly.
     * Requirement 16.6
     */
    @Test
    void readReceiptListenerFactoryConfiguredCorrectly() {
        // Given/When: Read receipt listener factory is created
        
        // Then: Should not be batch listener
        assertThat(kafkaReadReceiptListenerContainerFactory.isBatchListener())
            .isFalse();
        
        // And: Should have manual acknowledgment mode
        assertThat(kafkaReadReceiptListenerContainerFactory.getContainerProperties().getAckMode())
            .isEqualTo(ContainerProperties.AckMode.MANUAL);
    }

    /**
     * Test that bootstrap servers are configured correctly.
     */
    @Test
    void bootstrapServersConfigured() {
        // Given/When: Consumer factory is created
        var configs = chatMessageBroadcastConsumerFactory.getConfigurationProperties();

        // Then: Bootstrap servers should be set
        assertThat(configs.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG))
            .isEqualTo("localhost:9092");
    }

    /**
     * Test that trusted packages are configured for JsonDeserializer.
     * This is important for security to prevent deserialization attacks.
     */
    @Test
    void trustedPackagesConfigured() {
        // Given/When: Consumer factory is created
        var configs = chatMessageBroadcastConsumerFactory.getConfigurationProperties();

        // Then: Trusted packages should include our DTO package
        assertThat(configs.get(org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES))
            .isEqualTo("com.theblood.springfood.chat.service.dto");
    }

    /**
     * Test that all required consumer factories are created.
     */
    @Test
    void allConsumerFactoriesCreated() {
        // Then: All consumer factories should be non-null
        assertThat(chatMessageBroadcastConsumerFactory).isNotNull();
        assertThat(chatMessagePersistenceConsumerFactory).isNotNull();
        assertThat(readReceiptConsumerFactory).isNotNull();
    }

    /**
     * Test that all required listener container factories are created.
     */
    @Test
    void allListenerContainerFactoriesCreated() {
        // Then: All listener container factories should be non-null
        assertThat(kafkaBroadcastListenerContainerFactory).isNotNull();
        assertThat(kafkaBatchPersistenceListenerContainerFactory).isNotNull();
        assertThat(kafkaReadReceiptListenerContainerFactory).isNotNull();
    }
}
