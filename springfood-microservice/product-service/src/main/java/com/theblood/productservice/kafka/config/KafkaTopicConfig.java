package com.theblood.productservice.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // Aiven free tier giới hạn tối đa 2 partitions / topic.
    // Product service không cần parallel consumer cao nên 2 là đủ.
    private static final int DEFAULT_PARTITIONS = 2;
    private static final short DEFAULT_REPLICAS = 1;

    @Bean
    public NewTopic productCreationTopic() {
        return TopicBuilder.name("product-validation-request")
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic productCreationValidatedTopic() {
        return TopicBuilder.name("product-creation-validated")
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }

    @Bean
    public NewTopic productUpdateRequestTopic() {
        return TopicBuilder.name("product-update-request")
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICAS)
                .build();
    }
}
