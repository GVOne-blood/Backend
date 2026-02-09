# Hướng dẫn Test MinIO Library

## 🐳 Cài đặt MinIO bằng Docker

### 1. Chạy MinIO container

```bash
docker run -d \
  -p 9000:9000 \
  -p 9001:9001 \
  --name minio \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  -v minio-data:/data \
  quay.io/minio/minio server /data --console-address ":9001"
```

### 2. Truy cập MinIO Console

- URL: http://localhost:9001
- Username: `minioadmin`
- Password: `minioadmin`

### 3. Tạo bucket (nếu không dùng auto-create)

Vào Console và tạo bucket mới, ví dụ: `my-bucket`

## ⚙️ Cấu hình Test

Tạo file `application-test.properties` trong dự án của bạn:

```properties
# MinIO Test Configuration
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket=test-bucket
minio.auto-create-bucket=true
```

## 🧪 Test với Postman/cURL

### Upload file

```bash
curl -X POST http://localhost:8080/api/files/upload \
  -F "file=@/path/to/your/file.jpg" \
  -F "folder=images"
```

### Download file

```bash
curl -X GET http://localhost:8080/api/files/download/images/file.jpg \
  --output downloaded-file.jpg
```

### Get presigned URL

```bash
curl -X GET "http://localhost:8080/api/files/presigned-url/images/file.jpg?expiry=3600"
```

### Check if file exists

```bash
curl -X GET http://localhost:8080/api/files/exists/images/file.jpg
```

### Delete file

```bash
curl -X DELETE http://localhost:8080/api/files/images/file.jpg
```

### Move/Rename file

```bash
curl -X POST "http://localhost:8080/api/files/move?source=images/file.jpg&destination=archive/file.jpg"
```

## 🔧 Test trong Code

### Unit Test Example

```java
@SpringBootTest
class MinioServiceTest {

    @Autowired
    private MinIOClientImpl minioClient;

    @Test
    void testUploadFile() throws Exception {
        // Create mock MultipartFile
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "Hello MinIO!".getBytes()
        );

        // Upload
        String url = minioClient.upload(file, "test/test.txt");
        
        assertNotNull(url);
        assertTrue(minioClient.objectExists("test/test.txt"));
    }

    @Test
    void testDownloadFile() {
        // Upload first
        // ... upload code ...

        // Download
        InputStream stream = minioClient.getObject("test/test.txt");
        assertNotNull(stream);
    }

    @Test
    void testDeleteFile() {
        // Upload first
        // ... upload code ...

        // Delete
        boolean deleted = minioClient.deleteObject("test/test.txt");
        assertTrue(deleted);
        assertFalse(minioClient.objectExists("test/test.txt"));
    }
}
```

## 🚀 Build và Test Library

### 1. Build library

```bash
mvn clean install
```

### 2. Tạo dự án test mới

```bash
# Tạo Spring Boot project mới
spring init --dependencies=web test-minio-app
cd test-minio-app
```

### 3. Thêm dependency vào pom.xml

```xml
<dependency>
    <groupId>com.theblood</groupId>
    <artifactId>minio</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 4. Cấu hình application.properties

```properties
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket=test-bucket
```

### 5. Tạo test controller

Copy file `MinioExampleController.java` vào dự án của bạn.

### 6. Chạy và test

```bash
mvn spring-boot:run
```

## 📝 Troubleshooting

### Lỗi: Connection refused

- Kiểm tra MinIO container đang chạy: `docker ps`
- Kiểm tra port 9000 có available không

### Lỗi: Access Denied

- Kiểm tra access-key và secret-key
- Kiểm tra bucket policy trong MinIO Console

### Lỗi: Bucket not found

- Bật `minio.auto-create-bucket=true`
- Hoặc tạo bucket thủ công trong Console

### Lỗi: Cannot find MinIOClientImpl bean

- Kiểm tra file `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` có tồn tại
- Kiểm tra dependency đã được add đúng chưa
- Clean và rebuild: `mvn clean install`
