package com.theblood.springfood.chat.domain.enumeration;

/**
 * Status of a message report.
 * 
 * <ul>
 *   <li><b>PENDING</b> - Report is waiting to be reviewed</li>
 *   <li><b>REVIEWED</b> - Report has been reviewed by moderator</li>
 *   <li><b>RESOLVED</b> - Action has been taken on the report</li>
 *   <li><b>DISMISSED</b> - Report was dismissed (not valid)</li>
 * </ul>
 */
public enum ReportStatus {
    
    PENDING("PENDING", "Pending", false),
    REVIEWED("REVIEWED", "Reviewed", false),
    RESOLVED("RESOLVED", "Resolved", true),
    DISMISSED("DISMISSED", "Dismissed", true);
    
    private final String code;
    private final String displayName;
    private final boolean isFinalStatus;
    
    ReportStatus(String code, String displayName, boolean isFinalStatus) {
        this.code = code;
        this.displayName = displayName;
        this.isFinalStatus = isFinalStatus;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isFinalStatus() {
        return isFinalStatus;
    }
    
    public boolean isPending() {
        return this == PENDING;
    }
    
    public boolean canTransitionTo(ReportStatus newStatus) {
        if (this.isFinalStatus) {
            return false; // Cannot change from final status
        }
        if (this == PENDING) {
            return newStatus == REVIEWED || newStatus == RESOLVED || newStatus == DISMISSED;
        }
        if (this == REVIEWED) {
            return newStatus == RESOLVED || newStatus == DISMISSED;
        }
        return false;
    }
    
    public static ReportStatus fromCode(String code) {
        for (ReportStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ReportStatus code: " + code);
    }
}
