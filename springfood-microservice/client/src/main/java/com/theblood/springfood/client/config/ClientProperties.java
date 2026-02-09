package com.theblood.springfood.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for microservice clients
 */
@ConfigurationProperties(prefix = "springfood.client")
public class ClientProperties {

    /**
     * Enable/disable client auto-configuration
     */
    private boolean enabled = true;

    /**
     * Default protocol to use if not specified per service
     */
    private String defaultProtocol = "rest";

    /**
     * Global default configurations
     */
    @NestedConfigurationProperty
    private DefaultConfig defaults = new DefaultConfig();

    /**
     * Service-specific configurations
     */
    private Map<String, ServiceConfig> services = new HashMap<>();

    /**
     * Service discovery configuration
     */
    @NestedConfigurationProperty
    private ServiceDiscoveryConfig serviceDiscovery = new ServiceDiscoveryConfig();

    /**
     * Resilience configuration
     */
    @NestedConfigurationProperty
    private ResilienceConfig resilience = new ResilienceConfig();

    /**
     * Observability configuration
     */
    @NestedConfigurationProperty
    private ObservabilityConfig observability = new ObservabilityConfig();

    // Getters and setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultProtocol() {
        return defaultProtocol;
    }

    public void setDefaultProtocol(String defaultProtocol) {
        this.defaultProtocol = defaultProtocol;
    }

    public DefaultConfig getDefaults() {
        return defaults;
    }

    public void setDefaults(DefaultConfig defaults) {
        this.defaults = defaults;
    }

    public Map<String, ServiceConfig> getServices() {
        return services;
    }

    public void setServices(Map<String, ServiceConfig> services) {
        this.services = services;
    }

    public ServiceDiscoveryConfig getServiceDiscovery() {
        return serviceDiscovery;
    }

    public void setServiceDiscovery(ServiceDiscoveryConfig serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public ResilienceConfig getResilience() {
        return resilience;
    }

    public void setResilience(ResilienceConfig resilience) {
        this.resilience = resilience;
    }

    public ObservabilityConfig getObservability() {
        return observability;
    }

    public void setObservability(ObservabilityConfig observability) {
        this.observability = observability;
    }

    /**
     * Default configuration for all clients
     */
    public static class DefaultConfig {
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofSeconds(30);
        private Duration writeTimeout = Duration.ofSeconds(30);
        private Integer maxRetries = 3;
        private boolean enableMetrics = true;
        private boolean enableTracing = true;

        // Getters and setters
        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Duration getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
        }

        public Duration getWriteTimeout() {
            return writeTimeout;
        }

        public void setWriteTimeout(Duration writeTimeout) {
            this.writeTimeout = writeTimeout;
        }

        public Integer getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
        }

        public boolean isEnableMetrics() {
            return enableMetrics;
        }

        public void setEnableMetrics(boolean enableMetrics) {
            this.enableMetrics = enableMetrics;
        }

        public boolean isEnableTracing() {
            return enableTracing;
        }

        public void setEnableTracing(boolean enableTracing) {
            this.enableTracing = enableTracing;
        }
    }

    /**
     * Service-specific configuration
     */
    public static class ServiceConfig {
        private String name;
        private String protocol;
        private String url;
        private String host;
        private Integer port;
        private String basePath;
        private boolean enabled = true;

        @NestedConfigurationProperty
        private RestConfig rest = new RestConfig();

        @NestedConfigurationProperty
        private GrpcConfig grpc = new GrpcConfig();

        @NestedConfigurationProperty
        private ResilienceConfig resilience;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public RestConfig getRest() {
            return rest;
        }

        public void setRest(RestConfig rest) {
            this.rest = rest;
        }

        public GrpcConfig getGrpc() {
            return grpc;
        }

        public void setGrpc(GrpcConfig grpc) {
            this.grpc = grpc;
        }

        public ResilienceConfig getResilience() {
            return resilience;
        }

        public void setResilience(ResilienceConfig resilience) {
            this.resilience = resilience;
        }
    }

    /**
     * REST/Feign specific configuration
     */
    public static class RestConfig {
        private boolean logRequests = false;
        private boolean logResponses = false;
        private String logLevel = "BASIC";
        private boolean followRedirects = true;
        private boolean decode404 = false;

        // Getters and setters
        public boolean isLogRequests() {
            return logRequests;
        }

        public void setLogRequests(boolean logRequests) {
            this.logRequests = logRequests;
        }

        public boolean isLogResponses() {
            return logResponses;
        }

        public void setLogResponses(boolean logResponses) {
            this.logResponses = logResponses;
        }

        public String getLogLevel() {
            return logLevel;
        }

        public void setLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }

        public boolean isFollowRedirects() {
            return followRedirects;
        }

        public void setFollowRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
        }

        public boolean isDecode404() {
            return decode404;
        }

        public void setDecode404(boolean decode404) {
            this.decode404 = decode404;
        }
    }

    /**
     * gRPC specific configuration
     */
    public static class GrpcConfig {
        private boolean usePlaintext = false;
        private Integer maxInboundMessageSize = 4194304; // 4MB
        private Integer maxInboundMetadataSize = 8192; // 8KB
        private Duration keepAliveTime = Duration.ofMinutes(5);
        private Duration keepAliveTimeout = Duration.ofSeconds(20);
        private boolean keepAliveWithoutCalls = false;
        private String negotiationType = "PLAINTEXT";
        private boolean enableRetry = true;
        private Integer maxRetryAttempts = 5;

        // Getters and setters
        public boolean isUsePlaintext() {
            return usePlaintext;
        }

        public void setUsePlaintext(boolean usePlaintext) {
            this.usePlaintext = usePlaintext;
        }

        public Integer getMaxInboundMessageSize() {
            return maxInboundMessageSize;
        }

        public void setMaxInboundMessageSize(Integer maxInboundMessageSize) {
            this.maxInboundMessageSize = maxInboundMessageSize;
        }

        public Integer getMaxInboundMetadataSize() {
            return maxInboundMetadataSize;
        }

        public void setMaxInboundMetadataSize(Integer maxInboundMetadataSize) {
            this.maxInboundMetadataSize = maxInboundMetadataSize;
        }

        public Duration getKeepAliveTime() {
            return keepAliveTime;
        }

        public void setKeepAliveTime(Duration keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }

        public Duration getKeepAliveTimeout() {
            return keepAliveTimeout;
        }

        public void setKeepAliveTimeout(Duration keepAliveTimeout) {
            this.keepAliveTimeout = keepAliveTimeout;
        }

        public boolean isKeepAliveWithoutCalls() {
            return keepAliveWithoutCalls;
        }

        public void setKeepAliveWithoutCalls(boolean keepAliveWithoutCalls) {
            this.keepAliveWithoutCalls = keepAliveWithoutCalls;
        }

        public String getNegotiationType() {
            return negotiationType;
        }

        public void setNegotiationType(String negotiationType) {
            this.negotiationType = negotiationType;
        }

        public boolean isEnableRetry() {
            return enableRetry;
        }

        public void setEnableRetry(boolean enableRetry) {
            this.enableRetry = enableRetry;
        }

        public Integer getMaxRetryAttempts() {
            return maxRetryAttempts;
        }

        public void setMaxRetryAttempts(Integer maxRetryAttempts) {
            this.maxRetryAttempts = maxRetryAttempts;
        }
    }

    /**
     * Service discovery configuration
     */
    public static class ServiceDiscoveryConfig {
        private boolean enabled = false;
        private String type = "consul"; // consul, eureka, kubernetes, etc.
        private String namespace = "default";
        private Map<String, String> metadata = new HashMap<>();

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }
    }

    /**
     * Resilience configuration (circuit breaker, retry, etc.)
     */
    public static class ResilienceConfig {
        @NestedConfigurationProperty
        private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();

        @NestedConfigurationProperty
        private RetryConfig retry = new RetryConfig();

        @NestedConfigurationProperty
        private BulkheadConfig bulkhead = new BulkheadConfig();

        @NestedConfigurationProperty
        private RateLimiterConfig rateLimiter = new RateLimiterConfig();

        // Getters and setters
        public CircuitBreakerConfig getCircuitBreaker() {
            return circuitBreaker;
        }

        public void setCircuitBreaker(CircuitBreakerConfig circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
        }

        public RetryConfig getRetry() {
            return retry;
        }

        public void setRetry(RetryConfig retry) {
            this.retry = retry;
        }

        public BulkheadConfig getBulkhead() {
            return bulkhead;
        }

        public void setBulkhead(BulkheadConfig bulkhead) {
            this.bulkhead = bulkhead;
        }

        public RateLimiterConfig getRateLimiter() {
            return rateLimiter;
        }

        public void setRateLimiter(RateLimiterConfig rateLimiter) {
            this.rateLimiter = rateLimiter;
        }
    }

    /**
     * Circuit breaker configuration
     */
    public static class CircuitBreakerConfig {
        private boolean enabled = true;
        private Integer failureRateThreshold = 50;
        private Integer slowCallRateThreshold = 100;
        private Duration slowCallDurationThreshold = Duration.ofSeconds(60);
        private Integer slidingWindowSize = 100;
        private Integer minimumNumberOfCalls = 20;
        private Duration waitDurationInOpenState = Duration.ofSeconds(60);

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getFailureRateThreshold() {
            return failureRateThreshold;
        }

        public void setFailureRateThreshold(Integer failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
        }

        public Integer getSlowCallRateThreshold() {
            return slowCallRateThreshold;
        }

        public void setSlowCallRateThreshold(Integer slowCallRateThreshold) {
            this.slowCallRateThreshold = slowCallRateThreshold;
        }

        public Duration getSlowCallDurationThreshold() {
            return slowCallDurationThreshold;
        }

        public void setSlowCallDurationThreshold(Duration slowCallDurationThreshold) {
            this.slowCallDurationThreshold = slowCallDurationThreshold;
        }

        public Integer getSlidingWindowSize() {
            return slidingWindowSize;
        }

        public void setSlidingWindowSize(Integer slidingWindowSize) {
            this.slidingWindowSize = slidingWindowSize;
        }

        public Integer getMinimumNumberOfCalls() {
            return minimumNumberOfCalls;
        }

        public void setMinimumNumberOfCalls(Integer minimumNumberOfCalls) {
            this.minimumNumberOfCalls = minimumNumberOfCalls;
        }

        public Duration getWaitDurationInOpenState() {
            return waitDurationInOpenState;
        }

        public void setWaitDurationInOpenState(Duration waitDurationInOpenState) {
            this.waitDurationInOpenState = waitDurationInOpenState;
        }
    }

    /**
     * Retry configuration
     */
    public static class RetryConfig {
        private boolean enabled = true;
        private Integer maxAttempts = 3;
        private Duration waitDuration = Duration.ofMillis(500);
        private boolean exponentialBackoff = true;
        private Double exponentialBackoffMultiplier = 2.0;
        private Duration maxWaitDuration = Duration.ofSeconds(5);

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(Integer maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Duration getWaitDuration() {
            return waitDuration;
        }

        public void setWaitDuration(Duration waitDuration) {
            this.waitDuration = waitDuration;
        }

        public boolean isExponentialBackoff() {
            return exponentialBackoff;
        }

        public void setExponentialBackoff(boolean exponentialBackoff) {
            this.exponentialBackoff = exponentialBackoff;
        }

        public Double getExponentialBackoffMultiplier() {
            return exponentialBackoffMultiplier;
        }

        public void setExponentialBackoffMultiplier(Double exponentialBackoffMultiplier) {
            this.exponentialBackoffMultiplier = exponentialBackoffMultiplier;
        }

        public Duration getMaxWaitDuration() {
            return maxWaitDuration;
        }

        public void setMaxWaitDuration(Duration maxWaitDuration) {
            this.maxWaitDuration = maxWaitDuration;
        }
    }

    /**
     * Bulkhead configuration
     */
    public static class BulkheadConfig {
        private boolean enabled = false;
        private Integer maxConcurrentCalls = 25;
        private Duration maxWaitDuration = Duration.ofSeconds(0);

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getMaxConcurrentCalls() {
            return maxConcurrentCalls;
        }

        public void setMaxConcurrentCalls(Integer maxConcurrentCalls) {
            this.maxConcurrentCalls = maxConcurrentCalls;
        }

        public Duration getMaxWaitDuration() {
            return maxWaitDuration;
        }

        public void setMaxWaitDuration(Duration maxWaitDuration) {
            this.maxWaitDuration = maxWaitDuration;
        }
    }

    /**
     * Rate limiter configuration
     */
    public static class RateLimiterConfig {
        private boolean enabled = false;
        private Integer limitForPeriod = 50;
        private Duration limitRefreshPeriod = Duration.ofSeconds(1);
        private Duration timeoutDuration = Duration.ofSeconds(5);

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getLimitForPeriod() {
            return limitForPeriod;
        }

        public void setLimitForPeriod(Integer limitForPeriod) {
            this.limitForPeriod = limitForPeriod;
        }

        public Duration getLimitRefreshPeriod() {
            return limitRefreshPeriod;
        }

        public void setLimitRefreshPeriod(Duration limitRefreshPeriod) {
            this.limitRefreshPeriod = limitRefreshPeriod;
        }

        public Duration getTimeoutDuration() {
            return timeoutDuration;
        }

        public void setTimeoutDuration(Duration timeoutDuration) {
            this.timeoutDuration = timeoutDuration;
        }
    }

    /**
     * Observability configuration
     */
    public static class ObservabilityConfig {
        private boolean metricsEnabled = true;
        private boolean tracingEnabled = true;
        private boolean loggingEnabled = true;
        private String metricsPrefix = "springfood.client";

        // Getters and setters
        public boolean isMetricsEnabled() {
            return metricsEnabled;
        }

        public void setMetricsEnabled(boolean metricsEnabled) {
            this.metricsEnabled = metricsEnabled;
        }

        public boolean isTracingEnabled() {
            return tracingEnabled;
        }

        public void setTracingEnabled(boolean tracingEnabled) {
            this.tracingEnabled = tracingEnabled;
        }

        public boolean isLoggingEnabled() {
            return loggingEnabled;
        }

        public void setLoggingEnabled(boolean loggingEnabled) {
            this.loggingEnabled = loggingEnabled;
        }

        public String getMetricsPrefix() {
            return metricsPrefix;
        }

        public void setMetricsPrefix(String metricsPrefix) {
            this.metricsPrefix = metricsPrefix;
        }
    }
}
