# Skill: Carbone & MinIO Export API Integration

## Tổng quan
Skill này mô tả chi tiết luồng hoạt động của API export sử dụng Carbone (template engine) và MinIO (object storage) để tạo và lưu trữ báo cáo Excel/PDF trong hệ thống.

## Kiến trúc tổng quan

```
Client Request → REST Controller → Service Layer → CarboneService → Carbone Server
                                                                    ↓
                                                            Render Report (Excel/PDF)
                                                                    ↓
                                                            MinIO Storage ← Upload
                                                                    ↓
                                                            Return Download URL
```

## 1. Cấu hình hệ thống

### 1.1 MinIO Configuration

**File**: `config/MinIOConfiguration.java`

```java
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinIOConfiguration {
    private String url;              // MinIO server URL
    private String accessKey;        // Access key
    private String secretKey;        // Secret key
    private String uploadPath;       // Upload path prefix
    private String bucketName;       // Main bucket name
    private String avatarBucketName; // Avatar bucket name

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint(url)
            .credentials(accessKey, secretKey)
            .build();
    }
}
```

**Application Configuration** (`application-dev.yml`):

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

### 1.2 Dependencies (pom.xml)

```xml
<!-- MinIO Client -->
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.7</version>
</dependency>

<!-- OkHttp for Carbone API calls -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
</dependency>

<!-- Gson for JSON processing -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
</dependency>
```

## 2. Luồng hoạt động chi tiết

### 2.1 REST Controller Layer

**File**: `web/rest/PartyStatisticalReportResource.java`

```java
@RestController
@RequestMapping("/api/statistical-report")
@RequiredArgsConstructor
public class PartyStatisticalReportResource {
    private final StatisticReportService statisticReportService;

    @GetMapping("/form-five/export")
    public ResponseEntity<CarboneResponseData> exportExcelFormFive(ReportInputDTO request) {
        return switch (request.getFlagExport()) {
            case 1 -> statisticReportService.exportExcelFormFive(request);  // Excel
            case 2 -> statisticReportService.exportPdfFormFive(request);    // PDF
            default -> ResponseEntity.badRequest().build();
        };
    }
}
```

**Nhiệm vụ**:
- Nhận request từ client với tham số `flagExport` (1=Excel, 2=PDF)
- Route đến service method tương ứng
- Trả về `CarboneResponseData` chứa URL download

### 2.2 Service Implementation Layer

