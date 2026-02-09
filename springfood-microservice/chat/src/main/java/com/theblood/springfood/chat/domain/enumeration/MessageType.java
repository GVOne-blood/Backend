package com.theblood.springfood.chat.domain.enumeration;

/**
 * Type of message in chat.
 * 
 * <ul>
 *   <li><b>TEXT</b> - Regular text message</li>
 *   <li><b>IMAGE</b> - Image attachment</li>
 *   <li><b>VIDEO</b> - Video attachment</li>
 *   <li><b>FILE</b> - Document/file attachment</li>
 *   <li><b>AUDIO</b> - Voice message</li>
 *   <li><b>LOCATION</b> - Location sharing</li>
 *   <li><b>STICKER</b> - Sticker/GIF</li>
 *   <li><b>SYSTEM</b> - System message (user joined, left, etc.)</li>
 *   <li><b>ORDER_CARD</b> - Order information card (interactive)</li>
 *   <li><b>PRODUCT_CARD</b> - Product share card (interactive)</li>
 * </ul>
 */
public enum MessageType {
    
    TEXT("TEXT", "Text", false),
    IMAGE("IMAGE", "Image", true),
    VIDEO("VIDEO", "Video", true),
    FILE("FILE", "File", true),
    AUDIO("AUDIO", "Audio", true),
    LOCATION("LOCATION", "Location", false),
    STICKER("STICKER", "Sticker", true),
    SYSTEM("SYSTEM", "System", false),
    ORDER_CARD("ORDER_CARD", "Order Card", false),
    PRODUCT_CARD("PRODUCT_CARD", "Product Card", false);
    
    private final String code;
    private final String displayName;
    private final boolean hasMediaAttachment;
    
    MessageType(String code, String displayName, boolean hasMediaAttachment) {
        this.code = code;
        this.displayName = displayName;
        this.hasMediaAttachment = hasMediaAttachment;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean hasMediaAttachment() {
        return hasMediaAttachment;
    }
    
    public boolean isSystemGenerated() {
        return this == SYSTEM;
    }
    
    public boolean isInteractiveCard() {
        return this == ORDER_CARD || this == PRODUCT_CARD;
    }
    
    public static MessageType fromCode(String code) {
        for (MessageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MessageType code: " + code);
    }
}
