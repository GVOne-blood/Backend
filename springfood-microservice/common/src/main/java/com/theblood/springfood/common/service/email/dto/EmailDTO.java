package com.theblood.springfood.common.service.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for email messages.
 * 
 * Usage:
 * <pre>
 * EmailDTO email = EmailDTO.builder()
 *     .to("user@example.com")
 *     .subject("Welcome to SpringFood")
 *     .content("Hello, welcome!")
 *     .isHtml(true)
 *     .build();
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDTO {
    
    /**
     * Recipient email address (required)
     */
    private String to;
    
    /**
     * CC recipients (optional)
     */
    private String[] cc;
    
    /**
     * Email subject (required)
     */
    private String subject;
    
    /**
     * Email content/body (required)
     */
    private String content;
    
    /**
     * Whether content is HTML (default: false)
     */
    private boolean isHtml;
}
