package com.theblood.springfood.media.domain.enums;

import lombok.Getter;

/**
 * Upload status for media files.
 * Tracks the lifecycle of file upload process.
 */
@Getter
public enum UploadStatus {
    
    /**
     * File upload is in progress
     */
    UPLOADING("UPLOADING", "Đang tải lên"),
    
    /**
     * File uploaded successfully, processing (resize, compress, etc.)
     */
    PROCESSING("PROCESSING", "Đang xử lý"),
    
    /**
     * File upload and processing completed successfully
     */
    COMPLETED("COMPLETED", "Hoàn thành"),
    
    /**
     * File upload failed
     */
    FAILED("FAILED", "Thất bại"),
    
    /**
     * File upload cancelled by user
     */
    CANCELLED("CANCELLED", "Đã hủy"),
    
    /**
     * File is being validated (virus scan, content check, etc.)
     */
    VALIDATING("VALIDATING", "Đang kiểm tra"),
    
    /**
     * File validation failed (virus detected, invalid content, etc.)
     */
    REJECTED("REJECTED", "Bị từ chối");
    
    private final String code;
    private final String description;
    
    UploadStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * Get UploadStatus from string code.
     * 
     * @param code the code string (e.g., "UPLOADING", "COMPLETED")
     * @return UploadStatus enum value
     * @throws IllegalArgumentException if code is unknown
     */
    public static UploadStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return UPLOADING;
        }
        
        for (UploadStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        
        throw new IllegalArgumentException("Unknown UploadStatus code: " + code);
    }
    
    /**
     * Check if upload is in progress (not final state).
     * 
     * @return true if status is UPLOADING, PROCESSING, or VALIDATING
     */
    public boolean isInProgress() {
        return this == UPLOADING || this == PROCESSING || this == VALIDATING;
    }
    
    /**
     * Check if upload is completed successfully.
     * 
     * @return true if status is COMPLETED
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }
    
    /**
     * Check if upload has failed or been rejected.
     * 
     * @return true if status is FAILED, CANCELLED, or REJECTED
     */
    public boolean isFailed() {
        return this == FAILED || this == CANCELLED || this == REJECTED;
    }
    
    /**
     * Check if upload is in final state (cannot be changed).
     * 
     * @return true if status is COMPLETED, FAILED, CANCELLED, or REJECTED
     */
    public boolean isFinalState() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == REJECTED;
    }
}
