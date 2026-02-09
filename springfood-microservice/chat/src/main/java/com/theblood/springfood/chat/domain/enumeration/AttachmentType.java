package com.theblood.springfood.chat.domain.enumeration;

/**
 * Type of attachment in a message.
 * 
 * <ul>
 *   <li><b>IMAGE</b> - Image files (jpg, png, gif, webp)</li>
 *   <li><b>VIDEO</b> - Video files (mp4, mov, avi)</li>
 *   <li><b>AUDIO</b> - Audio files (mp3, wav, voice messages)</li>
 *   <li><b>DOCUMENT</b> - Document files (pdf, doc, xls, ppt)</li>
 *   <li><b>OTHER</b> - Other file types</li>
 * </ul>
 */
public enum AttachmentType {
    
    IMAGE("IMAGE", "Image", "image/*", new String[]{"jpg", "jpeg", "png", "gif", "webp", "bmp"}),
    VIDEO("VIDEO", "Video", "video/*", new String[]{"mp4", "mov", "avi", "mkv", "webm"}),
    AUDIO("AUDIO", "Audio", "audio/*", new String[]{"mp3", "wav", "ogg", "m4a", "aac"}),
    DOCUMENT("DOCUMENT", "Document", "application/*", new String[]{"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"}),
    OTHER("OTHER", "Other", "*/*", new String[]{});
    
    private final String code;
    private final String displayName;
    private final String mimeTypePrefix;
    private final String[] extensions;
    
    AttachmentType(String code, String displayName, String mimeTypePrefix, String[] extensions) {
        this.code = code;
        this.displayName = displayName;
        this.mimeTypePrefix = mimeTypePrefix;
        this.extensions = extensions;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getMimeTypePrefix() {
        return mimeTypePrefix;
    }
    
    public String[] getExtensions() {
        return extensions;
    }
    
    public boolean isMedia() {
        return this == IMAGE || this == VIDEO || this == AUDIO;
    }
    
    public static AttachmentType fromCode(String code) {
        for (AttachmentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AttachmentType code: " + code);
    }
    
    public static AttachmentType fromMimeType(String mimeType) {
        if (mimeType == null) {
            return OTHER;
        }
        String lowerMimeType = mimeType.toLowerCase();
        if (lowerMimeType.startsWith("image/")) {
            return IMAGE;
        } else if (lowerMimeType.startsWith("video/")) {
            return VIDEO;
        } else if (lowerMimeType.startsWith("audio/")) {
            return AUDIO;
        } else if (lowerMimeType.startsWith("application/pdf") ||
                   lowerMimeType.contains("document") ||
                   lowerMimeType.contains("spreadsheet") ||
                   lowerMimeType.contains("presentation") ||
                   lowerMimeType.startsWith("text/")) {
            return DOCUMENT;
        }
        return OTHER;
    }
    
    public static AttachmentType fromExtension(String extension) {
        if (extension == null) {
            return OTHER;
        }
        String lowerExt = extension.toLowerCase().replace(".", "");
        for (AttachmentType type : values()) {
            for (String ext : type.extensions) {
                if (ext.equals(lowerExt)) {
                    return type;
                }
            }
        }
        return OTHER;
    }
}
