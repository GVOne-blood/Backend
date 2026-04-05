# Báo Cáo Verification: Carbone Service Implementation

## Tổng Quan

Báo cáo này so sánh implementation hiện tại của Carbone service trong dự án `statistical-report` với thiết kế gốc từ dự án tham khảo (carbone-minio-export-api.md).

**Ngày kiểm tra:** 2026-02-23  
**Người thực hiện:** Kiro AI Assistant  
**Mục đích:** Verify implementation theo chuẩn thiết kế ban đầu

---

## 1. Kiến Trúc Tổng Quan

### ✅ Thiết Kế Gốc
```
Client Request → REST Controller → Service Layer → CarboneService → Carbone Server
                                                                    ↓
                                                            Render Report (Excel/PDF)
                                                                    ↓
                                                            MinIO Storage ← Upload
                                                                    ↓
                                                            Return Download URL
```

### ⚠️ Implementation Hiện Tại
```
Client Request → REST Controller → CarboneService → Carbone Server
                                                    ↓
                                            Render Report (Excel/PDF)
                                                    ↓
                                            Return URL (từ Carbone)
```

**Vấn đề:** THIẾU tầng MinIO Storage - file không được upload lên MinIO sau khi render!

---

## 2. So Sánh Chi Tiết Từng Component

### 2.1 Dependencies

| Component | Thiết Kế Gốc | Implementation Hiện Tại | Status |
|-----------|---------------|-------------------------|--------|
| MinIO Client | ✅ `io.minio:minio:8.5.7` | ❌ KHÔNG CÓ | 🔴 THIẾU |
| OkHttp | ✅ `com.squareup.okhttp3:okhttp` | ✅ `4.12.0` | ✅ OK |
| OkHttp Logging | ✅ `logging-interceptor` | ✅ `4.12.0` | ✅ OK |
| Gson | ✅ `com.google.code.gson:gson` | ✅ Có | ✅ OK |

**Vấn đề nghiêm trọng:** Không có MinIO dependency trong `statistical-report/pom.xml`!

### 2.2 Configuration

#### Thiết Kế Gốc (application-dev.yml)
```yaml
carbone:
  base-url: ${CARBONE_BASE_URL:http://117.5.148.63:8100/api/v1}

minio:
  url: http://${MINIO_HOST:117.5.148.63}:${MINIO_PORT:9000}
  access-key: ${MINIO_ACCESS:minioadmin}
  secret-key: ${MINIO_SECRET:Vhkt!@#2025}
  upload-path: party-card/uploads
  bucket-name: cslddv-bucket
  avatar-bucket-name: cslddv-public-bucket
```

#### Implementation Hiện Tại
```yaml
carbone:
  base-url: ${CARBONE_BASE_URL:http://localhost:8100}
  template-path: ${CARBONE_TEMPLATE_PATH:statistical-report/src/main/resources/templates/reports}
  minio-bucket: ${CARBONE_MINIO_BUCKET:springfood-input}
```

**Vấn đề:**
- ❌ THIẾU toàn bộ MinIO configuration (url, access-key, secret-key, upload-path, bucket-name)
- ⚠️ Có `minio-bucket` nhưng không có MinIO client để sử dụng
- ⚠️ Có `template-path` (local mode) nhưng thiết kế gốc không có

### 2.3 DTOs (Data Transfer Objects)

| DTO Class | Thiết Kế Gốc | Implementation Hiện Tại | Status |
|-----------|---------------|-------------------------|--------|
| CarboneBody | ✅ Đầy đủ fields | ✅ Đầy đủ fields | ✅ OK |
| CarboneOption | ✅ Đầy đủ fields | ✅ Đầy đủ fields | ✅ OK |
| CarboneResponseData | ✅ etag, url, viewUrl, fileName, bucketName | ✅ + versionId | ✅ OK |
| CarboneResponseDTO | ✅ status, data | ✅ status, data | ✅ OK |

**Đánh giá:** DTOs implementation tốt, có thêm field `versionId` (không ảnh hưởng).

### 2.4 CarboneService Class

