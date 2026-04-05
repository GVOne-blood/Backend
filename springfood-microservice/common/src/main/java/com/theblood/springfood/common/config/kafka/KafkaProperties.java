package com.theblood.springfood.common.config.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Centralized Kafka configuration properties for all SpringFood services.
 * 
 * Usage in application.yml:
 * <pre>
 * springfood:
 *   kafka:
 *     enabled: true
 *     bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
 *     consumer:
 *       group-id-prefix: order-service
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "springfood.kafka")
public class KafkaProperties {
    
    /**
     * Enable/disable Kafka integration
     */
    private boolean enabled = true;
    
    /**
     * Kafka bootstrap servers
     */
    private String bootstrapServers = "localhost:9092";
    
    /**
     * Producer configuration
     */
    private Producer producer = new Producer();
    
    /**
     * Consumer configuration
     */
    private Consumer consumer = new Consumer();
    
    @Data
    public static class Producer {
        private String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";
        private String valueSerializer = "org.springframework.kafka.support.serializer.JsonSerializer";
        private Integer retries = 3;
        private String acks = "all";
        private Integer batchSize = 16384;
        private Integer lingerMs = 10;
        private Boolean enableIdempotence = true;
        private Integer maxInFlightRequestsPerConnection = 5;
    }
    
    @Data
    public static class Consumer {
        private String groupIdPrefix = "springfood";
        private String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
        private String valueDeserializer = "org.springframework.kafka.support.serializer.JsonDeserializer";
        private String autoOffsetReset = "earliest";
        private Boolean enableAutoCommit = false;
        private String trustedPackages = "*";
    }
}
