package com.theblood.minio.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MinIO Response object
 * Contains information about MinIO operations
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MinIOResponse {
    
    /**
     * Operation success status
     */
    private boolean success;
    
    /**
     * Object key/name in MinIO
     */
    private String objectKey;
    
    /**
     * Bucket name
     */
    private String bucket;
    
    /**
     * Direct URL to access the file
     */
    private String url;
    
    /**
     * Presigned URL for temporary access
     */
    private String presignedUrl;
    
    /**
     * Response message
     */
    private String message;
    
    /**
     * File size in bytes
     */
    private Long size;
    
    /**
     * Content type
     */
    private String contentType;
}
