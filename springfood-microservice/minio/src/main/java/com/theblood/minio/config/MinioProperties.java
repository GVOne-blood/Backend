package com.theblood.minio.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * MinIO configuration properties
 * Configure these properties in your application.properties or application.yml
 * <p>
 * Example:
 * minio.endpoint=http://localhost:9000
 * minio.access-key=minioadmin
 * minio.secret-key=minioadmin
 * minio.bucket=my-bucket
 * minio.secure=false
 * minio.connect-timeout=10000
 * minio.write-timeout=60000
 * minio.read-timeout=10000
 */
@Builder
@Setter
@Getter
@ConfigurationProperties(prefix = "minio")
@Validated
public class MinioProperties {

    /**
     * MinIO server endpoint (e.g., http://localhost:9000)
     */
    @NotBlank(message = "MinIO endpoint is required")
    private String endpoint;

    /**
     * MinIO access key
     */
    @NotBlank(message = "MinIO access key is required")
    private String accessKey;

    /**
     * MinIO secret key
     */
    @NotBlank(message = "MinIO secret key is required")
    private String secretKey;

    /**
     * Default bucket name
     */
    @NotBlank(message = "MinIO bucket is required")
    private String bucket;

    /**
     * Enable HTTPS connection (default: false)
     */
    private boolean secure = false;

    /**
     * Connection timeout in milliseconds (default: 10000)
     */
    private long connectTimeout = 10000;

    /**
     * Write timeout in milliseconds (default: 60000)
     */
    private long writeTimeout = 60000;

    /**
     * Read timeout in milliseconds (default: 10000)
     */
    private long readTimeout = 10000;

    /**
     * Auto create bucket if not exists (default: true)
     */
    private boolean autoCreateBucket = true;

    /**
     * Part size for multipart upload in bytes (default: 100MB)
     * MinIO uses multipart upload for files larger than 5MB
     */
    private long partSize = 100 * 1024 * 1024; // 100MB

    /**
     * Base URL for generating file URLs (optional)
     * If not set, will use endpoint
     */
    private String baseUrl;
}