**File**: `service/impl/StatisticReportServiceImpl.java`

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
        if (Objects.isNull(dto)) {
            return ResponseEntity.noContent().build();
        }

        // Bước 2: Tạo tên file output
        String fileName = String.format("Biểu số 05 - BTCTW %s %d %s.xlsx",
            dto.getUnitName(),
            dto.getYear(),
            dto.getPeriodName()
        );
        
        // Bước 3: Gọi CarboneService để render report
        return ResponseEntity.ok().body(
            carboneService.renderReport(
                dto,                    // Dữ liệu input
                "form_report5.xlsx",    // Template file name
                fileName,               // Output file name
                "xlsx"                  // Convert format
            )
        );
    }

    private ReportCarboneDTO handleDataForFileExportFormFive(ReportInputDTO request) {
        // Lấy dữ liệu từ database
        ReportResponse response = statisticalLogicalService.statisticReportFormFive(request);
        
        // Transform sang DTO cho Carbone
        ReportCarboneDTO dto = new ReportCarboneDTO();
        dto.setUnitName(response.getReportInfo().getUnitName());
        dto.setYear(response.getReportInfo().getYear());
        dto.setPeriodName(response.getReportInfo().getPeriodName());
        dto.setList(response.getRows());  // Danh sách dữ liệu
        
        return dto;
    }
}
```

**Nhiệm vụ**:
- Lấy và transform dữ liệu từ database
- Tạo tên file output có ý nghĩa
- Gọi CarboneService để render report

### 2.3 Carbone Service Layer

**File**: `service/CarboneService.java`

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class CarboneService {

    @Value("${carbone.base-url}")
    private String carboneBaseUrl;

    @Value("${minio.url}")
    private String url;

    private final Gson gson;
    private final OkHttpClient client;
    private final MinioFileService minioFileService;
    private final FileCompressionService fileCompressionService;

    /**
     * Main method để render report
     */
    public CarboneResponseData renderReport(
        Object inputJson,           // Dữ liệu input
        String templateFileName,    // Tên template
        String reportName,          // Tên file output
        String convertTo            // Format: xlsx, pdf, docx
    ) {
        // Bước 1: Tạo CarboneOption
        CarboneOption carboneOption = CarboneOption.builder()
            .reportName(reportName)
            .convertTo(convertTo)
            .responseType("url")
            .timezone("Asia/Ho_Chi_Minh")
            .lang("vi")
            .build();
        
        // Bước 2: Gọi Carbone API để render
        return getCarboneResponseData(inputJson, templateFileName, carboneOption);
    }

    private CarboneResponseData getCarboneResponseData(
        Object inputJson, 
        String templateFileName, 
        CarboneOption carboneOption
    ) {
        // Render report qua Carbone API
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

    /**
     * Gọi Carbone API để render report
     */
    public CarboneResponseData renderReport(
        Object inputJson, 
        CarboneOption carboneOption, 
        String templateFileName
    ) {
        // Bước 1: Build JSON body
        if (inputJson instanceof String && isNullOrEmpty(String.valueOf(inputJson))) {
            inputJson = "{}";
        }
        String body = buildJsonBodyRender(templateFileName, carboneOption, inputJson);
        
        // Bước 2: Gửi request đến Carbone Server
        String response = sendToReportCore("/render", body);
        
        // Bước 3: Parse response
        CarboneResponseDTO carboneResponse = gson.fromJson(response, CarboneResponseDTO.class);
        
        // Bước 4: Clean URL
        if (Objects.nonNull(carboneResponse.getData())) {
            String urlNew = carboneResponse.getData().getUrl().replace(url, "");
            carboneResponse.getData().setUrl(urlNew);
        }
        
        return carboneResponse.getData();
    }

    /**
     * Build JSON body cho Carbone API
     */
    protected String buildJsonBodyRender(
        String fileTemplate, 
        CarboneOption carboneOption, 
        Object data
    ) {
        CarboneBody carboneBodyDTO = CarboneBody.builder()
            .fileName(fileTemplate)
            .options(carboneOption)
            .data(data)
            .build();
        return gson.toJson(carboneBodyDTO);
    }

    /**
     * Gửi request đến Carbone Server
     */
    protected String sendToReportCore(String requestPath, String jsonBody) {
        RequestBody requestBody = RequestBody.create(jsonBody.getBytes());
        Request request = createRequest(carboneBaseUrl + requestPath, "POST", requestBody);
        
        try {
            Response responses = client.newCall(request).execute();
            if (responses.isSuccessful()) {
                return responses.body().string();
            } else {
                throw new RuntimeException(
                    String.format("Carbone server response with error %s", responses.code())
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot connect to Carbone Server", e);
        }
    }

    protected Request createRequest(String url, String method, RequestBody body) {
        return new Request.Builder()
            .url(url)
            .method(method, body)
            .addHeader("Content-Type", "application/json")
            .build();
    }
}
```

**Nhiệm vụ**:
- Build request body cho Carbone API
- Gọi Carbone Server để render template với dữ liệu
- Parse response và clean URL
- Trả về thông tin file đã render

### 2.4 MinIO File Service

**File**: `service/util/MinioFileService.java`

