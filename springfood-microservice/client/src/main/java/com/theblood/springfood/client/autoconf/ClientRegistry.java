package com.theblood.springfood.client.autoconf;

import com.theblood.springfood.client.api.BaseClient;
import com.theblood.springfood.client.api.BaseClient.ServiceClient;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for discovering and managing client interfaces
 */
@Component
public class ClientRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ClientRegistry.class);

    private final Map<String, Class<? extends BaseClient>> serviceToClientMap = new ConcurrentHashMap<>();
    private final Map<Class<? extends BaseClient>, String> clientToServiceMap = new ConcurrentHashMap<>();
    private final Set<Class<? extends BaseClient>> registeredClients = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void init() {
        scanForClients();
    }

    /**
     * Scan classpath for client interfaces
     */
    private void scanForClients() {
        logger.info("Scanning for client interfaces...");

        try {
            // Scan the client package for interfaces with @ServiceClient
            Reflections reflections = new Reflections("com.theblood.springfood.client");
            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(ServiceClient.class);

            for (Class<?> clazz : annotatedClasses) {
                if (clazz.isInterface() && BaseClient.class.isAssignableFrom(clazz)) {
                    registerClient((Class<? extends BaseClient>) clazz);
                }
            }

            logger.info("Found {} client interfaces", registeredClients.size());
        } catch (Exception e) {
            logger.error("Error scanning for client interfaces", e);
        }
    }

    /**
     * Register a client interface
     */
    public void registerClient(Class<? extends BaseClient> clientInterface) {
        ServiceClient annotation = clientInterface.getAnnotation(ServiceClient.class);
        if (annotation != null) {
            String serviceName = annotation.value();

            registeredClients.add(clientInterface);
            serviceToClientMap.put(serviceName, clientInterface);
            clientToServiceMap.put(clientInterface, serviceName);

            logger.debug("Registered client {} for service {}",
                    clientInterface.getSimpleName(), serviceName);
        }
    }

    /**
     * Get client interface by service name
     */
    public Optional<Class<? extends BaseClient>> getClientByService(String serviceName) {
        return Optional.ofNullable(serviceToClientMap.get(serviceName));
    }

    /**
     * Get service name by client interface
     */
    public Optional<String> getServiceByClient(Class<? extends BaseClient> clientInterface) {
        return Optional.ofNullable(clientToServiceMap.get(clientInterface));
    }

    /**
     * Get all registered client interfaces
     */
    public Set<Class<? extends BaseClient>> getAllClients() {
        return new HashSet<>(registeredClients);
    }

    /**
     * Get all registered service names
     */
    public Set<String> getAllServices() {
        return new HashSet<>(serviceToClientMap.keySet());
    }

    /**
     * Check if a client is registered
     */
    public boolean isClientRegistered(Class<? extends BaseClient> clientInterface) {
        return registeredClients.contains(clientInterface);
    }

    /**
     * Check if a service has a registered client
     */
    public boolean hasClientForService(String serviceName) {
        return serviceToClientMap.containsKey(serviceName);
    }

    /**
     * Clear all registrations
     */
    public void clear() {
        registeredClients.clear();
        serviceToClientMap.clear();
        clientToServiceMap.clear();
    }

    /**
     * Refresh registry by re-scanning
     */
    public void refresh() {
        clear();
        scanForClients();
    }
}
