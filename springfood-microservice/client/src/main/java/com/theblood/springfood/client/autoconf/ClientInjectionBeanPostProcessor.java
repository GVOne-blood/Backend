package com.theblood.springfood.client.autoconf;

import com.theblood.springfood.client.api.BaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.*;
import java.lang.reflect.Field;

/**
 * Bean post processor to automatically inject client instances
 */
public class ClientInjectionBeanPostProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ClientInjectionBeanPostProcessor.class);

    private final ClientFactory clientFactory;

    public ClientInjectionBeanPostProcessor(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();

        ReflectionUtils.doWithFields(clazz, field -> {
            if (field.isAnnotationPresent(InjectClient.class)) {
                processClientInjection(bean, field);
            }
        });

        return bean;
    }

    private void processClientInjection(Object bean, Field field) {
        InjectClient annotation = field.getAnnotation(InjectClient.class);
        Class<?> fieldType = field.getType();

        if (!BaseClient.class.isAssignableFrom(fieldType)) {
            logger.warn("Field {} in {} is annotated with @InjectClient but does not extend BaseClient",
                    field.getName(), bean.getClass().getName());
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Class<? extends BaseClient> clientInterface = (Class<? extends BaseClient>) fieldType;

            BaseClient client;
            if (annotation.protocol().isEmpty()) {
                client = clientFactory.getClient(clientInterface);
            } else {
                BaseClient.Protocol protocol = BaseClient.Protocol.fromValue(annotation.protocol());
                client = clientFactory.getClient(clientInterface, protocol);
            }

            ReflectionUtils.makeAccessible(field);
            field.set(bean, client);

            logger.debug("Injected {} client into {}.{}",
                    clientInterface.getSimpleName(), bean.getClass().getSimpleName(), field.getName());

        } catch (Exception e) {
            logger.error("Failed to inject client into field {} in {}",
                    field.getName(), bean.getClass().getName(), e);
            throw new RuntimeException("Failed to inject client", e);
        }
    }

    /**
     * Annotation for automatic client injection
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface InjectClient {
        /**
         * The protocol to use (optional, defaults to configured protocol)
         */
        String protocol() default "";
    }
}
