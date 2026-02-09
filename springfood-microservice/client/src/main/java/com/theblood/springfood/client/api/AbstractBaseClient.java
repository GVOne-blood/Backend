package com.theblood.springfood.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base implementation for all service clients
 */
public abstract class AbstractBaseClient implements BaseClient {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final String serviceName;
    protected final Protocol protocol;

    protected AbstractBaseClient(String serviceName, Protocol protocol) {
        this.serviceName = serviceName;
        this.protocol = protocol;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public Protocol getProtocol() {
        return protocol;
    }

    @Override
    public boolean isHealthy() {
        try {
            return checkHealth();
        } catch (Exception e) {
            logger.warn("Health check failed for {} client to service {}",
                    protocol, serviceName, e);
            return false;
        }
    }

    /**
     * Protocol-specific health check implementation
     */
    protected abstract boolean checkHealth();

    /**
     * Log method invocation for debugging
     */
    protected void logMethodInvocation(String methodName, Object... params) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking {}.{} via {} with params: {}",
                    serviceName, methodName, protocol, params);
        }
    }

    /**
     * Log method response for debugging
     */
    protected void logMethodResponse(String methodName, Object response, long duration) {
        if (logger.isDebugEnabled()) {
            logger.debug("{}.{} via {} completed in {}ms with response: {}",
                    serviceName, methodName, protocol, duration, response);
        }
    }

    /**
     * Log method error
     */
    protected void logMethodError(String methodName, Exception error, long duration) {
        logger.error("{}.{} via {} failed after {}ms",
                serviceName, methodName, protocol, duration, error);
    }
}
