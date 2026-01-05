# MinIO Spring Boot Library

Thư viện Java Spring Boot để xử lý upload/download file với MinIO. Thư viện này được thiết kế để tái sử dụng cho nhiều dự án khác nhau với cấu hình linh hoạt.

## ✨ Tính năng

- ✅ Auto-configuration với Spring Boot
- ✅ Cấu hình linh hoạt qua properties/yml
- ✅ Upload file (MultipartFile, ByteArrayOutputStream)
- ✅ Download file
- ✅ Delete file
- ✅ Move/Rename file
- ✅ Check file existence
- ✅ Generate presigned URLs
- ✅ Auto-create bucket
- ✅ Multiple file operations
- ✅ Lombok support

## 📦 Cài đặt

### 1. Build thư viện

```bash
mvn clean install
```

### 2. Thêm dependency vào dự án

```xml
<dependency>
    <groupId>com.theblood</groupId>
    <artifactId>minio</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## ⚙️ Cấu hình

### application.properties

```properties
# Bắt buộc
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket=my-bucket

# Tùy chọn
minio.secure=false
minio.connect-timeout=10000
minio.write-timeout=60000
minio.read-timeout=10000
minio.auto-create-bucket=true
```

### application.yml

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: my-bucket
  secure: false
  auto-create-bucket: true
```

### Cấu hình theo môi trường

```properties
# application-dev.properties
minio.endpoint=http://localhost:9000
minio.access-key=dev-user
minio.secret-key=dev-password

# application-prod.properties
minio.endpoint=https://minio.production.com
minio.access-key=${MINIO_ACCESS_KEY}
minio.secret-key=${MINIO_SECRET_KEY}
minio.secure=true
```

## 🚀 Sử dụng

### Cách 1: Sử dụng MinioService (Recommended)

```java
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    
    private final MinioService minioService;
    
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        // Upload với tên file gốc
        return minioService.upload(file, "uploads");
    }
    
    @PostMapping("/upload-unique")
    public String uploadUnique(@RequestParam("file") MultipartFile file) {
        // Upload với tên file unique (UUID)
        return minioService.uploadWithUniqueFilename(file, "uploads");
    }
    
    @GetMapping("/download/{filename}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String filename) {
        InputStream stream = minioService.download("uploads/" + filename);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(new InputStreamResource(stream));
    }
    
    @DeleteMapping("/{filename}")
    public boolean delete(@PathVariable String filename) {
        return minioService.delete("uploads/" + filename);
    }
    
    @GetMapping("/url/{filename}")
    public String getUrl(@PathVariable String filename) {
        // URL có hiệu lực trong 1 giờ
        return minioService.getTemporaryUrl("uploads/" + filename);
    }
}
```

### Cách 2: Sử dụng MinIOClientImpl trực tiếp

```java
@Service
@RequiredArgsConstructor
public class FileService {
    
    private final MinIOClientImpl minioClient;
    
    public String uploadFile(MultipartFile file) {
        return minioClient.upload(file, "folder/" + file.getOriginalFilename());
    }
    
    public InputStream downloadFile(String objectKey) {
        return minioClient.getObject(objectKey);
    }
    
    public boolean deleteFile(String objectKey) {
        return minioClient.deleteObject(objectKey);
    }
    
    public String getPresignedUrl(String objectKey) {
        return minioClient.getPresignedUrl(objectKey, 3600);
    }
}
```

### Upload nhiều file

```java
@PostMapping("/upload-multiple")
public String[] uploadMultiple(@RequestParam("files") MultipartFile[] files) {
    return minioService.uploadMultiple(files, "uploads");
}
```

### Move/Rename file

```java
public boolean renameFile(String oldName, String newName) {
    return minioService.move("uploads/" + oldName, "uploads/" + newName);
}
```

## 📚 API Reference

### MinioService Methods

| Method | Description |
|--------|-------------|
| `upload(MultipartFile, String)` | Upload file với tên gốc |
| `uploadWithUniqueFilename(MultipartFile, String)` | Upload với tên unique |
| `upload(ByteArrayOutputStream, String)` | Upload từ stream |
| `download(String)` | Download file |
| `delete(String)` | Xóa file |
| `exists(String)` | Kiểm tra file tồn tại |
| `move(String, String)` | Di chuyển/đổi tên file |
| `getTemporaryUrl(String)` | Lấy URL tạm thời (1h) |
| `getTemporaryUrl(String, int)` | Lấy URL tạm thời (custom) |
| `uploadMultiple(MultipartFile[], String)` | Upload nhiều file |
| `deleteMultiple(String[])` | Xóa nhiều file |

### MinIOClientImpl Methods

| Method | Description |
|--------|-------------|
| `upload(ByteArrayOutputStream, String)` | Upload từ stream |
| `upload(MultipartFile, String)` | Upload MultipartFile |
| `getObject(String)` | Lấy object |
| `deleteObject(String)` | Xóa object |
| `moveObject(String, String)` | Di chuyển object |
| `objectExists(String)` | Kiểm tra tồn tại |
| `getPresignedUrl(String, int)` | Tạo presigned URL |

## 🔧 Configuration Properties

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `minio.endpoint` | ✅ | - | MinIO server URL |
| `minio.access-key` | ✅ | - | Access key |
| `minio.secret-key` | ✅ | - | Secret key |
| `minio.bucket` | ✅ | - | Default bucket |
| `minio.secure` | ❌ | false | Use HTTPS |
| `minio.connect-timeout` | ❌ | 10000 | Connection timeout (ms) |
| `minio.write-timeout` | ❌ | 60000 | Write timeout (ms) |
| `minio.read-timeout` | ❌ | 10000 | Read timeout (ms) |
| `minio.auto-create-bucket` | ❌ | true | Auto create bucket |
| `minio.part-size` | ❌ | 104857600 | Part size cho multipart upload (100MB) |
| `minio.base-url` | ❌ | endpoint | Base URL để tạo file URL |
| `minio.enabled` | ❌ | true | Enable auto-config |

## 🚀 Performance - Multipart Upload

Thư viện hỗ trợ **multipart upload tự động** với part size mặc định 100MB:

- ✅ File lớn được chia thành nhiều part
- ✅ Upload song song (parallel/multi-threaded) 
- ✅ Tăng tốc độ upload đáng kể cho file lớn
- ✅ Tự động retry từng part nếu lỗi

```properties
# Điều chỉnh part size theo nhu cầu
minio.part-size=52428800  # 50MB
minio.part-size=104857600 # 100MB (default)
minio.part-size=209715200 # 200MB
```

## 🐳 Setup MinIO với Docker

```bash
docker run -d \
  -p 9000:9000 \
  -p 9001:9001 \
  --name minio \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  quay.io/minio/minio server /data --console-address ":9001"
```

Console: http://localhost:9001

## 📖 Tài liệu bổ sung

- [README-USAGE.md](README-USAGE.md) - Hướng dẫn chi tiết
- [TESTING.md](TESTING.md) - Hướng dẫn test
- [Example Controller](src/test/java/com/theblood/minio/example/MinioExampleController.java)

## 🔐 Bảo mật

Không hardcode credentials! Sử dụng environment variables:

```properties
minio.access-key=${MINIO_ACCESS_KEY}
minio.secret-key=${MINIO_SECRET_KEY}
```

## 📝 License

MIT License

## 👥 Author

TheBlood Team
