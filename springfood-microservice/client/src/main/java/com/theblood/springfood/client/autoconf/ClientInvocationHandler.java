package com.theblood.springfood.client.autoconf;

import com.theblood.springfood.client.api.BaseClient;
import com.theblood.springfood.client.api.ClientMethod;
import com.theblood.springfood.client.config.ClientProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

/**
 * Dynamic proxy handler for client method invocations
 * Handles cross-cutting concerns like resilience, metrics, and context propagation
 */
public class ClientInvocationHandler implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientInvocationHandler.class);

    private final Object target;
    private final String serviceName;
    private final BaseClient.Protocol protocol;
    private final ClientProperties clientProperties;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final MeterRegistry meterRegistry;

    public ClientInvocationHandler(Object target,
                                   String serviceName,
                                   BaseClient.Protocol protocol,
                                   ClientProperties clientProperties,
                                   CircuitBreaker circuitBreaker,
                                   Retry retry,
                                   MeterRegistry meterRegistry) {
        this.target = target;
        this.serviceName = serviceName;
        this.protocol = protocol;
        this.clientProperties = clientProperties;
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Create a proxy instance with the handler
     */
    @SuppressWarnings("unchecked")
    public static <T extends BaseClient> T createProxy(Class<T> clientInterface,
                                                       Object target,
                                                       String serviceName,
                                                       BaseClient.Protocol protocol,
                                                       ClientProperties clientProperties,
                                                       CircuitBreaker circuitBreaker,
                                                       Retry retry,
                                                       MeterRegistry meterRegistry) {
        ClientInvocationHandler handler = new ClientInvocationHandler(
                target, serviceName, protocol, clientProperties,
                circuitBreaker, retry, meterRegistry
        );

        return (T) Proxy.newProxyInstance(
                clientInterface.getClassLoader(),
                new Class<?>[]{clientInterface},
                handler
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Handle Object methods
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(target, args);
        }

        // Handle BaseClient methods
        if (method.getDeclaringClass() == BaseClient.class) {
            return method.invoke(target, args);
        }

        // Get method metadata
        ClientMethod methodAnnotation = method.getAnnotation(ClientMethod.class);
        if (methodAnnotation == null) {
            // If no annotation, just invoke directly
            return method.invoke(target, args);
        }

        String methodName = method.getName();
        boolean includeUserContext = methodAnnotation.includeUserContext();

        // Prepare invocation
        Callable<Object> callable = () -> {
            try {
                // Propagate user context if needed
                if (includeUserContext) {
                    // Context will be propagated by interceptors
                }

                // Log invocation
                if (logger.isDebugEnabled()) {
                    logger.debug("Invoking {}.{} via {} protocol",
                            serviceName, methodName, protocol);
                }

                // Invoke the actual method
                return method.invoke(target, args);

            } catch (Exception e) {
                logger.error("Error invoking {}.{}: {}",
                        serviceName, methodName, e.getMessage());
                throw e;
            }
        };

        // Apply resilience patterns
        if (circuitBreaker != null && clientProperties.getResilience().getCircuitBreaker().isEnabled()) {
            callable = CircuitBreaker.decorateCallable(circuitBreaker, callable);
        }

        if (retry != null && clientProperties.getResilience().getRetry().isEnabled() &&
                (methodAnnotation.idempotent() || isReadMethod(method))) {
            callable = Retry.decorateCallable(retry, callable);
        }

        // Execute with metrics
        if (meterRegistry != null && clientProperties.getObservability().isMetricsEnabled()) {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                Object result = callable.call();
                sample.stop(Timer.builder(clientProperties.getObservability().getMetricsPrefix() + ".client.call")
                        .tag("service", serviceName)
                        .tag("method", methodName)
                        .tag("protocol", protocol.getValue())
                        .tag("status", "success")
                        .register(meterRegistry));
                return result;
            } catch (Exception e) {
                sample.stop(Timer.builder(clientProperties.getObservability().getMetricsPrefix() + ".client.call")
                        .tag("service", serviceName)
                        .tag("method", methodName)
                        .tag("protocol", protocol.getValue())
                        .tag("status", "error")
                        .tag("error", e.getClass().getSimpleName())
                        .register(meterRegistry));
                throw e;
            }
        } else {
            return callable.call();
        }
    }

    private boolean isReadMethod(Method method) {
        String name = method.getName().toLowerCase();
        return name.startsWith("get") || name.startsWith("list") ||
                name.startsWith("find") || name.startsWith("search");
    }
}
