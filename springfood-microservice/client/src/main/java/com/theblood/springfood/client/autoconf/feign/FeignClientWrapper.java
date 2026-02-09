package com.theblood.springfood.client.autoconf.feign;

import com.theblood.springfood.client.api.AbstractBaseClient;
import com.theblood.springfood.client.api.BaseClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Wrapper for Feign clients to implement BaseClient interface
 */
public class FeignClientWrapper<T> extends AbstractBaseClient implements InvocationHandler {

    private final T feignClient;

    public FeignClientWrapper(T feignClient, String serviceName, Protocol protocol) {
        super(serviceName, protocol);
        this.feignClient = feignClient;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // If it's a BaseClient method, handle it
        if (method.getDeclaringClass() == BaseClient.class ||
                method.getDeclaringClass() == AbstractBaseClient.class) {
            return method.invoke(this, args);
        }

        // Otherwise, delegate to the Feign client
        long startTime = System.currentTimeMillis();
        try {
            logMethodInvocation(method.getName(), args);

            Object result = method.invoke(feignClient, args);

            long duration = System.currentTimeMillis() - startTime;
            logMethodResponse(method.getName(), result, duration);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logMethodError(method.getName(), e, duration);
            throw e;
        }
    }

    @Override
    protected boolean checkHealth() {
        // For Feign clients, we can implement a simple health check
        // by calling a health endpoint if available
        try {
            // This is a simplified health check
            // In a real implementation, you might call a specific health endpoint
            return true;
        } catch (Exception e) {
            logger.error("Health check failed for Feign client", e);
            return false;
        }
    }

    public T getUnderlyingClient() {
        return feignClient;
    }
}
