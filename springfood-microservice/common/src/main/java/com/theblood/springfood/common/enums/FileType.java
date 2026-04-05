package com.theblood.springfood.common.enums;

import lombok.Getter;

/**
 * Unified file type enum for all services.
 * Consolidates AttachmentType, FileCategory, and other file type definitions.
 * 
 * <p>Supported types:</p>
 * <ul>
 *   <li><b>IMAGE</b> - Image files (jpg, png, gif, webp, bmp, svg)</li>
 *   <li><b>VIDEO</b> - Video files (mp4, mov, avi, mkv, webm)</li>
 *   <li><b>AUDIO</b> - Audio files (mp3, wav, ogg, m4a, aac, flac)</li>
 *   <li><b>DOCUMENT</b> - Document files (pdf, doc, docx, xls, xlsx, ppt, pptx, txt)</li>
 *   <li><b>OTHER</b> - Other file types</li>
 * </ul>
 */
@Getter
public enum FileType {
    
    IMAGE(
        "IMAGE", 
        "Image", 
        "image/*", 
        new String[]{"jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "ico", "tiff"}
    ),
    
    VIDEO(
        "VIDEO", 
        "Video", 
        "video/*", 
        new String[]{"mp4", "mov", "avi", "mkv", "webm", "flv", "wmv", "m4v"}
    ),
    
    AUDIO(
        "AUDIO", 
        "Audio", 
        "audio/*", 
        new String[]{"mp3", "wav", "ogg", "m4a", "aac", "flac", "wma", "opus"}
    ),
    
    DOCUMENT(
        "DOCUMENT", 
        "Document", 
        "application/*", 
        new String[]{
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", 
            "txt", "rtf", "odt", "ods", "odp", "csv"
        }
    ),
    
    OTHER(
        "OTHER", 
        "Other", 
        "*/*", 
        new String[]{}
    );
    
    private final String code;
    private final String displayName;
    private final String mimeTypePrefix;
    private final String[] extensions;
    
    FileType(String code, String displayName, String mimeTypePrefix, String[] extensions) {
        this.code = code;
        this.displayName = displayName;
        this.mimeTypePrefix = mimeTypePrefix;
        this.extensions = extensions;
    }
    
    /**
     * Check if this file type is a media type (image, video, or audio).
     * 
     * @return true if media type, false otherwise
     */
    public boolean isMedia() {
        return this == IMAGE || this == VIDEO || this == AUDIO;
    }
    
    /**
     * Check if this file type is a document.
     * 
     * @return true if document type, false otherwise
     */
    public boolean isDocument() {
        return this == DOCUMENT;
    }
    
    /**
     * Get FileType from string code.
     * 
     * @param code the code string (e.g., "IMAGE", "VIDEO")
     * @return FileType enum value
     * @throws IllegalArgumentException if code is unknown
     */
    public static FileType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return OTHER;
        }
        
        for (FileType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("Unknown FileType code: " + code);
    }
    
    /**
     * Detect FileType from MIME type.
     * 
     * @param mimeType the MIME type (e.g., "image/jpeg", "application/pdf")
     * @return detected FileType
     */
    public static FileType fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return OTHER;
        }
        
        String lowerMimeType = mimeType.toLowerCase();
        
        if (lowerMimeType.startsWith("image/")) {
            return IMAGE;
        } else if (lowerMimeType.startsWith("video/")) {
            return VIDEO;
        } else if (lowerMimeType.startsWith("audio/")) {
            return AUDIO;
        } else if (isDocumentMimeType(lowerMimeType)) {
            return DOCUMENT;
        }
        
        return OTHER;
    }
    
    /**
     * Detect FileType from file extension.
     * 
     * @param extension the file extension (with or without dot, e.g., "jpg" or ".jpg")
     * @return detected FileType
     */
    public static FileType fromExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return OTHER;
        }
        
        String lowerExt = extension.toLowerCase().replace(".", "");
        
        for (FileType type : values()) {
            for (String ext : type.extensions) {
                if (ext.equals(lowerExt)) {
                    return type;
                }
            }
        }
        
        return OTHER;
    }
    
    /**
     * Detect FileType from filename.
     * 
     * @param filename the filename (e.g., "document.pdf", "image.jpg")
     * @return detected FileType
     */
    public static FileType fromFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return OTHER;
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            String extension = filename.substring(lastDotIndex + 1);
            return fromExtension(extension);
        }
        
        return OTHER;
    }
    
    /**
     * Check if a file extension is supported by this FileType.
     * 
     * @param extension the file extension to check
     * @return true if supported, false otherwise
     */
    public boolean supportsExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return false;
        }
        
        String lowerExt = extension.toLowerCase().replace(".", "");
        
        for (String ext : this.extensions) {
            if (ext.equals(lowerExt)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Helper method to check if MIME type is a document type.
     */
    private static boolean isDocumentMimeType(String mimeType) {
        return mimeType.startsWith("application/pdf") ||
               mimeType.contains("document") ||
               mimeType.contains("spreadsheet") ||
               mimeType.contains("presentation") ||
               mimeType.contains("msword") ||
               mimeType.contains("ms-excel") ||
               mimeType.contains("ms-powerpoint") ||
               mimeType.contains("officedocument") ||
               mimeType.contains("opendocument") ||
               mimeType.startsWith("text/plain") ||
               mimeType.startsWith("text/csv") ||
               mimeType.startsWith("text/rtf");
    }
}
