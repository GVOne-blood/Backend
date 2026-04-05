package com.theblood.springfood.media.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Topic Configuration for Media Service.
 * Creates Kafka topics for media file events and processing.
 */
@Configuration
public class MediaKafkaTopicConfig {

    /**
     * Topic name for media upload events.
     * Used for notifying other services about new media uploads.
     */
    public static final String MEDIA_UPLOAD_TOPIC = "media-upload-events";

    /**
     * Topic name for media processing events.
     * Used for async media processing tasks (thumbnails, compression, etc).
     */
    public static final String MEDIA_PROCESSING_TOPIC = "media-processing-events";

    /**
     * Topic name for media deletion events.
     * Used for cleanup and cascade deletion notifications.
     */
    public static final String MEDIA_DELETION_TOPIC = "media-deletion-events";

    /**
     * Create "media-upload-events" topic.
     * <p>
     * Configuration:
     * - 3 partitions: Allows parallel processing of upload events
     * - Replication factor 1: Single broker setup (standalone Kafka)
     */
    @Bean
    public NewTopic mediaUploadTopic() {
        return TopicBuilder.name(MEDIA_UPLOAD_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    /**
     * Create "media-processing-events" topic.
     * <p>
     * Configuration:
     * - 3 partitions: Allows parallel processing of media files
     * - Replication factor 1: Single broker setup (standalone Kafka)
     */
    @Bean
    public NewTopic mediaProcessingTopic() {
        return TopicBuilder.name(MEDIA_PROCESSING_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    /**
     * Create "media-deletion-events" topic.
     * <p>
     * Configuration:
     * - 3 partitions: Allows parallel processing of deletion events
     * - Replication factor 1: Single broker setup (standalone Kafka)
     */
    @Bean
    public NewTopic mediaDeletionTopic() {
        return TopicBuilder.name(MEDIA_DELETION_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
