package com.theblood.springfood.chat.domain.enumeration;

/**
 * Status of a participant in a conversation.
 * 
 * <ul>
 *   <li><b>ACTIVE</b> - Currently in the conversation, can send/receive messages</li>
 *   <li><b>LEFT</b> - Left the conversation voluntarily</li>
 *   <li><b>REMOVED</b> - Removed from conversation by admin/owner</li>
 *   <li><b>MUTED</b> - Still in conversation but muted notifications</li>
 * </ul>
 */
public enum ParticipantStatus {
    
    ACTIVE("ACTIVE", "Active", true),
    LEFT("LEFT", "Left", false),
    REMOVED("REMOVED", "Removed", false),
    MUTED("MUTED", "Muted", true);
    
    private final String code;
    private final String displayName;
    private final boolean canReceiveMessages;
    
    ParticipantStatus(String code, String displayName, boolean canReceiveMessages) {
        this.code = code;
        this.displayName = displayName;
        this.canReceiveMessages = canReceiveMessages;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean canReceiveMessages() {
        return canReceiveMessages;
    }
    
    public boolean isActive() {
        return this == ACTIVE || this == MUTED;
    }
    
    public static ParticipantStatus fromCode(String code) {
        for (ParticipantStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ParticipantStatus code: " + code);
    }
}
