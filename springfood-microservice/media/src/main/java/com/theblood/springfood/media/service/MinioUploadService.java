package com.theblood.springfood.media.service;

import com.theblood.minio.core.impl.MinIOClientCustomImpl;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * MinIO Upload Service - Đơn giản và tối ưu
 * <p>
 * MinIO SDK tự động xử lý multipart upload khi bạn set partSize.
 * Nó sẽ tự động:
 * - Chia file thành parts theo partSize
 * - Upload parallel các parts
 * - Tự động retry nếu fail
 * - Compose lại thành file hoàn chỉnh
 * <p>
 * Bạn chỉ cần gọi putObject() là xong!
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioUploadService {

    private final MinIOClientCustomImpl minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${media.upload.part-size:10485760}") // 10MB default - MinIO sẽ tự động parallel upload
    private long partSize;

    /**
     * Upload file - MinIO tự động xử lý multipart nếu file > partSize
     */
    public String uploadFile(InputStream inputStream, String bucketName, String objectName,
                             long fileSize, String contentType) throws Exception {

        log.info("Uploading file: {} (size: {} MB)", objectName, fileSize / (1024 * 1024));

        // MinIO SDK tự động:
        // - Nếu file < partSize: upload trực tiếp
        // - Nếu file > partSize: tự động chia parts và upload parallel
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream, fileSize, partSize) // partSize = -1 để MinIO tự quyết định
                .contentType(contentType)
                .build()
        );

        log.info("File uploaded successfully: {}", objectName);
        return objectName;
    }


}