#### Thiết Kế Gốc - Methods
```java
// Main render methods
CarboneResponseData renderReport(Object inputJson, String templateFileName, String reportName, String convertTo)
CarboneResponseData renderReport(Object inputJson, CarboneOption carboneOption, String templateFileName)

// Convert method
CarboneResponseData convert(String bucketName, String templateFileName, String convertTo)

// Helper methods
String buildJsonBodyRender(String fileTemplate, CarboneOption carboneOption, Object data)
String sendToReportCore(String requestPath, String jsonBody)
Request createRequest(String url, String method, RequestBody body)

// MinIO integration
CarboneResponseData getCarboneResponseData(Object inputJson, String templateFileName, CarboneOption carboneOption)
// → Gọi renderReport() → Clean URL (remove domain) → Return
```

#### Implementation Hiện Tại - Methods
```java
// Main render methods
✅ CarboneResponseData renderReport(Object inputJson, String templateFileName, String convertTo)
✅ CarboneResponseData renderReport(Object inputJson, String templateFileName, String reportName, String convertTo)
✅ CarboneResponseData renderReport(Object inputJson, CarboneOption carboneOption, String templateFileName)

// Convert method
✅ CarboneResponseData convert(String bucketName, String templateFileName, String convertTo)
✅ CarboneResponseData convert(String bucketName, CarboneOption carboneOption, String templateFileName)

// Helper methods
✅ String buildJsonBodyRender(String fileTemplate, CarboneOption carboneOption, Object data)
✅ String buildJsonBodyConvert(String fileTemplate, CarboneOption carboneOption)
✅ String buildJsonBodyMultiRender(CarboneOption carboneOption, Object dataset)
✅ String sendToReportCore(String requestPath, String jsonBody)
✅ String sendToReportCore(String requestPath, String jsonBody, Map<String, String> additionalHeaders)
✅ Request createRequest(String url, String method, RequestBody body)
✅ Request createRequest(String url, String method, RequestBody body, Map<String, String> additionalHeaders)

// MinIO integration
❌ KHÔNG CÓ getCarboneResponseData()
❌ KHÔNG CÓ MinioFileService dependency
❌ KHÔNG CÓ FileCompressionService dependency
❌ KHÔNG CÓ renderCompressedFile()
❌ KHÔNG CÓ uploadCompressedFile()
```

**Đánh giá:**
- ✅ Core methods đầy đủ và tốt hơn (có thêm overload methods)
- ✅ Logging tốt hơn (có log request URL và error details)
- ❌ THIẾU hoàn toàn MinIO integration layer
- ❌ THIẾU URL cleaning logic (remove domain)
- ❌ THIẾU compressed file export feature

### 2.5 MinIO Integration

#### Thiết Kế Gốc
```java
@Service
@RequiredArgsConstructor
public class CarboneService {
    private final Gson gson;
    private final OkHttpClient client;
    private final MinioFileService minioFileService;  // ← QUAN TRỌNG
    private final FileCompressionService fileCompressionService;
    
    @Value("${minio.url}")
    private String url;
    
    private CarboneResponseData getCarboneResponseData(...) {
        var res = renderReport(inputJson, carboneOption, templateFileName);
        
        if (res != null) {
            // Clean URL (remove domain, keep path only)
            if (StringUtils.hasText(res.getUrl())) {
                res.setUrl(res.getUrl().replaceAll("^https?://[^/]+", ""));
            }
            if (StringUtils.hasText(res.getViewUrl())) {
                res.setViewUrl(res.getViewUrl().replaceAll("^https?://[^/]+", ""));
            }
        }
        return res;
    }
}
```

#### Implementation Hiện Tại
```java
@Service
@ConditionalOnProperty(name = "carbone.base-url")
public class CarboneService {
    @Autowired
    @Qualifier("carboneHttpClient")
    OkHttpClient carboneHttpClient;
    
    @Autowired
    @Qualifier("carboneGson")
    private Gson carboneGson;
    
    // ❌ KHÔNG CÓ MinioFileService
    // ❌ KHÔNG CÓ FileCompressionService
    // ❌ KHÔNG CÓ @Value("${minio.url}")
    // ❌ KHÔNG CÓ URL cleaning logic
}
```

**Vấn đề nghiêm trọng:**
1. ❌ Không có MinioFileService dependency
2. ❌ Không có logic upload file lên MinIO sau khi render
3. ❌ Không có logic clean URL
4. ❌ File chỉ tồn tại trên Carbone server, không được lưu vào MinIO storage

---

## 3. Luồng Xử Lý So Sánh

