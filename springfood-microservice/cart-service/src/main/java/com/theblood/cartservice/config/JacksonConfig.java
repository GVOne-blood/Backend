package com.theblood.cartservice.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Jackson cho HTTP REST response.
 *
 * <p>Mặc định Spring Boot serialize {@code LocalDateTime}/{@code Instant} thành
 * array số {@code [year, month, day, hour, ...]}. FE expect ISO string
 * ({@code "2026-05-16T17:15:39.994Z"}) nên ta:</p>
 * <ul>
 *   <li>Register {@link JavaTimeModule} để hỗ trợ JSR-310 types</li>
 *   <li>Tắt {@link SerializationFeature#WRITE_DATES_AS_TIMESTAMPS} để output
 *       ISO-8601 string</li>
 * </ul>
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                .modules(new JavaTimeModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
