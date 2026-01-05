package com.theblood.paymentservice.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic productCreationTopic() {
        return TopicBuilder.name("product-validation-request")
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
    public NewTopic codOrderPaymentTopic(){
        return TopicBuilder.name("cod-transaction-event")
                .partitions(5)
                .replicas(2)
                .build();
    }

}