```java
@Service
@RequiredArgsConstructor
public class MinioFileService {
    
    private static final Logger log = LoggerFactory.getLogger(MinioFileService.class);
    private final MinioClient minioClient;

    @Getter
    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.upload-path}")
    private String uploadPath;

    @Getter
    @Value("${minio.url}")
    private String url;

    /**
     * Upload byte array to MinIO
     */
    public String uploadFile(byte[] data, String fileName, String contentType) {
        try {
            // Tạo unique path
            String uuid = UUID.randomUUID().toString();
            String objectPath = String.format("%s/%s/%s", uploadPath, uuid, fileName);

            // Ensure bucket exists
            if (!minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
            )) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("Created bucket: {}", bucketName);
            }

            // Upload file to MinIO
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .stream(new ByteArrayInputStream(data), data.length, -1)
                    .contentType(contentType)
                    .build()
            );

            log.info("File uploaded successfully to MinIO: {}", objectPath);
            return objectPath;

        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", fileName, e);
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    /**
     * Generate presigned download URL with custom expiry
     */
    public String generateDownloadUrl(String objectPath, Duration expiry) {
        try {
            Map<String, String> extraQueryParams = new HashMap<>();
            extraQueryParams.put("response-content-disposition",
                "attachment; filename=\"" + getFileNameFromPath(objectPath) + "\"");

            String downloadUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectPath)
                    .expiry((int) expiry.getSeconds())
                    .extraQueryParams(extraQueryParams)
                    .build()
            );

            log.info("Generated download URL for file: {}", objectPath);
            // Remove domain, keep path only
            return downloadUrl.replace(url, "");

        } catch (Exception e) {
            log.error("Failed to generate download URL for file: {}", objectPath, e);
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }

    /**
     * Generate download URL with default expiry (7 days)
     */
    public String generateDownloadUrl(String objectPath) {
        return generateDownloadUrl(objectPath, Duration.ofDays(7));
    }

    private String getFileNameFromPath(String objectPath) {
        if (objectPath == null || objectPath.isEmpty()) {
            return "download";
        }
        int lastSlash = objectPath.lastIndexOf('/');
        return lastSlash >= 0 ? objectPath.substring(lastSlash + 1) : objectPath;
    }
}
```

**Nhiệm vụ**:
- Upload file lên MinIO storage
- Tạo presigned URL để download (có thời gian hết hạn)
- Quản lý bucket và object path

## 3. Data Transfer Objects (DTOs)

### 3.1 CarboneBody - Request body gửi đến Carbone

```java
@Builder
@Data
@AllArgsConstructor
public class CarboneBody {
    Object fileName;        // Tên template file
    CarboneOption options;  // Các tùy chọn render
    Object data;            // Dữ liệu input
    String convertTo;       // Format chuyển đổi
    Object dataset;         // Dataset cho multi-render
}
```

### 3.2 CarboneOption - Tùy chọn render

```java
@Builder
@Data
public class CarboneOption {
    String convertTo;       // xlsx, pdf, docx
    String responseType;    // "url" - trả về URL thay vì binary
    String reportName;      // Tên file output
    String timezone;        // "Asia/Ho_Chi_Minh"
    String lang;            // "vi"
    Boolean hardRefresh;    // Force refresh template cache
    Boolean saveTarget;     // Lưu file sau khi convert
}
```

### 3.3 CarboneResponseData - Response từ Carbone

```java
@Builder
@Data
public class CarboneResponseData {
    String etag;            // Template version hash
    String url;             // Download URL
    String viewUrl;         // Preview URL
    String fileName;        // File name
    String bucketName;      // MinIO bucket name
}
```

### 3.4 ReportCarboneDTO - Dữ liệu báo cáo

```java
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportCarboneDTO extends CarboneCommonDTO {
    private List<RowFormCacheDTO> list;  // Danh sách dữ liệu
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarboneCommonDTO {
    private String unitName;      // Tên đơn vị
    private Integer year;         // Năm báo cáo
    private String periodName;    // Kỳ báo cáo
    private String exportDate;    // Ngày xuất
}
```

## 4. Luồng xử lý từng bước

### Bước 1: Client gửi request
```http
GET /api/statistical-report/form-five/export?flagExport=1&year=2025&period=1&unitOrgId=123
```

### Bước 2: Controller nhận request
- Parse parameters từ `ReportInputDTO`
- Route đến service method dựa trên `flagExport`

### Bước 3: Service chuẩn bị dữ liệu
- Query database để lấy dữ liệu báo cáo
- Transform dữ liệu sang `ReportCarboneDTO`
- Tạo tên file output có ý nghĩa

### Bước 4: CarboneService render report
- Build `CarboneOption` với các tham số:
  - `convertTo`: "xlsx" hoặc "pdf"
  - `responseType`: "url"
  - `reportName`: Tên file output
  - `timezone`: "Asia/Ho_Chi_Minh"
  - `lang`: "vi"
- Build `CarboneBody` với:
  - `fileName`: Tên template (vd: "form_report5.xlsx")
  - `options`: CarboneOption
  - `data`: ReportCarboneDTO
- Gửi POST request đến Carbone Server: `{carbone-base-url}/render`

