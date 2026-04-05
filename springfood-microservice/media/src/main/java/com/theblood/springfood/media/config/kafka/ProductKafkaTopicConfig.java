package com.theblood.springfood.media.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class ProductKafkaTopicConfig {

    public static final String PRODUCT_FEEDBACK_TOPIC = "product-feedback";
    public static final String SHOP_FEEDBACK_TOPIC = "shop-feedback";

    @Bean
    public NewTopic productFeedbackTopic() {
        return TopicBuilder.name(PRODUCT_FEEDBACK_TOPIC)
            .partitions(1)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic shopFeedbackTopic() {
        return TopicBuilder.name(SHOP_FEEDBACK_TOPIC)
            .partitions(1)
            .replicas(1)
            .build();
    }
}
