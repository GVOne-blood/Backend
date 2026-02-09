package com.theblood.springfood.client.autoconf.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.theblood.springfood.client.autoconf.DefaultClientFactory;
import com.theblood.springfood.client.config.ClientProperties;
import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import okhttp3.ConnectionPool;
import okhttp3.Protocol;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Auto-configuration for Feign clients
 */
@Configuration
@ConditionalOnClass({Feign.class, OkHttpClient.class})
@ConditionalOnProperty(prefix = "springfood.client", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ClientProperties.class)
public class FeignClientAutoConfiguration {

    @Bean(name = "clientMethodContract")
    @Primary // Make this the primary Contract bean to override any others
    public Contract feignContract() {
        return new ClientMethodContract();
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryMapEncoder queryMapEncoder() {
        return new ClientRequestParameterProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Register JavaTimeModule to handle Java 8 time types like Instant
        mapper.registerModule(new JavaTimeModule());
        // Configure to write dates as timestamps (or use ISO-8601 string format)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean
    public Encoder feignEncoder(ObjectMapper objectMapper) {
        // Use custom encoder that handles ClientRequest objects
        return new ClientRequestEncoder(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public Decoder feignDecoder(ObjectMapper objectMapper) {
        // Wrap the JacksonDecoder with our ClientResponseDecoder
        return new ClientResponseDecoder(new JacksonDecoder(objectMapper));
    }

    @Bean
    @ConditionalOnMissingBean
    public Logger feignLogger() {
        return new Slf4jLogger();
    }

    @Bean
    @ConditionalOnMissingBean
    public Logger.Level feignLoggerLevel(ClientProperties clientProperties) {
        // Default to BASIC, can be overridden per service
        return Logger.Level.BASIC;
    }

    @Bean
    @ConditionalOnMissingBean
    public okhttp3.OkHttpClient okHttpClient(ClientProperties clientProperties) {
        ClientProperties.DefaultConfig defaults = clientProperties.getDefaults();

        return new okhttp3.OkHttpClient.Builder()
                .connectTimeout(defaults.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(defaults.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(defaults.getWriteTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
                .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .addInterceptor(new FeignLoggingInterceptor())
                .addInterceptor(new FeignMetricsInterceptor(clientProperties))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Client feignClient(okhttp3.OkHttpClient okHttpClient) {
        return new OkHttpClient(okHttpClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultClientFactory.FeignClientBuilder feignClientBuilder(
            @Qualifier("clientMethodContract") Contract contract,
            Encoder encoder,
            Decoder decoder,
            Client client,
            Logger logger,
            Logger.Level logLevel,
            QueryMapEncoder queryMapEncoder,
            ClientProperties clientProperties,
            FeignRequestInterceptor requestInterceptor) {
        return new FeignClientBuilderImpl(contract, encoder, decoder, client, logger, logLevel, queryMapEncoder, clientProperties, requestInterceptor);
    }

    @Bean
    @ConditionalOnMissingBean
    public FeignErrorDecoder feignErrorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public FeignRequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public FeignRetryer customFeignRetryer(ClientProperties clientProperties) {
        ClientProperties.DefaultConfig defaults = clientProperties.getDefaults();
        return new FeignRetryer(
                100, // period
                defaults.getReadTimeout().toMillis(), // maxPeriod
                defaults.getMaxRetries() // maxAttempts
        );
    }
}
