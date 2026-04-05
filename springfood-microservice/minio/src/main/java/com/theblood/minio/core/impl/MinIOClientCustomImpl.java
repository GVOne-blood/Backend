package com.theblood.minio.core.impl;

import com.theblood.minio.core.MinioClientCustom;
import com.theblood.minio.response.MinIOResponse;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO Client Implementation
 * Provides concrete implementation for MinIO file operations
 * Supports multipart upload for large files with configurable part size (default 100MB)
 * The partSize enables parallel/multi-threaded upload for better performance
 */
@Slf4j
public class MinIOClientCustomImpl extends MinioClient implements MinioClientCustom {

    private final io.minio.MinioClient minioClient;
    private final String defaultBucket;
    private final boolean autoCreateBucket;
    private final long partSize;
    private final String baseUrl;

    public MinIOClientCustomImpl(io.minio.MinioClient minioClient, String defaultBucket,
                                 boolean autoCreateBucket, long partSize, String baseUrl) {
        super(minioClient);
        this.minioClient = minioClient;
        this.defaultBucket = defaultBucket;
        this.autoCreateBucket = autoCreateBucket;
        this.partSize = partSize;
        this.baseUrl = baseUrl;

        if (autoCreateBucket) {
            createBucketIfNotExists(defaultBucket);
        }

        log.info("MinIO Client initialized - Bucket: {}, PartSize: {}MB, BaseURL: {}",
                defaultBucket, partSize / (1024 * 1024), baseUrl);
    }

    /**
     * Create bucket if it doesn't exist
     */
    private void createBucketIfNotExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Bucket '{}' created successfully", bucketName);
            } else {
                log.info("Bucket '{}' already exists", bucketName);
            }
        } catch (Exception e) {
            log.error("Error creating bucket: {}", bucketName, e);
            throw new RuntimeException("Failed to create bucket: " + bucketName, e);
        }
    }

    @Override
    public MinIOResponse upload(ByteArrayOutputStream outputStream, String objectKey) {
        try {
            byte[] bytes = outputStream.toByteArray();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

            // Use partSize for multipart upload
            // MinIO will automatically split into parts and upload in parallel for large files
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectKey)
                    .stream(inputStream, bytes.length, partSize)
                    .build());

            log.info("File uploaded successfully: {} (size: {} bytes)", objectKey, bytes.length);

            String fileUrl = String.format("%s/%s/%s", baseUrl, defaultBucket, objectKey);

            return MinIOResponse.builder()
                    .success(true)
                    .objectKey(objectKey)
                    .bucket(defaultBucket)
                    .url(fileUrl)
                    .size((long) bytes.length)
                    .message("File uploaded successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error uploading file: {}", objectKey, e);
            return MinIOResponse.builder()
                    .success(false)
                    .objectKey(objectKey)
                    .message("Failed to upload file: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public String upload(MultipartFile multipartFile, String fileKey) {
        try {
            String contentType = multipartFile.getContentType();
            long fileSize = multipartFile.getSize();

            // Use partSize for multipart upload
            // For files > partSize, MinIO will use parallel upload automatically
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(fileKey)
                    .stream(multipartFile.getInputStream(), fileSize, partSize)
                    .contentType(contentType)
                    .build());

            log.info("MultipartFile uploaded successfully: {} (size: {} bytes, type: {})",
                    fileKey, fileSize, contentType);

            // Return the object URL using configured baseUrl
            return String.format("%s/%s/%s", baseUrl, defaultBucket, fileKey);

        } catch (Exception e) {
            log.error("Error uploading multipart file: {}", fileKey, e);
            throw new RuntimeException("Failed to upload file: " + fileKey, e);
        }
    }

    @Override
    public InputStream getObject(String objectKey) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            log.error("Error getting object: {}", objectKey, e);
            throw new RuntimeException("Failed to get object: " + objectKey, e);
        }
    }

    @Override
    public boolean deleteObject(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectKey)
                    .build());

            log.info("Object deleted successfully: {}", objectKey);
            return true;

        } catch (Exception e) {
            log.error("Error deleting object: {}", objectKey, e);
            return false;
        }
    }

    @Override
    public boolean moveObject(String srcObjectKey, String destObjectKey) {
        try {
            // Copy object to new location
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(destObjectKey)
                    .source(CopySource.builder()
                            .bucket(defaultBucket)
                            .object(srcObjectKey)
                            .build())
                    .build());

            // Delete source object
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(srcObjectKey)
                    .build());

            log.info("Object moved from {} to {}", srcObjectKey, destObjectKey);
            return true;

        } catch (Exception e) {
            log.error("Error moving object from {} to {}", srcObjectKey, destObjectKey, e);
            return false;
        }
    }

    @Override
    public boolean objectExists(String objectKey) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectKey)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getPresignedUrl(String objectKey, int expirySeconds) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(defaultBucket)
                    .object(objectKey)
                    .expiry(expirySeconds, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            log.error("Error generating presigned URL for: {}", objectKey, e);
            throw new RuntimeException("Failed to generate presigned URL: " + objectKey, e);
        }
    }

}
