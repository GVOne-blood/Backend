package com.theblood.springfood.media.domain.enums;

import lombok.Getter;

/**
 * Module/Service that uploaded the file.
 * Used for tracking and organizing files by source.
 */
@Getter
public enum UploadModule {
    
    /**
     * Product service - product images, thumbnails
     */
    PRODUCT("PRODUCT", "Product Service", "Hình ảnh sản phẩm"),
    
    /**
     * Chat service - message attachments, avatars
     */
    CHAT("CHAT", "Chat Service", "Tin nhắn và tệp đính kèm"),
    
    /**
     * Order service - order documents, invoices
     */
    ORDER("ORDER", "Order Service", "Đơn hàng và hóa đơn"),
    
    /**
     * User service - user avatars, profile images
     */
    USER("USER", "User Service", "Ảnh đại diện người dùng"),
    
    /**
     * Shop service - shop logos, banners
     */
    SHOP("SHOP", "Shop Service", "Logo và banner cửa hàng"),
    
    /**
     * Statistical report service - exported reports, charts
     */
    REPORT("REPORT", "Statistical Report Service", "Báo cáo thống kê"),
    
    /**
     * Notification service - notification images, attachments
     */
    NOTIFICATION("NOTIFICATION", "Notification Service", "Thông báo"),
    
    /**
     * Marketing service - banners, promotional images
     */
    MARKETING("MARKETING", "Marketing Service", "Marketing và quảng cáo"),
    
    /**
     * System - system files, configurations
     */
    SYSTEM("SYSTEM", "System", "Hệ thống"),
    
    /**
     * Other/Unknown module
     */
    OTHER("OTHER", "Other", "Khác");
    
    private final String code;
    private final String serviceName;
    private final String description;
    
    UploadModule(String code, String serviceName, String description) {
        this.code = code;
        this.serviceName = serviceName;
        this.description = description;
    }
    
    /**
     * Get UploadModule from string code.
     * 
     * @param code the code string (e.g., "PRODUCT", "CHAT")
     * @return UploadModule enum value
     * @throws IllegalArgumentException if code is unknown
     */
    public static UploadModule fromCode(String code) {
        if (code == null || code.isBlank()) {
            return OTHER;
        }
        
        for (UploadModule module : values()) {
            if (module.code.equalsIgnoreCase(code)) {
                return module;
            }
        }
        
        throw new IllegalArgumentException("Unknown UploadModule code: " + code);
    }
    
    /**
     * Get UploadModule from service name.
     * 
     * @param serviceName the service name (e.g., "Product Service", "Chat Service")
     * @return UploadModule enum value, or OTHER if not found
     */
    public static UploadModule fromServiceName(String serviceName) {
        if (serviceName == null || serviceName.isBlank()) {
            return OTHER;
        }
        
        for (UploadModule module : values()) {
            if (module.serviceName.equalsIgnoreCase(serviceName)) {
                return module;
            }
        }
        
        return OTHER;
    }
    
    /**
     * Check if this module is a core business module.
     * 
     * @return true if module is PRODUCT, CHAT, ORDER, USER, or SHOP
     */
    public boolean isBusinessModule() {
        return this == PRODUCT || this == CHAT || this == ORDER || 
               this == USER || this == SHOP;
    }
    
    /**
     * Check if this module is a support module.
     * 
     * @return true if module is REPORT, NOTIFICATION, or MARKETING
     */
    public boolean isSupportModule() {
        return this == REPORT || this == NOTIFICATION || this == MARKETING;
    }
    
    /**
     * Check if this module is system-related.
     * 
     * @return true if module is SYSTEM
     */
    public boolean isSystemModule() {
        return this == SYSTEM;
    }
}
