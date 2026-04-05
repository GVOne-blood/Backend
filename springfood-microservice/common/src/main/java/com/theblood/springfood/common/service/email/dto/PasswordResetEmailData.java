package com.theblood.springfood.common.service.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for password reset email template data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetEmailData {
    
    /**
     * User's display name
     */
    private String userName;
    
    /**
     * Reset code (e.g., "A8X9K2")
     */
    private String resetCode;
    
    /**
     * Code expiry time in minutes (e.g., 15)
     */
    private Integer expiryMinutes;
    
    /**
     * Password reset URL
     */
    private String resetUrl;
}
