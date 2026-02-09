package com.theblood.springfood.client.autoconf;

import com.theblood.springfood.client.api.BaseClient;
import com.theblood.springfood.client.api.BaseClient.Protocol;
import com.theblood.springfood.client.config.ClientProperties;

import java.util.List;

/**
 * Factory interface for creating service clients
 */
public interface ClientFactory {

    /**
     * Create a client instance for the specified interface and protocol
     *
     * @param clientInterface The client interface class
     * @param protocol        The protocol to use (REST or gRPC)
     * @param <T>             The client interface type
     * @return The client instance
     */
    <T extends BaseClient> T createClient(Class<T> clientInterface, Protocol protocol);

    /**
     * Create a client instance for the specified interface using default protocol
     *
     * @param clientInterface The client interface class
     * @param <T>             The client interface type
     * @return The client instance
     */
    <T extends BaseClient> T createClient(Class<T> clientInterface);

    /**
     * Get or create a client instance (cached)
     *
     * @param clientInterface The client interface class
     * @param protocol        The protocol to use
     * @param <T>             The client interface type
     * @return The cached client instance
     */
    <T extends BaseClient> T getClient(Class<T> clientInterface, Protocol protocol);

    /**
     * Get or create a client instance using default protocol (cached)
     *
     * @param clientInterface The client interface class
     * @param <T>             The client interface type
     * @return The cached client instance
     */
    <T extends BaseClient> T getClient(Class<T> clientInterface);

    /**
     * Check if a client is registered for the given interface
     *
     * @param clientInterface The client interface class
     * @return true if registered, false otherwise
     */
    boolean isClientRegistered(Class<? extends BaseClient> clientInterface);

    /**
     * Get all registered client interfaces
     *
     * @return List of registered client interface classes
     */
    List<Class<? extends BaseClient>> getRegisteredClients();

    /**
     * Get the configuration properties
     *
     * @return The client configuration properties
     */
    ClientProperties getClientProperties();

    /**
     * Refresh client configurations
     */
    void refresh();

    /**
     * Destroy all cached clients
     */
    void destroy() throws Exception;
}