### Thiết Kế Gốc (Đúng)
```
1. Client request → Controller
2. Controller → Service chuẩn bị data
3. Service → CarboneService.renderReport()
4. CarboneService → Carbone Server (render template)
5. Carbone Server → Upload file lên MinIO
6. Carbone Server → Return MinIO URL
7. CarboneService → Clean URL (remove domain)
8. CarboneService → Return CarboneResponseData với MinIO URL
9. Client → Download từ MinIO (presigned URL, có expiry)
```

### Implementation Hiện Tại (Sai)
```
1. Client request → Controller
2. Controller → CarboneService.renderReport()
3. CarboneService → Carbone Server (render template)
4. Carbone Server → Return URL (Carbone internal URL)
5. CarboneService → Return CarboneResponseData với Carbone URL
6. Client → Download từ Carbone Server (không có MinIO)
```

**Vấn đề:**
- ❌ File không được upload lên MinIO
- ❌ Client phải download trực tiếp từ Carbone server
- ❌ Không có presigned URL với expiry time
- ❌ Không có persistent storage (file có thể bị xóa khỏi Carbone)

---

## 4. Missing Components (Các Component Bị Thiếu)

### 4.1 MinIOConfiguration.java
**Thiết kế gốc:**
```java
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinIOConfiguration {
    private String url;
    private String accessKey;
    private String secretKey;
    private String uploadPath;
    private String bucketName;
    private String avatarBucketName;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint(url)
            .credentials(accessKey, secretKey)
            .build();
    }
}
```

**Implementation hiện tại:** ❌ KHÔNG CÓ

### 4.2 MinioFileService.java
**Thiết kế gốc:**
```java
@Service
@RequiredArgsConstructor
public class MinioFileService {
    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    @Value("${minio.upload-path}")
    private String uploadPath;
    
    @Value("${minio.url}")
    private String url;
    
    // Upload file to MinIO
    public String uploadFile(byte[] data, String fileName, String contentType) { ... }
    
    // Generate presigned download URL
    public String generateDownloadUrl(String objectPath, Duration expiry) { ... }
    
    public String generateDownloadUrl(String objectPath) {
        return generateDownloadUrl(objectPath, Duration.ofDays(7));
    }
}
```

**Implementation hiện tại:** ❌ KHÔNG CÓ

### 4.3 FileCompressionService.java
**Thiết kế gốc:**
```java
@Service
public class FileCompressionService {
    public Path createCompressedFile(Map<String, String> fileMap, String exportFileName) { ... }
    public void cleanupTempFiles(Path tempDir) { ... }
}
```

**Implementation hiện tại:** ❌ KHÔNG CÓ

### 4.4 Service Layer với Data Preparation
**Thiết kế gốc:**
```java
@Service
@RequiredArgsConstructor
public class StatisticReportServiceImpl implements StatisticReportService {
    private final CarboneService carboneService;
    private final StatisticalLogicalService statisticalLogicalService;

    @Override
    public ResponseEntity<CarboneResponseData> exportExcelFormFive(ReportInputDTO request) {
        // Bước 1: Chuẩn bị dữ liệu từ database
        ReportCarboneDTO dto = handleDataForFileExportFormFive(request);
        
        // Bước 2: Tạo tên file output
        String fileName = String.format("Biểu số 05 - BTCTW %s %d %s.xlsx",
            dto.getUnitName(), dto.getYear(), dto.getPeriodName());
        
        // Bước 3: Gọi CarboneService
        return ResponseEntity.ok().body(
            carboneService.renderReport(dto, "form_report5.xlsx", fileName, "xlsx")
        );
    }
}
```

**Implementation hiện tại:** ❌ KHÔNG CÓ Service layer, chỉ có Controller gọi trực tiếp CarboneService

---

## 5. Các Vấn Đề Cần Fix

### 🔴 Critical Issues (Phải fix ngay)

1. **THIẾU MinIO Integration**
   - Không có MinIO dependency trong pom.xml
   - Không có MinIOConfiguration
   - Không có MinioFileService
   - File không được upload lên MinIO storage

2. **THIẾU Service Layer**
   - Controller gọi trực tiếp CarboneService
   - Không có logic chuẩn bị dữ liệu từ database
   - Không có logic tạo tên file có ý nghĩa

