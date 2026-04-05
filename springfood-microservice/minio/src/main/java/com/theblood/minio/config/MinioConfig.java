package com.theblood.minio.config;

import com.theblood.minio.core.impl.MinIOClientCustomImpl;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO Auto Configuration
 * This configuration will be automatically loaded when the library is included in a project
 */
@Configuration
@ConditionalOnClass(MinioClient.class)
@ConditionalOnProperty(prefix = "minio", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    private final MinioProperties minioProperties;

    public MinioConfig(MinioProperties minioProperties) {
        this.minioProperties = minioProperties;
    }

    /**
     * Create MinIO client bean with configured properties
     */
    @Bean
    @ConditionalOnMissingBean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    /**
     * Create custom MinIO client implementation
     */
    @Bean
    @ConditionalOnMissingBean
    public MinIOClientCustomImpl minIOClient(MinioClient minioClient) {
        String baseUrl = minioProperties.getBaseUrl() != null
                ? minioProperties.getBaseUrl()
                : minioProperties.getEndpoint();

        return new MinIOClientCustomImpl(
                minioClient,
                minioProperties.getBucket(),
                minioProperties.isAutoCreateBucket(),
                minioProperties.getPartSize(),
                baseUrl
        );
    }
}