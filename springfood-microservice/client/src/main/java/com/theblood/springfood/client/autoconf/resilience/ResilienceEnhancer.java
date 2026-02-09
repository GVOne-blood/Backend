package com.theblood.springfood.client.autoconf.resilience;

import com.theblood.springfood.client.config.ClientProperties;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Helper class to enhance method calls with resilience patterns
 */
public class ResilienceEnhancer {

    private static final Logger logger = LoggerFactory.getLogger(ResilienceEnhancer.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final BulkheadRegistry bulkheadRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final ClientProperties clientProperties;

    public ResilienceEnhancer(CircuitBreakerRegistry circuitBreakerRegistry,
                              RetryRegistry retryRegistry,
                              BulkheadRegistry bulkheadRegistry,
                              RateLimiterRegistry rateLimiterRegistry,
                              ClientProperties clientProperties) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.bulkheadRegistry = bulkheadRegistry;
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.clientProperties = clientProperties;
    }

    /**
     * Enhance a callable with resilience patterns
     */
    public <T> T enhance(String serviceName, Callable<T> callable) throws Exception {
        return enhance(serviceName, callable, true);
    }

    /**
     * Enhance a callable with resilience patterns
     */
    public <T> T enhance(String serviceName, Callable<T> callable, boolean idempotent) throws Exception {
        Callable<T> enhanced = callable;

        // Apply rate limiter if enabled
        if (isRateLimiterEnabled(serviceName)) {
            RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(serviceName);
            enhanced = RateLimiter.decorateCallable(rateLimiter, enhanced);
        }

        // Apply bulkhead if enabled
        if (isBulkheadEnabled(serviceName)) {
            Bulkhead bulkhead = bulkheadRegistry.bulkhead(serviceName);
            enhanced = Bulkhead.decorateCallable(bulkhead, enhanced);
        }

        // Apply retry if enabled and idempotent
        if (isRetryEnabled(serviceName) && idempotent) {
            Retry retry = retryRegistry.retry(serviceName);
            enhanced = Retry.decorateCallable(retry, enhanced);
        }

        // Apply circuit breaker if enabled
        if (isCircuitBreakerEnabled(serviceName)) {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
            enhanced = CircuitBreaker.decorateCallable(circuitBreaker, enhanced);
        }

        return enhanced.call();
    }

    /**
     * Enhance a supplier with resilience patterns
     */
    public <T> T enhance(String serviceName, Supplier<T> supplier) {
        return enhance(serviceName, supplier, true);
    }

    /**
     * Enhance a supplier with resilience patterns
     */
    public <T> T enhance(String serviceName, Supplier<T> supplier, boolean idempotent) {
        Supplier<T> enhanced = supplier;

        // Apply rate limiter if enabled
        if (isRateLimiterEnabled(serviceName)) {
            RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(serviceName);
            enhanced = RateLimiter.decorateSupplier(rateLimiter, enhanced);
        }

        // Apply bulkhead if enabled
        if (isBulkheadEnabled(serviceName)) {
            Bulkhead bulkhead = bulkheadRegistry.bulkhead(serviceName);
            enhanced = Bulkhead.decorateSupplier(bulkhead, enhanced);
        }

        // Apply retry if enabled and idempotent
        if (isRetryEnabled(serviceName) && idempotent) {
            Retry retry = retryRegistry.retry(serviceName);
            enhanced = Retry.decorateSupplier(retry, enhanced);
        }

        // Apply circuit breaker if enabled
        if (isCircuitBreakerEnabled(serviceName)) {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
            enhanced = CircuitBreaker.decorateSupplier(circuitBreaker, enhanced);
        }

        return enhanced.get();
    }

    /**
     * Enhance a CompletableFuture supplier with resilience patterns
     */
    public <T> CompletableFuture<T> enhanceAsync(String serviceName,
                                                 Supplier<CompletableFuture<T>> futureSupplier) {
        return enhanceAsync(serviceName, futureSupplier, true);
    }

    /**
     * Enhance a CompletableFuture supplier with resilience patterns
     */
    public <T> CompletableFuture<T> enhanceAsync(String serviceName,
                                                 Supplier<CompletableFuture<T>> futureSupplier,
                                                 boolean idempotent) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Supplier<CompletableFuture<T>> supplier = futureSupplier;
                return enhance(serviceName, supplier, idempotent);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenCompose(future -> future);
    }

    private boolean isCircuitBreakerEnabled(String serviceName) {
        ClientProperties.ServiceConfig serviceConfig = clientProperties.getServices().get(serviceName);
        if (serviceConfig != null && serviceConfig.getResilience() != null &&
                serviceConfig.getResilience().getCircuitBreaker() != null) {
            return serviceConfig.getResilience().getCircuitBreaker().isEnabled();
        }
        return clientProperties.getResilience().getCircuitBreaker().isEnabled();
    }

    private boolean isRetryEnabled(String serviceName) {
        ClientProperties.ServiceConfig serviceConfig = clientProperties.getServices().get(serviceName);
        if (serviceConfig != null && serviceConfig.getResilience() != null &&
                serviceConfig.getResilience().getRetry() != null) {
            return serviceConfig.getResilience().getRetry().isEnabled();
        }
        return clientProperties.getResilience().getRetry().isEnabled();
    }

    private boolean isBulkheadEnabled(String serviceName) {
        ClientProperties.ServiceConfig serviceConfig = clientProperties.getServices().get(serviceName);
        if (serviceConfig != null && serviceConfig.getResilience() != null &&
                serviceConfig.getResilience().getBulkhead() != null) {
            return serviceConfig.getResilience().getBulkhead().isEnabled();
        }
        return clientProperties.getResilience().getBulkhead().isEnabled();
    }

    private boolean isRateLimiterEnabled(String serviceName) {
        ClientProperties.ServiceConfig serviceConfig = clientProperties.getServices().get(serviceName);
        if (serviceConfig != null && serviceConfig.getResilience() != null &&
                serviceConfig.getResilience().getRateLimiter() != null) {
            return serviceConfig.getResilience().getRateLimiter().isEnabled();
        }
        return clientProperties.getResilience().getRateLimiter().isEnabled();
    }
}
