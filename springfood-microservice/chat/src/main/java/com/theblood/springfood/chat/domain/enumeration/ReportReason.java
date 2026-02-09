package com.theblood.springfood.chat.domain.enumeration;

/**
 * Reason for reporting a message.
 * 
 * <ul>
 *   <li><b>SPAM</b> - Spam or advertising</li>
 *   <li><b>HARASSMENT</b> - Harassment or bullying</li>
 *   <li><b>INAPPROPRIATE_CONTENT</b> - Inappropriate or offensive content</li>
 *   <li><b>SCAM</b> - Scam or fraud attempt</li>
 *   <li><b>OTHER</b> - Other reason</li>
 * </ul>
 */
public enum ReportReason {
    
    SPAM("SPAM", "Spam", "Message contains spam or advertising"),
    HARASSMENT("HARASSMENT", "Harassment", "Harassment, bullying, or threatening behavior"),
    INAPPROPRIATE_CONTENT("INAPPROPRIATE_CONTENT", "Inappropriate Content", "Offensive, violent, or adult content"),
    SCAM("SCAM", "Scam", "Scam or fraud attempt"),
    OTHER("OTHER", "Other", "Other reason not listed");
    
    private final String code;
    private final String displayName;
    private final String description;
    
    ReportReason(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean requiresDetails() {
        return this == OTHER;
    }
    
    public static ReportReason fromCode(String code) {
        for (ReportReason reason : values()) {
            if (reason.code.equals(code)) {
                return reason;
            }
        }
        throw new IllegalArgumentException("Unknown ReportReason code: " + code);
    }
}
