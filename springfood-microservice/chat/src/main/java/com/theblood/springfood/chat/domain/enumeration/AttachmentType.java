package com.theblood.springfood.chat.domain.enumeration;

import com.theblood.springfood.common.enums.FileType;

/**
 * @deprecated Use {@link FileType} from common module instead.
 * This class is kept for backward compatibility only.
 * 
 * Type of attachment in a message.
 * All methods now delegate to FileType.
 */
@Deprecated(since = "2.0", forRemoval = true)
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
        return toFileType().isMedia();
    }
    
    /**
     * Convert to FileType enum
     */
    public FileType toFileType() {
        return FileType.valueOf(this.name());
    }
    
    /**
     * Create from FileType enum
     */
    public static AttachmentType fromFileType(FileType fileType) {
        return AttachmentType.valueOf(fileType.name());
    }
    
    public static AttachmentType fromCode(String code) {
        FileType fileType = FileType.fromCode(code);
        return fromFileType(fileType);
    }
    
    public static AttachmentType fromMimeType(String mimeType) {
        FileType fileType = FileType.fromMimeType(mimeType);
        return fromFileType(fileType);
    }
    
    public static AttachmentType fromExtension(String extension) {
        FileType fileType = FileType.fromExtension(extension);
        return fromFileType(fileType);
    }
}
