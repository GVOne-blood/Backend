package com.theblood.springfood.common.service.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for shop new order email template data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopNewOrderEmailData {
    
    /**
     * Shop name
     */
    private String shopName;
    
    /**
     * Order ID
     */
    private String orderId;
    
    /**
     * Order date (e.g., "24/02/2026 10:30")
     */
    private String orderDate;
    
    /**
     * Customer name
     */
    private String customerName;
    
    /**
     * Customer phone number
     */
    private String customerPhone;
    
    /**
     * Shipping address
     */
    private String shippingAddress;
    
    /**
     * Total amount
     */
    private BigDecimal totalAmount;
    
    /**
     * Payment method (e.g., "COD", "Banking")
     */
    private String paymentMethod;
    
    /**
     * Order detail URL
     */
    private String orderUrl;
}
