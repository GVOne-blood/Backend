package com.theblood.orderservice.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

public class KafkaTopicConfig {

    @Bean
    public NewTopic orderCreationTopic() {
        return TopicBuilder.name("order-validation-request")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderPaymentTopic() {
        return TopicBuilder.name("order-payment-request")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderShopTopic() {
        return TopicBuilder.name("order-shop-request")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderValidationTopic() {
        return TopicBuilder.name("order-validation-request")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderUserTopic() {
        return TopicBuilder.name("order-address-request")
                .partitions(3)
                .replicas(1)
                .build();
    }

}
