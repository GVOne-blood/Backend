package com.theblood.springfood.common.service.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin alert email template data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAlertEmailData {
    
    /**
     * Alert title
     */
    private String title;
    
    /**
     * Alert message (supports HTML)
     */
    private String message;
    
    /**
     * Severity: "critical", "warning", "info"
     */
    private String severity;
    
    /**
     * Severity text for display (e.g., "Critical", "Warning", "Info")
     */
    private String severityText;
    
    /**
     * Alert type (e.g., "System Error", "High Traffic", "Security Alert")
     */
    private String alertType;
    
    /**
     * Timestamp (e.g., "24/02/2026 10:30:45")
     */
    private String timestamp;
    
    /**
     * Affected service name (optional)
     */
    private String affectedService;
    
    /**
     * Error count (optional)
     */
    private Integer errorCount;
    
    /**
     * Detailed information (optional, can be stack trace or logs)
     */
    private String details;
    
    /**
     * Action URL to view details and handle the alert
     */
    private String actionUrl;
}
