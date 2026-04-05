package com.theblood.springfood.common.service.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for order confirmation email template data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmationEmailData {
    
    /**
     * Order ID (e.g., "2602208D4A10BQ")
     */
    private String orderId;
    
    /**
     * Customer name
     */
    private String customerName;
    
    /**
     * Delivery date (e.g., "24/02/2026")
     */
    private String deliveryDate;
    
    /**
     * Order date (e.g., "20/02/2026 19:16:22")
     */
    private String orderDate;
    
    /**
     * Seller name
     */
    private String sellerName;
    
    /**
     * List of order items
     */
    private List<OrderItem> items;
    
    /**
     * Subtotal amount
     */
    private BigDecimal subtotal;
    
    /**
     * Voucher discount amount
     */
    private BigDecimal voucherDiscount;
    
    /**
     * Promo code
     */
    private String promoCode;
    
    /**
     * Shipping fee
     */
    private BigDecimal shippingFee;
    
    /**
     * Total amount
     */
    private BigDecimal totalAmount;
    
    /**
     * Order detail URL
     */
    private String orderUrl;
    
    /**
     * Return request URL
     */
    private String returnUrl;
    
    /**
     * Order item details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        /**
         * Product name
         */
        private String name;
        
        /**
         * Product color/variant
         */
        private String color;
        
        /**
         * Quantity
         */
        private Integer quantity;
        
        /**
         * Item price
         */
        private BigDecimal price;
    }
}