3. **THIẾU URL Cleaning Logic**
   - URL trả về từ Carbone không được clean (remove domain)
   - Client nhận full URL thay vì relative path

### ⚠️ Medium Issues (Nên fix)

4. **THIẾU FileCompressionService**
   - Không hỗ trợ export multiple files thành ZIP
   - Thiết kế gốc có feature này

5. **THIẾU Error Handling**
   - Không có validation cho input
   - Không có proper error messages
   - Không có logging đầy đủ cho troubleshooting

6. **THIẾU Configuration Validation**
   - Không validate MinIO connection khi startup
   - Không validate Carbone server connection

### ℹ️ Low Issues (Nice to have)

7. **THIẾU Testing**
   - Không có unit tests
   - Không có integration tests
   - Không có test documentation

8. **THIẾU Documentation**
   - Không có API documentation
   - Không có usage examples
   - Không có troubleshooting guide

---

## 6. Recommended Actions (Hành Động Khuyến Nghị)

### Phase 1: Critical Fixes (Ưu tiên cao nhất)

#### 1.1 Thêm MinIO Dependencies
```xml
<!-- statistical-report/pom.xml -->
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.7</version>
</dependency>
```

#### 1.2 Tạo MinIO Configuration
```java
// config/MinIOConfiguration.java
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinIOConfiguration {
    private String url;
    private String accessKey;
    private String secretKey;
    private String uploadPath;
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint(url)
            .credentials(accessKey, secretKey)
            .build();
    }
}
```

#### 1.3 Tạo MinioFileService
```java
// service/util/MinioFileService.java
@Service
@RequiredArgsConstructor
public class MinioFileService {
    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    @Value("${minio.upload-path}")
    private String uploadPath;
    
    public String uploadFile(byte[] data, String fileName, String contentType) {
        // Implementation theo thiết kế gốc
    }
    
    public String generateDownloadUrl(String objectPath) {
        return generateDownloadUrl(objectPath, Duration.ofDays(7));
    }
}
```

#### 1.4 Update CarboneService
```java
@Service
@RequiredArgsConstructor
public class CarboneService {
    private final Gson gson;
    private final OkHttpClient client;
    private final MinioFileService minioFileService;  // ← THÊM
    
    @Value("${minio.url}")
    private String url;  // ← THÊM
    
    // Thêm method getCarboneResponseData() với URL cleaning logic
    private CarboneResponseData getCarboneResponseData(...) {
        var res = renderReport(inputJson, carboneOption, templateFileName);
        
        if (res != null) {
            // Clean URL
            if (StringUtils.hasText(res.getUrl())) {
                res.setUrl(res.getUrl().replaceAll("^https?://[^/]+", ""));
            }
            if (StringUtils.hasText(res.getViewUrl())) {
                res.setViewUrl(res.getViewUrl().replaceAll("^https?://[^/]+", ""));
            }
        }
        return res;
    }
}
```

#### 1.5 Update application-dev.yml
```yaml
carbone:
  base-url: ${CARBONE_BASE_URL:http://localhost:8100}

minio:
  url: ${MINIO_URL:http://localhost:9000}
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin}
  upload-path: statistical-reports/uploads
  bucket-name: springfood-reports
```

### Phase 2: Service Layer (Ưu tiên cao)

#### 2.1 Tạo Service Interface
```java
public interface StatisticalReportService {
    ResponseEntity<CarboneResponseData> exportExcelFormFive(ReportInputDTO request);
    ResponseEntity<CarboneResponseData> exportPdfFormFive(ReportInputDTO request);
}
```

#### 2.2 Tạo Service Implementation
```java
@Service
@RequiredArgsConstructor
public class StatisticalReportServiceImpl implements StatisticalReportService {
    private final CarboneService carboneService;
    // private final StatisticalLogicalService statisticalLogicalService;
    
    @Override
    public ResponseEntity<CarboneResponseData> exportExcelFormFive(ReportInputDTO request) {
        // 1. Validate input
        // 2. Query database
        // 3. Transform data
        // 4. Generate meaningful filename
        // 5. Call CarboneService
        // 6. Return response
    }
}
```

