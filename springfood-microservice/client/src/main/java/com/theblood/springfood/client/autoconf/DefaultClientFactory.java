package com.theblood.springfood.client.autoconf;

import com.theblood.springfood.client.api.BaseClient;
import com.theblood.springfood.client.api.BaseClient.Protocol;
import com.theblood.springfood.client.api.BaseClient.ServiceClient;
import com.theblood.springfood.client.config.ClientProperties;
import com.theblood.springfood.client.config.ClientProperties.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of ClientFactory
 */
public class DefaultClientFactory implements ClientFactory, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(DefaultClientFactory.class);

    private final ApplicationContext applicationContext;
    private final ClientProperties clientProperties;
    private final Map<String, BaseClient> clientCache = new ConcurrentHashMap<>();
    private final Map<Class<? extends BaseClient>, ClientMetadata> clientMetadataMap = new ConcurrentHashMap<>();

    private final FeignClientBuilder feignClientBuilder;
    private final GrpcClientBuilder grpcClientBuilder;

    public DefaultClientFactory(ApplicationContext applicationContext,
                                ClientProperties clientProperties,
                                FeignClientBuilder feignClientBuilder,
                                GrpcClientBuilder grpcClientBuilder) {
        this.applicationContext = applicationContext;
        this.clientProperties = clientProperties;
        this.feignClientBuilder = feignClientBuilder;
        this.grpcClientBuilder = grpcClientBuilder;
    }

    // Removed @PostConstruct to avoid circular dependency
    // Scanning will be done lazily when needed
    // @PostConstruct
    // public void init() {
    //     // Scan and register all client interfaces
    //     scanAndRegisterClients();
    // }

    @Override
    public <T extends BaseClient> T createClient(Class<T> clientInterface, Protocol protocol) {
        validateClientInterface(clientInterface);

        ClientMetadata metadata = getClientMetadata(clientInterface);
        ServiceConfig serviceConfig = getServiceConfig(metadata.serviceName);

        if (serviceConfig != null && !serviceConfig.isEnabled()) {
            throw new IllegalStateException("Service " + metadata.serviceName + " is disabled");
        }

        // Validate that builders are available for the requested protocol
        if (protocol == Protocol.REST && feignClientBuilder == null) {
            throw new IllegalStateException(
                    "FeignClientBuilder is not available. Ensure that the client library is properly configured."
            );
        }
        if (protocol == Protocol.GRPC && grpcClientBuilder == null) {
            throw new IllegalStateException(
                    "GrpcClientBuilder is not available. Ensure that the gRPC dependencies are properly configured."
            );
        }

        T client;
        switch (protocol) {
            case REST:
                client = feignClientBuilder.build(clientInterface, serviceConfig, metadata);
                break;
            case GRPC:
                client = grpcClientBuilder.build(clientInterface, serviceConfig, metadata);
                break;
            default:
                throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        logger.info("Created {} client for service {} using interface {}",
                protocol, metadata.serviceName, clientInterface.getName());

        return client;
    }

    @Override
    public <T extends BaseClient> T createClient(Class<T> clientInterface) {
        Protocol protocol = determineProtocol(clientInterface);
        return createClient(clientInterface, protocol);
    }

    @Override
    public <T extends BaseClient> T getClient(Class<T> clientInterface, Protocol protocol) {
        String cacheKey = getCacheKey(clientInterface, protocol);

        return (T) clientCache.computeIfAbsent(cacheKey, key -> {
            return createClient(clientInterface, protocol);
        });
    }

    @Override
    public <T extends BaseClient> T getClient(Class<T> clientInterface) {
        Protocol protocol = determineProtocol(clientInterface);
        return getClient(clientInterface, protocol);
    }

    @Override
    public boolean isClientRegistered(Class<? extends BaseClient> clientInterface) {
        return clientMetadataMap.containsKey(clientInterface);
    }

    @Override
    public List<Class<? extends BaseClient>> getRegisteredClients() {
        return new ArrayList<>(clientMetadataMap.keySet());
    }

    @Override
    public ClientProperties getClientProperties() {
        return clientProperties;
    }

    @Override
    public void refresh() {
        logger.info("Refreshing client factory...");

        // Clear cache
        clientCache.clear();
        clientMetadataMap.clear();

        logger.info("Client factory refreshed successfully");
    }

    @Override
    public void destroy() throws Exception {
        logger.info("Destroying client factory...");

        // Destroy all cached clients
        for (Map.Entry<String, BaseClient> entry : clientCache.entrySet()) {
            BaseClient client = entry.getValue();
            if (client instanceof DisposableBean) {
                try {
                    ((DisposableBean) client).destroy();
                } catch (Exception e) {
                    logger.error("Error destroying client: " + entry.getKey(), e);
                }
            }
        }

        clientCache.clear();
        clientMetadataMap.clear();

        logger.info("Client factory destroyed");
    }


    private void validateClientInterface(Class<? extends BaseClient> clientInterface) {
        if (!clientInterface.isInterface()) {
            throw new IllegalArgumentException("Client must be an interface: " + clientInterface.getName());
        }

        if (!clientInterface.isAnnotationPresent(ServiceClient.class)) {
            throw new IllegalArgumentException("Client interface must be annotated with @ServiceClient: " +
                    clientInterface.getName());
        }
    }

    private ClientMetadata getClientMetadata(Class<? extends BaseClient> clientInterface) {
        // Use lazy registration - create metadata on-demand from annotation
        ClientMetadata metadata = clientMetadataMap.get(clientInterface);
        if (metadata == null) {
            // Create metadata from annotation on-demand
            ServiceClient annotation = clientInterface.getAnnotation(ServiceClient.class);
            if (annotation != null) {
                metadata = new ClientMetadata(
                        annotation.value(),
                        annotation.path(),
                        annotation.circuitBreaker(),
                        annotation.retry()
                );
                clientMetadataMap.put(clientInterface, metadata);
            } else {
                throw new IllegalStateException("Client interface must be annotated with @ServiceClient: " + clientInterface.getName());
            }
        }
        return metadata;
    }

    private ServiceConfig getServiceConfig(String serviceName) {
        return clientProperties.getServices().get(serviceName);
    }

    private Protocol determineProtocol(Class<? extends BaseClient> clientInterface) {
        ClientMetadata metadata = getClientMetadata(clientInterface);
        ServiceConfig serviceConfig = getServiceConfig(metadata.serviceName);

        if (serviceConfig != null && serviceConfig.getProtocol() != null) {
            return Protocol.fromValue(serviceConfig.getProtocol());
        }

        // Use default protocol from properties
        return Protocol.fromValue(clientProperties.getDefaultProtocol());
    }

    private String getCacheKey(Class<? extends BaseClient> clientInterface, Protocol protocol) {
        return clientInterface.getName() + "_" + protocol.getValue();
    }

    /**
     * Interface for protocol-specific client builders
     */
    public interface ClientBuilder {
        <T extends BaseClient> T build(Class<T> clientInterface,
                                       ServiceConfig serviceConfig,
                                       ClientMetadata metadata);
    }

    /**
     * Feign client builder (to be implemented)
     */
    public interface FeignClientBuilder extends ClientBuilder {
    }

    /**
     * gRPC client builder (to be implemented)
     */
    public interface GrpcClientBuilder extends ClientBuilder {
    }

    /**
     * Client metadata holder
     */
    public static class ClientMetadata {
        public final String serviceName;
        public final String path;
        public final boolean circuitBreaker;
        public final boolean retry;

        public ClientMetadata(String serviceName, String path, boolean circuitBreaker, boolean retry) {
            this.serviceName = serviceName;
            this.path = path.isEmpty() ? "/" + serviceName : path;
            this.circuitBreaker = circuitBreaker;
            this.retry = retry;
        }
    }
}
