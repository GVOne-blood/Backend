package com.theblood.springfood.chat.domain.enumeration;

/**
 * Online/presence status of a user.
 * 
 * <ul>
 *   <li><b>ONLINE</b> - User is currently active</li>
 *   <li><b>AWAY</b> - User is away (inactive for a while)</li>
 *   <li><b>BUSY</b> - User is busy, do not disturb</li>
 *   <li><b>OFFLINE</b> - User is offline</li>
 * </ul>
 */
public enum PresenceStatus {
    
    ONLINE("ONLINE", "Online", true, "#22c55e"),    // Green
    AWAY("AWAY", "Away", true, "#eab308"),          // Yellow
    BUSY("BUSY", "Busy", true, "#ef4444"),          // Red
    OFFLINE("OFFLINE", "Offline", false, "#9ca3af"); // Gray
    
    private final String code;
    private final String displayName;
    private final boolean isAvailable;
    private final String indicatorColor; // For UI display
    
    PresenceStatus(String code, String displayName, boolean isAvailable, String indicatorColor) {
        this.code = code;
        this.displayName = displayName;
        this.isAvailable = isAvailable;
        this.indicatorColor = indicatorColor;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isAvailable() {
        return isAvailable;
    }
    
    public String getIndicatorColor() {
        return indicatorColor;
    }
    
    public boolean canReceiveNotifications() {
        return this == ONLINE || this == AWAY;
    }
    
    public static PresenceStatus fromCode(String code) {
        for (PresenceStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PresenceStatus code: " + code);
    }
}
