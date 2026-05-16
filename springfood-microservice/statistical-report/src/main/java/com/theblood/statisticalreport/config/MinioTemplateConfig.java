package com.theblood.statisticalreport.config;

import com.theblood.minio.core.impl.MinIOClientCustomImpl;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

/**
 * MinIO configuration for template and report buckets
 * Creates separate MinIO clients for downloading templates and uploading reports
 */
@Configuration
public class MinioTemplateConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.template-bucket:springfood-input}")
    private String templateBucket;

    @Value("${minio.report-bucket:springfood-carbone-out}")
    private String reportBucket;

    @Value("${minio.auto-create-bucket:true}")
    private boolean autoCreateBucket;

    /**
     * MinIO client for template bucket (springfood-input)
     */
    @Bean(name = "templateMinioClient")
    @Primary
    public MinIOClientCustomImpl templateMinioClient() {
        MinioClient minioClient = MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();

        return new MinIOClientCustomImpl(
            minioClient,
            templateBucket,
            autoCreateBucket,
            100 * 1024 * 1024, // 100MB part size
            endpoint
        );
    }

    /**
     * MinIO client for report bucket (springfood-carbone-out)
     */
    @Bean(name = "reportMinioClient")
    public MinIOClientCustomImpl reportMinioClient() {
        MinioClient minioClient = MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();

        return new MinIOClientCustomImpl(
            minioClient,
            reportBucket,
            autoCreateBucket,
            100 * 1024 * 1024, // 100MB part size
            endpoint
        );
    }
}
