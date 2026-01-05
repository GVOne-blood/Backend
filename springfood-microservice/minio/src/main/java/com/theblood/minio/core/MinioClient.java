package com.theblood.minio.core;

import com.theblood.minio.response.MinIOResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * MinIO Client Interface
 * Provides methods for file operations with MinIO
 */
public interface MinioClient {

    /**
     * Upload file from ByteArrayOutputStream
     * @param outputStream the output stream containing file data
     * @param objectKey the object key/name in MinIO
     * @return MinIOResponse with upload result
     */
    MinIOResponse upload(ByteArrayOutputStream outputStream, String objectKey);

    /**
     * Upload file from MultipartFile
     * @param multipartFile the multipart file from HTTP request
     * @param fileKey the file key/name in MinIO
     * @return URL of uploaded file
     */
    String upload(MultipartFile multipartFile, String fileKey);

    /**
     * Download/Get object from MinIO
     * @param objectKey the object key/name in MinIO
     * @return InputStream of the file
     */
    InputStream getObject(String objectKey);

    /**
     * Delete object from MinIO
     * @param objectKey the object key/name to delete
     * @return true if deleted successfully
     */
    boolean deleteObject(String objectKey);

    /**
     * Move/Copy object from one location to another
     * @param srcObjectKey source object key
     * @param destObjectKey destination object key
     * @return true if moved successfully
     */
    boolean moveObject(String srcObjectKey, String destObjectKey);

    /**
     * Check if object exists
     * @param objectKey the object key to check
     * @return true if exists
     */
    boolean objectExists(String objectKey);

    /**
     * Get presigned URL for temporary access
     * @param objectKey the object key
     * @param expirySeconds expiry time in seconds
     * @return presigned URL
     */
    String getPresignedUrl(String objectKey, int expirySeconds);
}
