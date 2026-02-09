package com.theblood.springfood.client.autoconf.feign;

import com.theblood.springfood.client.api.BaseClient;
import com.theblood.springfood.client.autoconf.DefaultClientFactory;
import com.theblood.springfood.client.config.ClientProperties;
import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of Feign client builder
 */
public class FeignClientBuilderImpl implements DefaultClientFactory.FeignClientBuilder {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FeignClientBuilderImpl.class);

    private final Contract contract;
    private final Encoder encoder;
    private final Decoder decoder;
    private final Client client;
    private final Logger logger;
    private final Logger.Level logLevel;
    private final QueryMapEncoder queryMapEncoder;
    private final ClientProperties clientProperties;
    private final FeignRequestInterceptor requestInterceptor;

    public FeignClientBuilderImpl(Contract contract,
                                  Encoder encoder,
                                  Decoder decoder,
                                  Client client,
                                  Logger logger,
                                  Logger.Level logLevel,
                                  QueryMapEncoder queryMapEncoder,
                                  ClientProperties clientProperties,
                                  FeignRequestInterceptor requestInterceptor) {
        this.contract = contract;
        this.encoder = encoder;
        this.decoder = decoder;
        this.client = client;
        this.logger = logger;
        this.logLevel = logLevel;
        this.queryMapEncoder = queryMapEncoder;
        this.clientProperties = clientProperties;
        this.requestInterceptor = requestInterceptor;
    }

    @Override
    public <T extends BaseClient> T build(Class<T> clientInterface,
                                          ClientProperties.ServiceConfig serviceConfig,
                                          DefaultClientFactory.ClientMetadata metadata) {

        String serviceName = metadata.serviceName;
        String url = buildUrl(serviceConfig);

        log.info("Building Feign client for service {} with URL: {}", serviceName, url);

        // Build Feign client
        Feign.Builder builder = Feign.builder()
                .contract(contract) // Use injected contract to handle @ClientMethod
                .encoder(encoder)
                .decoder(decoder)
                .queryMapEncoder(queryMapEncoder) // Use custom query map encoder for ClientRequest
                .client(client)
                .logger(logger)
                .logLevel(determineLogLevel(serviceConfig))
                .errorDecoder(new FeignErrorDecoder())
                .requestInterceptor(requestInterceptor)
                .retryer(buildRetryer(serviceConfig));

        // Configure options
        Request.Options options = buildRequestOptions(serviceConfig);
        builder.options(options);

        // Configure decode404 if needed
        if (serviceConfig != null && serviceConfig.getRest().isDecode404()) {
            builder.decode404();
        }

        // Create the actual Feign client
        T feignClient = builder.target(clientInterface, url);

        // Wrap with base client implementation
        return wrapWithBaseClient(feignClient, clientInterface, serviceName);
    }

    private String buildUrl(ClientProperties.ServiceConfig serviceConfig) {
        if (serviceConfig == null) {
            throw new IllegalArgumentException("Service configuration is required");
        }

        // If full URL is provided, use it
        if (serviceConfig.getUrl() != null) {
            return serviceConfig.getUrl();
        }

        // Build URL from host and port
        String protocol = "http";
        String host = serviceConfig.getHost();
        Integer port = serviceConfig.getPort();
        String basePath = serviceConfig.getBasePath();

        if (host == null) {
            throw new IllegalArgumentException("Either url or host must be configured");
        }

        StringBuilder urlBuilder = new StringBuilder(protocol).append("://").append(host);

        if (port != null) {
            urlBuilder.append(":").append(port);
        }

        if (basePath != null && !basePath.isEmpty()) {
            if (!basePath.startsWith("/")) {
                urlBuilder.append("/");
            }
            urlBuilder.append(basePath);
        }

        return urlBuilder.toString();
    }

    private Logger.Level determineLogLevel(ClientProperties.ServiceConfig serviceConfig) {
        if (serviceConfig != null && serviceConfig.getRest().getLogLevel() != null) {
            try {
                return Logger.Level.valueOf(serviceConfig.getRest().getLogLevel());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid log level: {}, using default", serviceConfig.getRest().getLogLevel());
            }
        }
        return logLevel;
    }

    private Request.Options buildRequestOptions(ClientProperties.ServiceConfig serviceConfig) {
        ClientProperties.DefaultConfig defaults = clientProperties.getDefaults();

        long connectTimeout = defaults.getConnectTimeout().toMillis();
        long readTimeout = defaults.getReadTimeout().toMillis();
        boolean followRedirects = true;

        if (serviceConfig != null) {
            // Override with service-specific config if available
            ClientProperties.RestConfig restConfig = serviceConfig.getRest();
            if (restConfig != null) {
                followRedirects = restConfig.isFollowRedirects();
            }
        }

        return new Request.Options(
                connectTimeout, TimeUnit.MILLISECONDS,
                readTimeout, TimeUnit.MILLISECONDS,
                followRedirects
        );
    }

    private FeignRetryer buildRetryer(ClientProperties.ServiceConfig serviceConfig) {
        ClientProperties.RetryConfig retryConfig = null;

        if (serviceConfig != null && serviceConfig.getResilience() != null) {
            retryConfig = serviceConfig.getResilience().getRetry();
        }

        if (retryConfig == null) {
            retryConfig = clientProperties.getResilience().getRetry();
        }

        if (retryConfig.isEnabled()) {
            return new FeignRetryer(
                    100, // period
                    retryConfig.getMaxWaitDuration().toMillis(),
                    retryConfig.getMaxAttempts()
            );
        } else {
            return new FeignRetryer(100, 1000, 1); // No retry - max attempts = 1
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseClient> T wrapWithBaseClient(T feignClient,
                                                        Class<T> clientInterface,
                                                        String serviceName) {
        // Create a wrapper that implements both the client interface and BaseClient
        return (T) java.lang.reflect.Proxy.newProxyInstance(
                clientInterface.getClassLoader(),
                new Class<?>[]{clientInterface},
                new FeignClientWrapper<>(feignClient, serviceName, BaseClient.Protocol.REST)
        );
    }
}
