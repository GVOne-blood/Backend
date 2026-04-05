package com.theblood.springfood.common.service.email;

import com.theblood.springfood.common.service.email.dto.EmailDTO;

/**
 * Centralized Email Service interface for all SpringFood services.
 * 
 * Provides methods for:
 * - Sending plain text emails
 * - Sending HTML emails
 * - Sending template-based emails (with Thymeleaf)
 * 
 * All methods are async by default for better performance.
 */
public interface EmailService {
    
    /**
     * Send a plain text email.
     * 
     * @param emailDTO Email data including recipient, subject, and content
     */
    void sendEmail(EmailDTO emailDTO);
    
    /**
     * Send an HTML email.
     * 
     * @param emailDTO Email data with HTML content
     */
    void sendHtmlEmail(EmailDTO emailDTO);
    
    /**
     * Send an email using a Thymeleaf template.
     * 
     * @param templateName Name of the template file (without .html extension)
     * @param emailDTO Email data (to, subject)
     * @param context Data object to be passed to the template
     */
    void sendTemplateEmail(String templateName, EmailDTO emailDTO, Object context);
}
