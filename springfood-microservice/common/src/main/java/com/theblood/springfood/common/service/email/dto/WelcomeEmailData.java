package com.theblood.springfood.common.service.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for welcome email template data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WelcomeEmailData {
    
    /**
     * User's display name
     */
    private String userName;
    
    /**
     * Account activation URL
     */
    private String activationUrl;
}
