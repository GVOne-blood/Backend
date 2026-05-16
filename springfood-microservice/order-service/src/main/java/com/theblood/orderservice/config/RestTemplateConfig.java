package com.theblood.orderservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate dùng để gọi sang các service khác qua Eureka service-id.
 *
 * Ví dụ: {@code restTemplate.postForObject("http://CHAT/api/realtime/notify", ...)}.
 */
@Configuration
public class RestTemplateConfig {

    @Bean("realtimeNotifierRestTemplate")
    @LoadBalanced
    public RestTemplate realtimeNotifierRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        return new RestTemplate(factory);
    }
}