### Bước 5: Carbone Server xử lý
- Load template từ storage
- Merge dữ liệu vào template
- Convert sang format yêu cầu (xlsx/pdf)
- Upload file lên MinIO
- Trả về URL download

### Bước 6: Parse response
- Parse JSON response từ Carbone
- Extract `CarboneResponseData`
- Clean URL (remove domain, keep path)

### Bước 7: Trả về client
```json
{
  "etag": "abc123def456",
  "url": "/cslddv-bucket/party-card/uploads/uuid/Biểu số 05.xlsx",
  "viewUrl": "/cslddv-bucket/party-card/uploads/uuid/Biểu số 05.xlsx",
  "fileName": "Biểu số 05 - BTCTW Đảng bộ phường Âu Lâu 2025 Kỳ 1 năm.xlsx",
  "bucketName": "cslddv-bucket"
}
```

### Bước 8: Client download file
- Client sử dụng URL để download file
- MinIO serve file với presigned URL (có thời gian hết hạn)

## 5. Tính năng nâng cao

### 5.1 Compressed File Export (ZIP)

```java
public CarboneResponseData renderCompressedFile(
    List<CarboneResponseData> files,
    String exportFileName
) {
    Path tempFilePath = null;
    Map<String, String> fileMap = new HashMap<>();
    
    // Collect all file URLs
    for (var file : files) {
        fileMap.put(file.getFileName(), url + file.getUrl());
    }
    
    try {
        // Create ZIP file
        tempFilePath = fileCompressionService.createCompressedFile(
            fileMap, exportFileName
        );
        
        // Upload ZIP to MinIO
        String compressedFileUrl = uploadCompressedFile(tempFilePath);
        
        return CarboneResponseData.builder()
            .fileName(exportFileName)
            .url(compressedFileUrl)
            .bucketName(minioFileService.getBucketName())
            .build();
            
    } catch (Exception e) {
        throw new RuntimeException("Failed to create compressed export file", e);
    } finally {
        if (tempFilePath != null) {
            fileCompressionService.cleanupTempFiles(tempFilePath.getParent());
        }
    }
}

private String uploadCompressedFile(Path filePath) {
    try {
        byte[] fileData = java.nio.file.Files.readAllBytes(filePath);
        String fileName = filePath.getFileName().toString();
        String contentType = CommonFunction.getContentTypeFromFileName(fileName);
        String objectPath = minioFileService.uploadFile(fileData, fileName, contentType);
        return minioFileService.generateDownloadUrl(objectPath);
    } catch (Exception e) {
        throw new RuntimeException("Failed to upload compressed file to MinIO", e);
    }
}
```

### 5.2 Convert Template (không có dữ liệu)

```java
public CarboneResponseData convert(
    String bucketName, 
    String templateFileName, 
    String convertTo
) {
    CarboneOption carboneOption = CarboneOption.builder()
        .convertTo(convertTo)
        .responseType("url")
        .timezone("Asia/Ho_Chi_Minh")
        .saveTarget(true)
        .lang("vi")
        .build();
    
    return convert(bucketName, carboneOption, templateFileName);
}

public CarboneResponseData convert(
    String bucketName, 
    CarboneOption carboneOption, 
    String templateFileName
) {
    String body = buildJsonBodyConvert(templateFileName, carboneOption);
    String response = sendToReportCore(
        "/convert", 
        body, 
        Map.of("X-BUCKET-IN", bucketName, "X-BUCKET-OUT", bucketName)
    );
    CarboneResponseDTO carboneResponse = gson.fromJson(response, CarboneResponseDTO.class);
    return carboneResponse.getData();
}
```

## 6. Best Practices

### 6.1 Error Handling
```java
try {
    CarboneResponseData result = carboneService.renderReport(dto, template, fileName, format);
    return ResponseEntity.ok(result);
} catch (RuntimeException e) {
    log.error("Failed to export report: {}", e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("Export failed: " + e.getMessage()));
}
```

### 6.2 Validation
```java
private void validateExportRequest(ReportInputDTO request) {
    if (request.getYear() == null || request.getYear() < 2000) {
        throw new BadRequestAlertException("Invalid year", "report", "invalidyear");
    }
    if (request.getFlagExport() == null || 
        (request.getFlagExport() != 1 && request.getFlagExport() != 2)) {
        throw new BadRequestAlertException("Invalid export flag", "report", "invalidflag");
    }
}
```

