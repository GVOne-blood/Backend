# 🚀 Quick Start Guide

Hướng dẫn nhanh để bắt đầu sử dụng MinIO Library trong 5 phút.

## Bước 1: Setup MinIO Server

```bash
# Chạy MinIO với Docker
docker run -d \
  -p 9000:9000 \
  -p 9001:9001 \
  --name minio \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  quay.io/minio/minio server /data --console-address ":9001"
```

## Bước 2: Build Library

```bash
# Trong thư mục library này
mvn clean install
```

## Bước 3: Tạo Project Mới

```bash
# Tạo Spring Boot project
spring init --dependencies=web,lombok my-app
cd my-app
```

## Bước 4: Thêm Dependency

Thêm vào `pom.xml`:

```xml
<dependency>
    <groupId>com.theblood</groupId>
    <artifactId>minio</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Bước 5: Cấu hình

Tạo `application.properties`:

```properties
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket=test-bucket
```

## Bước 6: Tạo Controller

```java
package com.example.myapp;

import com.theblood.minio.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    
    private final MinioService minioService;
    
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        return minioService.upload(file, "uploads");
    }
    
    @GetMapping("/download/{filename}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String filename) {
        var stream = minioService.download("uploads/" + filename);
        return ResponseEntity.ok().body(new InputStreamResource(stream));
    }
}
```

## Bước 7: Chạy và Test

```bash
# Chạy application
mvn spring-boot:run

# Test upload (terminal khác)
curl -X POST http://localhost:8080/files/upload \
  -F "file=@test.txt"

# Test download
curl -X GET http://localhost:8080/files/download/test.txt \
  --output downloaded.txt
```

## 🎉 Xong!

Bạn đã có một ứng dụng upload/download file với MinIO!

## 📚 Tiếp theo

- Xem [README.md](README.md) để biết thêm tính năng
- Xem [README-USAGE.md](README-USAGE.md) để biết cách sử dụng nâng cao
- Xem [TESTING.md](TESTING.md) để biết cách test

## 💡 Tips

1. **Sử dụng unique filename** để tránh ghi đè:
   ```java
   minioService.uploadWithUniqueFilename(file, "uploads");
   ```

2. **Tạo temporary URL** thay vì download trực tiếp:
   ```java
   String url = minioService.getTemporaryUrl("uploads/file.txt");
   ```

3. **Upload nhiều file cùng lúc**:
   ```java
   String[] urls = minioService.uploadMultiple(files, "uploads");
   ```

4. **Sử dụng environment variables** cho production:
   ```properties
   minio.access-key=${MINIO_ACCESS_KEY}
   minio.secret-key=${MINIO_SECRET_KEY}
   ```
