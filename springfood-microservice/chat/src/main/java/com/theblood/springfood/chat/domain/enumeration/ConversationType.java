package com.theblood.springfood.chat.domain.enumeration;

/**
 * Type of conversation in chat system.
 * 
 * <ul>
 *   <li><b>DIRECT</b> - 1-1 chat between 2 users (Buyer ↔ Seller)</li>
 *   <li><b>GROUP</b> - Group chat with multiple participants</li>
 *   <li><b>ORDER_SUPPORT</b> - Chat related to a specific order</li>
 *   <li><b>SHOP_SUPPORT</b> - Customer support for a shop</li>
 * </ul>
 */
public enum ConversationType {
    
    DIRECT("DIRECT", "Direct Message"),
    GROUP("GROUP", "Group Chat"),
    ORDER_SUPPORT("ORDER_SUPPORT", "Order Support"),
    SHOP_SUPPORT("SHOP_SUPPORT", "Shop Support");
    
    private final String code;
    private final String displayName;
    
    ConversationType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static ConversationType fromCode(String code) {
        for (ConversationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ConversationType code: " + code);
    }
}
