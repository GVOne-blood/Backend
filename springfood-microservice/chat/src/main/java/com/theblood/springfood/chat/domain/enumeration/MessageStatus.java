package com.theblood.springfood.chat.domain.enumeration;

/**
 * Delivery status of a message.
 * 
 * <ul>
 *   <li><b>SENDING</b> - Message is being sent to server</li>
 *   <li><b>SENT</b> - Message has been sent to server</li>
 *   <li><b>DELIVERED</b> - Message has been delivered to recipient(s)</li>
 *   <li><b>READ</b> - Message has been read by recipient(s)</li>
 *   <li><b>FAILED</b> - Message failed to send</li>
 * </ul>
 */
public enum MessageStatus {
    
    SENDING("SENDING", "Sending", 1),
    SENT("SENT", "Sent", 2),
    DELIVERED("DELIVERED", "Delivered", 3),
    READ("READ", "Read", 4),
    FAILED("FAILED", "Failed", 0);
    
    private final String code;
    private final String displayName;
    private final int order; // For status progression
    
    MessageStatus(String code, String displayName, int order) {
        this.code = code;
        this.displayName = displayName;
        this.order = order;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getOrder() {
        return order;
    }
    
    public boolean isSuccessful() {
        return this == SENT || this == DELIVERED || this == READ;
    }
    
    public boolean isFailed() {
        return this == FAILED;
    }
    
    public boolean isPending() {
        return this == SENDING;
    }
    
    public boolean canTransitionTo(MessageStatus newStatus) {
        // FAILED can only transition to SENDING (retry)
        if (this == FAILED) {
            return newStatus == SENDING;
        }
        // Status can only progress forward
        return newStatus.order > this.order;
    }
    
    public static MessageStatus fromCode(String code) {
        for (MessageStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown MessageStatus code: " + code);
    }
}
