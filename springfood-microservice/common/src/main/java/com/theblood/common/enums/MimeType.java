package com.theblood.common.enums;

import lombok.Getter;

/**
 * MIME Type enum - Định danh loại file theo chuẩn IANA
 */
@Getter
public enum MimeType {

    // ========== IMAGE TYPES ==========
    JPEG("image/jpeg", "jpg", "jpeg"),
    PNG("image/png", "png"),
    GIF("image/gif", "gif"),
    WEBP("image/webp", "webp"),
    SVG("image/svg+xml", "svg"),
    BMP("image/bmp", "bmp"),
    ICO("image/x-icon", "ico"),
    TIFF("image/tiff", "tiff", "tif"),

    // ========== VIDEO TYPES ==========
    MP4("video/mp4", "mp4"),
    WEBM("video/webm", "webm"),
    MPEG("video/mpeg", "mpeg"),
    MOV("video/quicktime", "mov"),
    AVI("video/x-msvideo", "avi"),
    MKV("video/x-matroska", "mkv"),

    // ========== AUDIO TYPES ==========
    MPEG_AUDIO("audio/mpeg", "mp3"),
    WAVE("audio/wav", "wav"),
    OGG("audio/ogg", "ogg"),
    AAC("audio/aac", "aac"),
    FLAC("audio/flac", "flac"),
    M4A("audio/mp4", "m4a"),

    // ========== DOCUMENT TYPES ==========
    PDF("application/pdf", "pdf"),
    DOC("application/msword", "doc"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
    XLS("application/vnd.ms-excel", "xls"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    PPT("application/vnd.ms-powerpoint", "ppt"),
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx"),
    TXT("text/plain", "txt"),
    CSV("text/csv", "csv"),

    // ========== WEB TYPES ==========
    HTML("text/html", "html", "htm"),
    XML("application/xml", "xml"),
    JSON("application/json", "json"),
    CSS("text/css", "css"),
    JAVASCRIPT("application/javascript", "js"),

    // ========== ARCHIVE TYPES ==========
    ZIP("application/zip", "zip"),
    RAR("application/x-rar-compressed", "rar"),
    GZIP("application/gzip", "gz"),
    TAR("application/x-tar", "tar"),

    // ========== OTHER ==========
    OCTET_STREAM("application/octet-stream", "bin");

    // ========== FIELDS ==========
    private final String mimeType;
    private final String[] extensions;

    MimeType(String mimeType, String... extensions) {
        this.mimeType = mimeType;
        this.extensions = extensions;
    }

    /**
     * Tìm MimeType từ chuỗi MIME (ví dụ: "image/jpeg")
     *
     * @param mimeTypeString ví dụ: "image/jpeg"
     * @return MimeType nếu tìm thấy, null nếu không
     */
    public static MimeType fromMimeString(String mimeTypeString) {
        if (mimeTypeString == null || mimeTypeString.isBlank()) {
            return OCTET_STREAM;
        }

        for (MimeType type : MimeType.values()) {
            if (type.mimeType.equals(mimeTypeString)) {
                return type;
            }
        }

        return OCTET_STREAM; // Default
    }

    /**
     * Tìm MimeType từ file extension (ví dụ: "jpg")
     *
     * @param extension ví dụ: "jpg" hoặc ".jpg"
     * @return MimeType nếu tìm thấy, null nếu không
     */
    public static MimeType fromExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return OCTET_STREAM;
        }

        // Remove dot if present
        String ext = extension.startsWith(".")
                ? extension.substring(1)
                : extension;
        ext = ext.toLowerCase();

        for (MimeType type : MimeType.values()) {
            for (String typeExt : type.extensions) {
                if (typeExt.equals(ext)) {
                    return type;
                }
            }
        }

        return OCTET_STREAM;
    }

    /**
     * Kiểm tra có phải loại image không
     */
    public boolean isImage() {
        return this.mimeType.startsWith("image/");
    }

    /**
     * Kiểm tra có phải loại video không
     */
    public boolean isVideo() {
        return this.mimeType.startsWith("video/");
    }

    /**
     * Kiểm tra có phải loại audio không
     */
    public boolean isAudio() {
        return this.mimeType.startsWith("audio/");
    }

    /**
     * Kiểm tra có phải loại document không
     */
    public boolean isDocument() {
        return this.mimeType.startsWith("application/") ||
                this.mimeType.startsWith("text/");
    }
}
