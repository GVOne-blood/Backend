package com.theblood.springfood.common.config.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Centralized Email configuration properties for all SpringFood services.
 * Uses Brevo (formerly Sendinblue) API for email delivery.
 * 
 * Usage in application.yml:
 * <pre>
 * springfood:
 *   email:
 *     enabled: true
 *     from: noreply@springfood.com
 *     brevo:
 *       api-key: ${BREVO_API_KEY}
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "springfood.email")
public class EmailProperties {
    
    /**
     * Enable/disable email functionality
     */
    private boolean enabled = false;
    
    /**
     * Default sender email address
     */
    private String from = "thereisnogod256@gmail.com";
    
    /**
     * Default sender name
     */
    private String fromName = "SpringFood";
    
    /**
     * Reply-to email address
     */
    private String replyTo;
    
    /**
     * Brevo API configuration
     */
    private Brevo brevo = new Brevo();
    
    /**
     * Template configuration
     */
    private Template template = new Template();
    
    @Data
    public static class Brevo {
        /**
         * Brevo API Key
         */
        private String apiKey;
        
        /**
         * Brevo API URL
         */
        private String apiUrl = "https://api.brevo.com/v3";
    }
    
    @Data
    public static class Template {
        private String basePath = "classpath:/templates/email/";
        private String defaultLocale = "vi_VN";
    }
}
