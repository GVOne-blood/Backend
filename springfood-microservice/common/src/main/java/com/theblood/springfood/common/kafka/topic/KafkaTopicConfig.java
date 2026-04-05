package com.theblood.springfood.common.kafka.topic;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic sagaOrderCreationTopic() {
        return TopicBuilder.name("order-creation-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productCreationValidatedTopic() {
        return TopicBuilder.name("product-creation-validated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productUpdateRequestTopic() {
        return TopicBuilder.name("product-update-request")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
