package com.theblood.statisticalreport.config;

/**
 * JWT configuration is disabled for statistical-report service.
 * This service does not require authentication — all endpoints are public.
 *
 * The original JWT beans (JwtDecoder, JwtEncoder) have been removed
 * because spring-boot-starter-oauth2-resource-server is excluded via
 * spring.autoconfigure.exclude in application.yml.
 */
public class SecurityJwtConfiguration {
    // Intentionally empty — JWT not used in this service
}
