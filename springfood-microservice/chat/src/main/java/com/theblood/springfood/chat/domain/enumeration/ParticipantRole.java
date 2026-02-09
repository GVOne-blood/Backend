package com.theblood.springfood.chat.domain.enumeration;

/**
 * Role of participant in a conversation.
 * 
 * <ul>
 *   <li><b>OWNER</b> - Creator of group, has full control (can delete, transfer ownership)</li>
 *   <li><b>ADMIN</b> - Can manage members, edit settings, delete messages</li>
 *   <li><b>MEMBER</b> - Regular participant, can only chat</li>
 * </ul>
 */
public enum ParticipantRole {
    
    OWNER("OWNER", "Owner", 100),
    ADMIN("ADMIN", "Admin", 50),
    MEMBER("MEMBER", "Member", 10);
    
    private final String code;
    private final String displayName;
    private final int priority; // Higher = more permissions
    
    ParticipantRole(String code, String displayName, int priority) {
        this.code = code;
        this.displayName = displayName;
        this.priority = priority;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public boolean hasHigherOrEqualPriority(ParticipantRole other) {
        return this.priority >= other.priority;
    }
    
    public static ParticipantRole fromCode(String code) {
        for (ParticipantRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown ParticipantRole code: " + code);
    }
}
