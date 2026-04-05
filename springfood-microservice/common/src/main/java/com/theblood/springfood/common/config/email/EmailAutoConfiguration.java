package com.theblood.springfood.common.config.email;

import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Auto-configuration for Email service using Brevo API.
 * Automatically creates OkHttpClient bean when springfood.email.enabled=true.
 * 
 * Services can inject EmailService:
 * <pre>
 * {@code @Autowired}
 * private EmailService emailService;
 * </pre>
 */
@Configuration
@EnableConfigurationProperties(EmailProperties.class)
@ConditionalOnProperty(prefix = "springfood.email", name = "enabled", havingValue = "true")
public class EmailAutoConfiguration {
    
    @Bean
    public OkHttpClient emailHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(30))
            .build();
    }
}
