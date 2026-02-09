package com.theblood.springfood.client.api;

import java.lang.annotation.*;

/**
 * Base interface for all service clients.
 * This interface defines the contract that all protocol-specific clients must implement.
 */
public interface BaseClient {

    /**
     * Get the service name this client is connected to
     */
    String getServiceName();

    /**
     * Get the protocol type used by this client
     */
    Protocol getProtocol();

    /**
     * Check if the client is healthy and can accept requests
     */
    boolean isHealthy();

    /**
     * Protocol types supported by the client
     */
    enum Protocol {
        REST("rest"),
        GRPC("grpc");

        private final String value;

        Protocol(String value) {
            this.value = value;
        }

        public static Protocol fromValue(String value) {
            for (Protocol protocol : values()) {
                if (protocol.value.equalsIgnoreCase(value)) {
                    return protocol;
                }
            }
            throw new IllegalArgumentException("Unknown protocol: " + value);
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Annotation to mark a client interface
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface ServiceClient {
        /**
         * The name of the service this client connects to
         */
        String value();

        /**
         * The path prefix for REST endpoints (optional, defaults to service name)
         */
        String path() default "";

        /**
         * Whether to enable circuit breaker for this client
         */
        boolean circuitBreaker() default true;

        /**
         * Whether to enable retry for this client
         */
        boolean retry() default true;
    }
}