#### 2.3 Update Controller
```java
@RestController
@RequestMapping("/api/statistical-report")
@RequiredArgsConstructor
public class StatisticalReportResource {
    private final StatisticalReportService statisticalReportService;  // ← Thay đổi
    
    @GetMapping("/form-five/export")
    public ResponseEntity<CarboneResponseData> exportFormFive(ReportInputDTO request) {
        return switch (request.getFlagExport()) {
            case 1 -> statisticalReportService.exportExcelFormFive(request);
            case 2 -> statisticalReportService.exportPdfFormFive(request);
            default -> ResponseEntity.badRequest().build();
        };
    }
}
```

### Phase 3: Additional Features (Ưu tiên trung bình)

#### 3.1 FileCompressionService
```java
@Service
public class FileCompressionService {
    public Path createCompressedFile(Map<String, String> fileMap, String exportFileName) {
        // Create ZIP file from multiple files
    }
    
    public void cleanupTempFiles(Path tempDir) {
        // Cleanup temporary files
    }
}
```

#### 3.2 Compressed Export Feature
```java
// Trong CarboneService
public CarboneResponseData renderCompressedFile(
    List<CarboneResponseData> files,
    String exportFileName
) {
    // Implementation theo thiết kế gốc
}
```

### Phase 4: Quality Improvements (Ưu tiên thấp)

#### 4.1 Add Validation
```java
private void validateExportRequest(ReportInputDTO request) {
    if (request.getYear() == null || request.getYear() < 2000) {
        throw new BadRequestAlertException("Invalid year", "report", "invalidyear");
    }
    // More validations...
}
```

#### 4.2 Add Error Handling
```java
try {
    CarboneResponseData result = carboneService.renderReport(...);
    return ResponseEntity.ok(result);
} catch (RuntimeException e) {
    log.error("Failed to export report: {}", e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("Export failed: " + e.getMessage()));
}
```

#### 4.3 Add Tests
- Unit tests cho CarboneService
- Unit tests cho MinioFileService
- Integration tests cho export flow
- Test documentation

---

## 7. Kết Luận

### Tình Trạng Hiện Tại
Implementation hiện tại của Carbone service **KHÔNG HOÀN CHỈNH** so với thiết kế gốc:

- ✅ **Có:** Core Carbone integration (render templates)
- ✅ **Có:** DTOs structure
- ✅ **Có:** Basic configuration
- ❌ **THIẾU:** MinIO integration (critical)
- ❌ **THIẾU:** Service layer với data preparation
- ❌ **THIẾU:** URL cleaning logic
- ❌ **THIẾU:** File compression feature
- ❌ **THIẾU:** Proper error handling và validation

### Mức Độ Hoàn Thiện
**40%** - Chỉ có core Carbone API integration, thiếu toàn bộ storage layer và business logic layer.

### Rủi Ro
1. **High Risk:** File không được lưu vào persistent storage (MinIO)
2. **High Risk:** Client phải download trực tiếp từ Carbone server (không scalable)
3. **Medium Risk:** Không có service layer để chuẩn bị dữ liệu
4. **Medium Risk:** Không có error handling và validation

### Khuyến Nghị
**Cần implement ngay Phase 1 (Critical Fixes)** để đảm bảo hệ thống hoạt động đúng theo thiết kế:
1. Thêm MinIO integration
2. Tạo Service layer
3. Update configuration

Sau đó mới tiếp tục Phase 2, 3, 4 để hoàn thiện các tính năng còn lại.

---

## 8. Checklist Implementation

### Must Have (Bắt buộc)
- [ ] Add MinIO dependency to pom.xml
- [ ] Create MinIOConfiguration class
- [ ] Create MinioFileService class
- [ ] Update CarboneService with MinIO integration
- [ ] Add URL cleaning logic
- [ ] Update application-dev.yml with MinIO config
- [ ] Create Service layer (StatisticalReportService)
- [ ] Update Controller to use Service layer
- [ ] Add input validation
- [ ] Add error handling

### Should Have (Nên có)
- [ ] Create FileCompressionService
- [ ] Add compressed export feature
- [ ] Add proper logging
- [ ] Add configuration validation
- [ ] Create unit tests
- [ ] Create integration tests

### Nice to Have (Tốt nếu có)
- [ ] Add API documentation
- [ ] Add usage examples
- [ ] Add troubleshooting guide
- [ ] Add monitoring metrics
- [ ] Add performance optimization

---

**Người tạo báo cáo:** Kiro AI Assistant  
**Ngày:** 2026-02-23  
**Version:** 1.0
