package com.theblood.springfood.client.autoconf;

import com.theblood.springfood.client.api.*;
import com.theblood.springfood.client.autoconf.feign.FeignClientAutoConfiguration;
import com.theblood.springfood.client.config.ClientProperties;
import com.theblood.springfood.client.service.LoggingService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

/**
 * Main auto-configuration for microservice clients
 * This configuration sets up the client factory and protocol selection
 */
@Configuration
@AutoConfiguration
@ConditionalOnProperty(prefix = "springfood.client", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ClientProperties.class)
@Import({FeignClientAutoConfiguration.class})
public class ClientConfiguration {

    @Autowired(required = false)
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired(required = false)
    private RetryRegistry retryRegistry;

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    @Bean
    @ConditionalOnMissingBean
    public ClientFactory clientFactory(ApplicationContext applicationContext,
                                       ClientProperties clientProperties,
                                       @Autowired(required = false) DefaultClientFactory.FeignClientBuilder feignClientBuilder,
                                       @Autowired(required = false) DefaultClientFactory.GrpcClientBuilder grpcClientBuilder) {
        return new DefaultClientFactory(applicationContext, clientProperties, feignClientBuilder, grpcClientBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientRegistry clientRegistry() {
        return new ClientRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProtocolSelector protocolSelector(ClientProperties clientProperties) {
        return new DefaultProtocolSelector(clientProperties);
    }

    /**
     * Auto-configure PartyMemberClient bean
     * The actual implementation (Feign or gRPC) is determined by configuration
     */
    @Bean
    @Lazy
    @ConditionalOnMissingBean
    public AuthenticationClient authenticationClient(ClientFactory clientFactory) {
        return clientFactory.getClient(AuthenticationClient.class);
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean
    public CategoryClient categoryClient(ClientFactory clientFactory) {
        return clientFactory.getClient(CategoryClient.class);
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean
    public LogActionsClient logActionsClient(ClientFactory clientFactory) {
        return clientFactory.getClient(LogActionsClient.class);
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean
    public LogActionAnnualUpdateClient logActionAnnualUpdateClient(ClientFactory clientFactory) {
        return clientFactory.getClient(LogActionAnnualUpdateClient.class);
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean
    public AccLoginLogClient accLoginLogClient(ClientFactory clientFactory) {
        return clientFactory.getClient(AccLoginLogClient.class);
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean
    public LoggingService loggingService(LogActionsClient logActionsClient, LogActionAnnualUpdateClient logActionAnnualUpdateClient) {
        return new LoggingService(logActionsClient, logActionAnnualUpdateClient);
    }

    /**
     * Bean post processor to auto-inject clients based on annotations
     */
    @Bean
    @ConditionalOnMissingBean
    public ClientInjectionBeanPostProcessor clientInjectionBeanPostProcessor(@Lazy ClientFactory clientFactory) {
        return new ClientInjectionBeanPostProcessor(clientFactory);
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean
    public NotificationClient notificationClient(ClientFactory clientFactory) {
        return clientFactory.getClient(NotificationClient.class);
    }

    /**
     * Protocol selector interface
     */
    public interface ProtocolSelector {
        BaseClient.Protocol selectProtocol(String serviceName);

        BaseClient.Protocol getDefaultProtocol();
    }

    /**
     * Default implementation of protocol selector
     */
    public static class DefaultProtocolSelector implements ProtocolSelector {

        private final ClientProperties clientProperties;

        public DefaultProtocolSelector(ClientProperties clientProperties) {
            this.clientProperties = clientProperties;
        }

        @Override
        public BaseClient.Protocol selectProtocol(String serviceName) {
            // Check service-specific configuration
            ClientProperties.ServiceConfig serviceConfig = clientProperties.getServices().get(serviceName);
            if (serviceConfig != null && serviceConfig.getProtocol() != null) {
                return BaseClient.Protocol.fromValue(serviceConfig.getProtocol());
            }

            // Fall back to default protocol
            return getDefaultProtocol();
        }

        @Override
        public BaseClient.Protocol getDefaultProtocol() {
            return BaseClient.Protocol.fromValue(clientProperties.getDefaultProtocol());
        }
    }
}
