package com.theblood.springfood.common.service.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for notification email template data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEmailData {
    
    /**
     * Notification title
     */
    private String title;
    
    /**
     * Notification body (supports HTML)
     */
    private String body;
    
    /**
     * Optional action URL
     */
    private String actionUrl;
    
    /**
     * Optional action button text (default: "Xem chi tiết")
     */
    private String actionText;
}
