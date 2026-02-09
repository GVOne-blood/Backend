package com.theblood.springfood.client.config;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

/**
 * Application context initializer to load client-config.yml if present.
 * This allows for optional external configuration file loading.
 */
public class ClientConfigurationImporter implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        Resource resource = new ClassPathResource("application.yml");

        if (resource.exists()) {
            try {
                YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
                List<PropertySource<?>> propertySources = loader.load("application", resource);

                for (PropertySource<?> propertySource : propertySources) {
                    environment.getPropertySources().addLast(propertySource);
                }
            } catch (IOException e) {
                // Log warning but don't fail startup if client-config.yml can't be loaded
                System.err.println("Warning: Could not load client-config.yml: " + e.getMessage());
            }
        }
    }
}