### 6.3 Logging
```java
log.info("Starting export for report: {}, year: {}, format: {}", 
    reportType, request.getYear(), request.getFlagExport() == 1 ? "Excel" : "PDF");
log.debug("Report data prepared: {} rows", dto.getList().size());
log.info("Report exported successfully: {}", result.getFileName());
```

### 6.4 Performance
- Cache template files trong Carbone
- Sử dụng connection pool cho OkHttpClient
- Async processing cho large reports
- Cleanup temp files sau khi upload

### 6.5 Security
- Validate file types
- Limit file size
- Use presigned URLs với expiry time
- Sanitize file names
- Validate user permissions

## 7. Template Structure

### 7.1 Template File Location
- Templates được lưu trong Carbone Server
- Tên template: `form_report5.xlsx`, `BieuSo6.docx`, etc.
- Template sử dụng Carbone syntax để bind dữ liệu

### 7.2 Template Syntax Examples
```
{d.unitName}                    // Simple binding
{d.year}                        // Number binding
{d.list[i].columnName}          // Array iteration
{d.list[i].value:ifEQ(0):show()} // Conditional
```

## 8. Troubleshooting

### 8.1 Carbone Connection Issues
```
Error: Cannot connect to Carbone Server
Solution: 
- Check carbone.base-url configuration
- Verify Carbone Server is running
- Check network connectivity
```

### 8.2 MinIO Upload Issues
```
Error: Failed to upload file to MinIO
Solution:
- Check MinIO credentials
- Verify bucket exists
- Check MinIO server status
- Verify network connectivity
```

### 8.3 Template Not Found
```
Error: Template not found
Solution:
- Verify template file exists in Carbone Server
- Check template file name spelling
- Upload template to Carbone Server
```

## 9. Testing

### 9.1 Unit Test Example
```java
@Test
void testExportExcelFormFive() {
    // Given
    ReportInputDTO request = new ReportInputDTO();
    request.setYear(2025);
    request.setPeriod(1);
    request.setFlagExport(1);
    
    ReportCarboneDTO dto = new ReportCarboneDTO();
    dto.setUnitName("Test Unit");
    dto.setYear(2025);
    
    CarboneResponseData expected = CarboneResponseData.builder()
        .fileName("test.xlsx")
        .url("/bucket/path/test.xlsx")
        .build();
    
    when(carboneService.renderReport(any(), any(), any(), any()))
        .thenReturn(expected);
    
    // When
    ResponseEntity<CarboneResponseData> result = 
        statisticReportService.exportExcelFormFive(request);
    
    // Then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(expected);
}
```

### 9.2 Integration Test
```java
@SpringBootTest
@AutoConfigureMockMvc
class ExportIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testExportEndpoint() throws Exception {
        mockMvc.perform(get("/api/statistical-report/form-five/export")
                .param("flagExport", "1")
                .param("year", "2025")
                .param("period", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileName").exists())
            .andExpect(jsonPath("$.url").exists());
    }
}
```

## 10. Monitoring & Metrics

### 10.1 Key Metrics
- Export request count
- Export success/failure rate
- Average export time
- File size distribution
- MinIO storage usage

### 10.2 Logging Points
- Request received
- Data preparation completed
- Carbone render started
- Carbone render completed
- MinIO upload completed
- Response sent

## Kết luận

Luồng export API với Carbone và MinIO bao gồm:
1. **Controller** nhận request và route
2. **Service** chuẩn bị dữ liệu từ database
3. **CarboneService** gọi Carbone API để render template
4. **Carbone Server** merge dữ liệu vào template và convert format
5. **MinIO** lưu trữ file đã render
6. **Response** trả về URL download cho client

Ưu điểm:
- Tách biệt logic render template (Carbone) và storage (MinIO)
- Scalable và maintainable
- Support nhiều format (Excel, PDF, Word)
- Presigned URL với expiry time bảo mật
- Template-based, dễ customize

Lưu ý khi implement:
- Validate input data
- Handle errors gracefully
- Log đầy đủ để troubleshoot
- Cleanup temp files
- Monitor performance
- Secure file access với presigned URLs
