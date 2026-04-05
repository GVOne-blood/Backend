package com.theblood.springfood.chat.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Kafka Topic Configuration.
 * 
 * Tests verify:
 * - Topics created with correct partition count (Requirement 16.1, 16.2)
 * - Topics created with correct replication factor
 * - Topic names are correct
 */
@SpringBootTest
@ActiveProfiles("test")
class KafkaTopicConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Test that chat-messages topic is created with correct configuration.
     * Requirement 16.1: Create chat-messages topic with 3 partitions, replication factor 1
     */
    @Test
    void testChatMessagesTopicCreatedWithCorrectConfiguration() {
        // Given: Chat messages topic bean
        NewTopic chatMessagesTopic = applicationContext.getBean("chatMessagesTopic", NewTopic.class);

        // Then: Topic name should be "chat-messages"
        assertThat(chatMessagesTopic.name()).isEqualTo(ChatKafkaTopicConfig.CHAT_MESSAGES_TOPIC);
        assertThat(chatMessagesTopic.name()).isEqualTo("chat-messages");

        // And: Should have 3 partitions
        assertThat(chatMessagesTopic.numPartitions()).isEqualTo(3);

        // And: Should have replication factor 1 (single broker setup)
        assertThat(chatMessagesTopic.replicationFactor()).isEqualTo((short) 1);
    }

    /**
     * Test that chat-read-receipts topic is created with correct configuration.
     * Requirement 16.2: Create chat-read-receipts topic with 3 partitions, replication factor 1
     */
    @Test
    void testChatReadReceiptsTopicCreatedWithCorrectConfiguration() {
        // Given: Chat read receipts topic bean
        NewTopic chatReadReceiptsTopic = applicationContext.getBean("chatReadReceiptsTopic", NewTopic.class);

        // Then: Topic name should be "chat-read-receipts"
        assertThat(chatReadReceiptsTopic.name()).isEqualTo(ChatKafkaTopicConfig.CHAT_READ_RECEIPTS_TOPIC);
        assertThat(chatReadReceiptsTopic.name()).isEqualTo("chat-read-receipts");

        // And: Should have 3 partitions
        assertThat(chatReadReceiptsTopic.numPartitions()).isEqualTo(3);

        // And: Should have replication factor 1 (single broker setup)
        assertThat(chatReadReceiptsTopic.replicationFactor()).isEqualTo((short) 1);
    }

    /**
     * Test that both topics have the same partition count.
     * This ensures consistent parallelism across both topics.
     */
    @Test
    void testBothTopicsHaveSamePartitionCount() {
        // Given: Both topic beans
        NewTopic chatMessagesTopic = applicationContext.getBean("chatMessagesTopic", NewTopic.class);
        NewTopic chatReadReceiptsTopic = applicationContext.getBean("chatReadReceiptsTopic", NewTopic.class);

        // Then: Both should have same partition count
        assertThat(chatMessagesTopic.numPartitions())
            .isEqualTo(chatReadReceiptsTopic.numPartitions())
            .isEqualTo(3);
    }

    /**
     * Test that both topics have the same replication factor.
     * This ensures consistent reliability across both topics.
     */
    @Test
    void testBothTopicsHaveSameReplicationFactor() {
        // Given: Both topic beans
        NewTopic chatMessagesTopic = applicationContext.getBean("chatMessagesTopic", NewTopic.class);
        NewTopic chatReadReceiptsTopic = applicationContext.getBean("chatReadReceiptsTopic", NewTopic.class);

        // Then: Both should have same replication factor
        assertThat(chatMessagesTopic.replicationFactor())
            .isEqualTo(chatReadReceiptsTopic.replicationFactor())
            .isEqualTo((short) 1);
    }

    /**
     * Test that topic constants are correctly defined.
     */
    @Test
    void testTopicConstantsAreCorrect() {
        // Then: Constants should match expected values
        assertThat(ChatKafkaTopicConfig.CHAT_MESSAGES_TOPIC).isEqualTo("chat-messages");
        assertThat(ChatKafkaTopicConfig.CHAT_READ_RECEIPTS_TOPIC).isEqualTo("chat-read-receipts");
    }
}
