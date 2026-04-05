package com.theblood.cartservice.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Load .env file into Spring Environment for cart-service
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        System.out.println("=================================================");
        System.out.println("DotenvConfig for CART-SERVICE is being loaded!");
        System.out.println("=================================================");

        File envFile = new File(".env");
        if (!envFile.exists()) {
            System.out.println("Warning: .env file not found in current directory");
            return;
        }

        Map<String, Object> envMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int separatorIndex = line.indexOf('=');
                if (separatorIndex > 0) {
                    String key = line.substring(0, separatorIndex).trim();
                    String value = line.substring(separatorIndex + 1).trim();
                    envMap.put(key, value);
                }
            }

            environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", envMap));
            System.out.println("✓ Successfully loaded " + envMap.size() + " properties from .env file");
            System.out.println("=================================================");

        } catch (Exception e) {
            System.err.println("Error loading .env file: " + e.getMessage());
        }
    }
}
