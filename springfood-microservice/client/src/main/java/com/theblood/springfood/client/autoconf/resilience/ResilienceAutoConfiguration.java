package com.viettel.dvs.client.autoconf.resilience;

import com.theblood.springfood.client.autoconf.resilience.ResilienceEnhancer;
import com.theblood.springfood.client.config.ClientProperties;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/// **
// * Auto-configuration for resilience patterns (circuit breaker, retry, etc.)
// */
@Configuration
@ConditionalOnClass({CircuitBreaker.class})
@ConditionalOnProperty(prefix = "springfood.client.resilience", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ClientProperties.class)
public class ResilienceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry(ClientProperties clientProperties) {
        Map<String, CircuitBreakerConfig> configs = new HashMap<>();

        // Default config
        ClientProperties.CircuitBreakerConfig defaultConfig = clientProperties.getResilience().getCircuitBreaker();
        CircuitBreakerConfig baseConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(defaultConfig.getFailureRateThreshold())
                .slowCallRateThreshold(defaultConfig.getSlowCallRateThreshold())
                .slowCallDurationThreshold(defaultConfig.getSlowCallDurationThreshold())
                .slidingWindowSize(defaultConfig.getSlidingWindowSize())
                .minimumNumberOfCalls(defaultConfig.getMinimumNumberOfCalls())
                .waitDurationInOpenState(defaultConfig.getWaitDurationInOpenState())
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        configs.put("default", baseConfig);

        // Service-specific configs
        clientProperties.getServices().forEach((serviceName, serviceConfig) -> {
            if (serviceConfig.getResilience() != null &&
                    serviceConfig.getResilience().getCircuitBreaker() != null &&
                    serviceConfig.getResilience().getCircuitBreaker().isEnabled()) {

                ClientProperties.CircuitBreakerConfig cbConfig = serviceConfig.getResilience().getCircuitBreaker();
                CircuitBreakerConfig customConfig = CircuitBreakerConfig.from(baseConfig)
                        .failureRateThreshold(cbConfig.getFailureRateThreshold())
                        .slowCallRateThreshold(cbConfig.getSlowCallRateThreshold())
                        .slowCallDurationThreshold(cbConfig.getSlowCallDurationThreshold())
                        .build();

                configs.put(serviceName, customConfig);
            }
        });

        return CircuitBreakerRegistry.of(configs);
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryRegistry retryRegistry(ClientProperties clientProperties) {
        Map<String, RetryConfig> configs = new HashMap<>();

        // Default config
        ClientProperties.RetryConfig defaultConfig = clientProperties.getResilience().getRetry();
        RetryConfig baseConfig = RetryConfig.custom()
                .maxAttempts(defaultConfig.getMaxAttempts())
                .waitDuration(defaultConfig.getWaitDuration())
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        if (defaultConfig.isExponentialBackoff()) {
            baseConfig = RetryConfig.from(baseConfig)
                    .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                            defaultConfig.getWaitDuration().toMillis(),
                            defaultConfig.getExponentialBackoffMultiplier(),
                            defaultConfig.getMaxWaitDuration().toMillis()
                    ))
                    .build();
        }

        configs.put("default", baseConfig);

        // Service-specific configs
        final RetryConfig finalBaseConfig = baseConfig;
        clientProperties.getServices().forEach((serviceName, serviceConfig) -> {
            if (serviceConfig.getResilience() != null &&
                    serviceConfig.getResilience().getRetry() != null &&
                    serviceConfig.getResilience().getRetry().isEnabled()) {

                ClientProperties.RetryConfig retryConfig = serviceConfig.getResilience().getRetry();
                RetryConfig customConfig = RetryConfig.from(finalBaseConfig)
                        .maxAttempts(retryConfig.getMaxAttempts())
                        .waitDuration(retryConfig.getWaitDuration())
                        .build();

                configs.put(serviceName, customConfig);
            }
        });

        return RetryRegistry.of(configs);
    }

    @Bean
    @ConditionalOnMissingBean
    public BulkheadRegistry bulkheadRegistry(ClientProperties clientProperties) {
        Map<String, BulkheadConfig> configs = new HashMap<>();

        // Default config
        ClientProperties.BulkheadConfig defaultConfig = clientProperties.getResilience().getBulkhead();
        BulkheadConfig baseConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(defaultConfig.getMaxConcurrentCalls())
                .maxWaitDuration(defaultConfig.getMaxWaitDuration())
                .build();

        configs.put("default", baseConfig);

        // Service-specific configs
        clientProperties.getServices().forEach((serviceName, serviceConfig) -> {
            if (serviceConfig.getResilience() != null &&
                    serviceConfig.getResilience().getBulkhead() != null &&
                    serviceConfig.getResilience().getBulkhead().isEnabled()) {

                ClientProperties.BulkheadConfig bulkheadConfig = serviceConfig.getResilience().getBulkhead();
                BulkheadConfig customConfig = BulkheadConfig.from(baseConfig)
                        .maxConcurrentCalls(bulkheadConfig.getMaxConcurrentCalls())
                        .maxWaitDuration(bulkheadConfig.getMaxWaitDuration())
                        .build();

                configs.put(serviceName, customConfig);
            }
        });

        return BulkheadRegistry.of(configs);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimiterRegistry rateLimiterRegistry(ClientProperties clientProperties) {
        Map<String, RateLimiterConfig> configs = new HashMap<>();

        // Default config
        ClientProperties.RateLimiterConfig defaultConfig = clientProperties.getResilience().getRateLimiter();
        RateLimiterConfig baseConfig = RateLimiterConfig.custom()
                .limitForPeriod(defaultConfig.getLimitForPeriod())
                .limitRefreshPeriod(defaultConfig.getLimitRefreshPeriod())
                .timeoutDuration(defaultConfig.getTimeoutDuration())
                .build();

        configs.put("default", baseConfig);

        // Service-specific configs
        clientProperties.getServices().forEach((serviceName, serviceConfig) -> {
            if (serviceConfig.getResilience() != null &&
                    serviceConfig.getResilience().getRateLimiter() != null &&
                    serviceConfig.getResilience().getRateLimiter().isEnabled()) {

                ClientProperties.RateLimiterConfig rateLimiterConfig = serviceConfig.getResilience().getRateLimiter();
                RateLimiterConfig customConfig = RateLimiterConfig.from(baseConfig)
                        .limitForPeriod(rateLimiterConfig.getLimitForPeriod())
                        .limitRefreshPeriod(rateLimiterConfig.getLimitRefreshPeriod())
                        .timeoutDuration(rateLimiterConfig.getTimeoutDuration())
                        .build();

                configs.put(serviceName, customConfig);
            }
        });

        return RateLimiterRegistry.of(configs);
    }

    @Bean
    public ResilienceEnhancer resilienceEnhancer(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            BulkheadRegistry bulkheadRegistry,
            RateLimiterRegistry rateLimiterRegistry,
            ClientProperties clientProperties) {
        return new ResilienceEnhancer(
                circuitBreakerRegistry,
                retryRegistry,
                bulkheadRegistry,
                rateLimiterRegistry,
                clientProperties
        );
    }
}
