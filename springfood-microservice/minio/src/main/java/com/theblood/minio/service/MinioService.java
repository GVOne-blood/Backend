package com.theblood.minio.service;

import com.theblood.minio.core.impl.MinIOClientCustomImpl;
import com.theblood.minio.response.MinIOResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * MinIO Service - High-level service for common MinIO operations
 * This service provides convenient methods with additional features
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinIOClientCustomImpl minioClient;

    /**
     * Upload file with auto-generated unique filename
     */
    public String uploadWithUniqueFilename(MultipartFile file, String folder) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String uniqueFilename = UUID.randomUUID().toString() + extension;
        String objectKey = folder.isEmpty() ? uniqueFilename : folder + "/" + uniqueFilename;

        return minioClient.upload(file, objectKey);
    }

    /**
     * Upload file with original filename
     */
    public String upload(MultipartFile file, String folder) {
        String filename = file.getOriginalFilename();
        String objectKey = folder.isEmpty() ? filename : folder + "/" + filename;

        return minioClient.upload(file, objectKey);
    }

    /**
     * Upload from ByteArrayOutputStream
     */
    public MinIOResponse upload(ByteArrayOutputStream outputStream, String objectKey) {
        return minioClient.upload(outputStream, objectKey);
    }

    /**
     * Download file as InputStream
     */
    public InputStream download(String objectKey) {
        return minioClient.getObject(objectKey);
    }

    /**
     * Delete file
     */
    public boolean delete(String objectKey) {
        return minioClient.deleteObject(objectKey);
    }

    /**
     * Check if file exists
     */
    public boolean exists(String objectKey) {
        return minioClient.objectExists(objectKey);
    }

    /**
     * Move/Rename file
     */
    public boolean move(String sourceKey, String destKey) {
        return minioClient.moveObject(sourceKey, destKey);
    }

    /**
     * Get temporary access URL (expires after specified seconds)
     */
    public String getTemporaryUrl(String objectKey, int expirySeconds) {
        return minioClient.getPresignedUrl(objectKey, expirySeconds);
    }

    /**
     * Get temporary access URL with default 1 hour expiry
     */
    public String getTemporaryUrl(String objectKey) {
        return getTemporaryUrl(objectKey, 3600); // 1 hour
    }

    /**
     * Upload multiple files
     */
    public String[] uploadMultiple(MultipartFile[] files, String folder) {
        String[] urls = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            urls[i] = upload(files[i], folder);
        }

        return urls;
    }

    /**
     * Delete multiple files
     */
    public boolean[] deleteMultiple(String[] objectKeys) {
        boolean[] results = new boolean[objectKeys.length];

        for (int i = 0; i < objectKeys.length; i++) {
            results[i] = delete(objectKeys[i]);
        }

        return results;
    }
}
