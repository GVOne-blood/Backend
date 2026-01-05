# Changelog

All notable changes to MinIO Spring Boot Library will be documented in this file.

## [0.0.1-SNAPSHOT] - 2025-11-20

### ✨ Features

- **Auto-configuration** - Tự động cấu hình MinIO client khi import vào dự án Spring Boot
- **Flexible Configuration** - Cấu hình linh hoạt qua properties/yml
- **Multipart Upload** - Hỗ trợ upload file lớn với part size configurable (default 100MB)
- **Parallel Upload** - Upload đa luồng tự động cho file lớn
- **File Operations**:
  - Upload file (MultipartFile, ByteArrayOutputStream)
  - Download file
  - Delete file
  - Move/Rename file
  - Check file existence
  - Generate presigned URLs
- **Auto-create Bucket** - Tự động tạo bucket nếu chưa tồn tại
- **MinioService** - High-level service với các helper methods
- **Example Controller** - Controller mẫu để tham khảo

### 🔧 Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `minio.endpoint` | - | MinIO server URL (required) |
| `minio.access-key` | - | Access key (required) |
| `minio.secret-key` | - | Secret key (required) |
| `minio.bucket` | - | Default bucket (required) |
| `minio.secure` | false | Use HTTPS |
| `minio.connect-timeout` | 10000 | Connection timeout (ms) |
| `minio.write-timeout` | 60000 | Write timeout (ms) |
| `minio.read-timeout` | 10000 | Read timeout (ms) |
| `minio.auto-create-bucket` | true | Auto create bucket |
| `minio.part-size` | 104857600 | Part size for multipart upload (100MB) |
| `minio.base-url` | endpoint | Base URL for file URLs |
| `minio.enabled` | true | Enable auto-configuration |

### 📦 Dependencies

- Spring Boot 3.5.7
- MinIO Java SDK 8.5.10
- Jakarta Validation API
- Hibernate Validator
- Lombok
- Spring Web

### 📚 Documentation

- README.md - Tổng quan và quick start
- README-USAGE.md - Hướng dẫn sử dụng chi tiết
- QUICKSTART.md - Bắt đầu nhanh trong 5 phút
- TESTING.md - Hướng dẫn test
- PERFORMANCE.md - Hướng dẫn tối ưu performance
- BUILD.md - Hướng dẫn build và deploy

### 🎯 Use Cases

- Upload/Download file trong microservices
- Lưu trữ file cho web application
- Backup và archive
- CDN integration
- Multi-tenant file storage

### 🔐 Security

- Không hardcode credentials
- Hỗ trợ environment variables
- Validation đầy đủ cho config
- Presigned URLs cho temporary access

### ⚡ Performance

- Multipart upload với configurable part size
- Parallel upload tự động cho file lớn
- Connection pooling
- Configurable timeouts
- Retry mechanism cho từng part

### 🐛 Known Issues

None

### 📝 Notes

- Đây là SNAPSHOT version cho development
- Cần MinIO server đang chạy để test
- Auto-configuration chỉ hoạt động với Spring Boot
- Requires Java 17+

---

## [Unreleased]

### Planned Features

- [ ] Batch upload/download
- [ ] Progress callback cho upload
- [ ] Compression support
- [ ] Encryption support
- [ ] Metrics và monitoring
- [ ] Spring Boot Actuator integration
- [ ] Reactive support (WebFlux)
- [ ] Multiple bucket support
- [ ] Object versioning
- [ ] Lifecycle management

### Future Improvements

- [ ] Better error handling
- [ ] More comprehensive tests
- [ ] Performance benchmarks
- [ ] Docker compose example
- [ ] Kubernetes deployment guide
- [ ] Spring Cloud Config integration

---

## Version History

- **0.0.1-SNAPSHOT** (2025-11-20) - Initial release

---

## Migration Guide

N/A - First version

---

## Breaking Changes

N/A - First version

---

## Contributors

- TheBlood Team

---

## License

MIT License
