package com.theblood.springfood.common.service.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for product status update email template data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusUpdateEmailData {
    
    /**
     * Shop name
     */
    private String shopName;
    
    /**
     * Product name
     */
    private String productName;
    
    /**
     * Product code
     */
    private String productCode;
    
    /**
     * Product category
     */
    private String category;
    
    /**
     * Product price
     */
    private BigDecimal price;
    
    /**
     * Submitted date (e.g., "24/02/2026")
     */
    private String submittedDate;
    
    /**
     * Status: "APPROVED" or "REJECTED"
     */
    private String status;
    
    /**
     * Status text for display (e.g., "Đã duyệt", "Từ chối")
     */
    private String statusText;
    
    /**
     * Rejection reason (only if status is REJECTED)
     */
    private String rejectionReason;
    
    /**
     * Product detail URL
     */
    private String productUrl;
}
