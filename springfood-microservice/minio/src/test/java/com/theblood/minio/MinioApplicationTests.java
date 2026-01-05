package com.theblood.minio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic test to verify Spring context loads correctly
 * Uses test profile with dummy MinIO configuration
 */
@SpringBootTest(properties = {
        "minio.endpoint=http://localhost:9000",
        "minio.access-key=test-key",
        "minio.secret-key=test-secret",
        "minio.bucket=test-bucket",
        "minio.auto-create-bucket=false"
})
class MinioApplicationTests {

    @Test
    void contextLoads() {
        // Test that Spring context loads successfully with MinIO configuration
    }

}
