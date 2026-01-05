# MinIO Library - Hướng dẫn sử dụng

Thư viện Java để xử lý upload/download file với MinIO, có thể tái sử dụng cho nhiều dự án Spring Boot.

## 📦 Cài đặt

### 1. Build thư viện

```bash
mvn clean install
```

Sau khi build, file JAR sẽ được tạo trong thư mục `target/` và cài đặt vào local Maven repository.

### 2. Thêm dependency vào dự án của bạn

Thêm vào `pom.xml` của dự án:

```xml
<dependency>
    <groupId>com.theblood</groupId>
    <artifactId>minio</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## ⚙️ Cấu hình

### Cấu hình trong application.properties

```properties
# Bắt buộc
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket=my-bucket

# Tùy chọn (có giá trị mặc định)
minio.secure=false
minio.connect-timeout=10000
minio.write-timeout=60000
minio.read-timeout=10000
minio.auto-create-bucket=true
```

### Cấu hình trong application.yml

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: my-bucket
  secure: false
  connect-timeout: 10000
  write-timeout: 60000
  read-timeout: 10000
  auto-create-bucket: true
```

### Cấu hình theo môi trường

Bạn có thể tạo nhiều file cấu hình cho các môi trường khác nhau:

**application-dev.properties**
```properties
minio.endpoint=http://localhost:9000
minio.access-key=dev-user
minio.secret-key=dev-password
minio.bucket=dev-bucket
```

**application-prod.properties**
```properties
minio.endpoint=https://minio.production.com
minio.access-key=${MINIO_ACCESS_KEY}
minio.secret-key=${MINIO_SECRET_KEY}
minio.bucket=prod-bucket
minio.secure=true
```

## 🚀 Sử dụng

### Inject MinIOClient vào service của bạn

```java
import com.theblood.minio.core.impl.MinIOClientImpl;
import org.springframework.stereotype.Service;

@Service
public class FileService {
    
    private final MinIOClientImpl minioClient;
    
    public FileService(MinIOClientImpl minioClient) {
        this.minioClient = minioClient;
    }
    
    // Sử dụng minioClient để upload/download file
}
```

## 🔧 Tùy chọn nâng cao

### Tắt auto-configuration

Nếu bạn muốn tự cấu hình MinIO client:

```properties
minio.enabled=false
```

### Override MinIO client bean

```java
@Configuration
public class CustomMinioConfig {
    
    @Bean
    @Primary
    public MinIOClientImpl customMinioClient() {
        // Custom implementation
        return new MinIOClientImpl(...);
    }
}
```

## 📝 Các thuộc tính cấu hình

| Thuộc tính | Bắt buộc | Mặc định | Mô tả |
|-----------|----------|----------|-------|
| `minio.endpoint` | ✅ | - | URL của MinIO server |
| `minio.access-key` | ✅ | - | Access key để xác thực |
| `minio.secret-key` | ✅ | - | Secret key để xác thực |
| `minio.bucket` | ✅ | - | Tên bucket mặc định |
| `minio.secure` | ❌ | false | Sử dụng HTTPS |
| `minio.connect-timeout` | ❌ | 10000 | Timeout kết nối (ms) |
| `minio.write-timeout` | ❌ | 60000 | Timeout ghi (ms) |
| `minio.read-timeout` | ❌ | 10000 | Timeout đọc (ms) |
| `minio.auto-create-bucket` | ❌ | true | Tự động tạo bucket nếu chưa tồn tại |
| `minio.part-size` | ❌ | 104857600 | Kích thước part cho multipart upload (bytes) |
| `minio.base-url` | ❌ | endpoint | Base URL để tạo file URL |
| `minio.enabled` | ❌ | true | Bật/tắt auto-configuration |

### Giải thích Part Size

`part-size` là kích thước mỗi phần khi upload file lớn:
- **Mặc định: 100MB** (104857600 bytes)
- MinIO tự động chia file thành nhiều part và upload song song (đa luồng)
- File nhỏ hơn part-size sẽ upload trực tiếp
- File lớn hơn sẽ được chia nhỏ và upload parallel để tăng tốc độ
- **Khuyến nghị**: 50MB-200MB tùy theo network và kích thước file thường dùng

## 🔐 Bảo mật

Không nên hardcode credentials trong code. Sử dụng environment variables:

```properties
minio.access-key=${MINIO_ACCESS_KEY}
minio.secret-key=${MINIO_SECRET_KEY}
```

Hoặc sử dụng Spring Cloud Config, Vault, hoặc các secret management tools khác.
