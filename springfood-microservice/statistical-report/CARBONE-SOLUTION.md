# Giải Pháp Carbone Integration - Community Edition

## Vấn Đề Hiện Tại

1. ❌ Carbone đang chạy **Community Edition** (không có license)
2. ❌ Community Edition **KHÔNG CÓ S3/MinIO plugin**
3. ❌ Code đang gửi sai endpoint và format
4. ❌ Thiếu workflow upload template và download result

## Workflow Đúng (Community Edition)

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ 1. Request render
       ▼
┌─────────────────────┐
│  Your Service       │
│  (Spring Boot)      │
└──────┬──────────────┘
       │ 2. Download template
       ▼
┌─────────────────────┐
│     MinIO           │
│  (Template Storage) │
└──────┬──────────────┘
       │ 3. Template bytes
       ▼
┌─────────────────────┐
│  Your Service       │
└──────┬──────────────┘
       │ 4. Upload template (multipart)
       ▼
┌─────────────────────┐
│  Carbone Server     │
│  (Community Ed)     │
└──────┬──────────────┘
       │ 5. Template ID
       ▼
┌─────────────────────┐
│  Your Service       │
└──────┬──────────────┘
       │ 6. Render with template ID + data
       ▼
┌─────────────────────┐
│  Carbone Server     │
└──────┬──────────────┘
       │ 7. Render ID
       ▼
┌─────────────────────┐
│  Your Service       │
└──────┬──────────────┘
       │ 8. Download rendered file
       ▼
┌─────────────────────┐
│  Carbone Server     │
└──────┬──────────────┘
       │ 9. File bytes
       ▼
┌─────────────────────┐
│  Your Service       │
└──────┬──────────────┘
       │ 10. Upload to MinIO
       ▼
┌─────────────────────┐
│     MinIO           │
│  (Result Storage)   │
└──────┬──────────────┘
       │ 11. MinIO URL
       ▼
┌─────────────────────┐
│  Your Service       │
└──────┬──────────────┘
       │ 12. Return URL
       ▼
┌─────────────┐
│   Client    │
└─────────────┘
```

## Implementation

### Step 1: Thêm MinIO Service

```java
@Service
@RequiredArgsConstructor
public class MinioFileService {
    
    @Value("${minio.url}")
    private String minioUrl;
    
    @Value("${minio.access-key}")
    private String accessKey;
    
    @Value("${minio.secret-key}")
    private String secretKey;
    
    @Value("${minio.bucket-templates}")
    private String templateBucket;
    
    @Value("${minio.bucket-reports}")
    private String reportBucket;
    
    private MinioClient minioClient;
    
    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
            .endpoint(minioUrl)
            .credentials(accessKey, secretKey)
            .build();
    }
    
    /**
     * Download template từ MinIO
     */
    public byte[] downloadTemplate(String templateName) throws Exception {
        try (InputStream stream = minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(templateBucket)
                .object(templateName)
                .build()
        )) {
            return stream.readAllBytes();
        }
    }
    
    /**
     * Upload rendered file lên MinIO
     */
    public String uploadReport(byte[] fileBytes, String fileName, String contentType) throws Exception {
        String objectName = "reports/" + UUID.randomUUID() + "/" + fileName;
        
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(reportBucket)
                .object(objectName)
                .stream(new ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                .contentType(contentType)
                .build()
        );
        
        // Generate presigned URL (7 days expiry)
        String url = minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(reportBucket)
                .object(objectName)
                .expiry(7, TimeUnit.DAYS)
                .build()
        );
        
        return url;
    }
}
```

### Step 2: Tạo Carbone Template Service

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CarboneTemplateService {
    
    private final OkHttpClient client;
    
    @Value("${carbone.base-url}")
    private String carboneBaseUrl;
    
    // Cache: templateName -> templateId
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();
    
    /**
     * Upload template lên Carbone và lấy template ID
     */
    public String uploadTemplate(byte[] templateBytes, String templateName) throws IOException {
        // Check cache
        String cachedId = templateCache.get(templateName);
        if (cachedId != null) {
            log.debug("Using cached template ID for: {}", templateName);
            return cachedId;
        }
        
        // Upload template
        RequestBody fileBody = RequestBody.create(
            templateBytes,
            MediaType.parse("application/octet-stream")
        );
        
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("template", templateName, fileBody)
            .build();
        
        Request request = new Request.Builder()
            .url(carboneBaseUrl + "/template")
            .post(requestBody)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("Failed to upload template: {} - {}", response.code(), errorBody);
                throw new RuntimeException("Failed to upload template: " + response.code());
            }
            
            String responseBody = response.body().string();
            log.debug("Upload template response: {}", responseBody);
            
            // Parse response: {"success": true, "data": {"templateId": "..."}}
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            String templateId = json.getAsJsonObject("data").get("templateId").getAsString();
            
            // Cache it
            templateCache.put(templateName, templateId);
            log.info("Template uploaded successfully: {} -> {}", templateName, templateId);
            
            return templateId;
        }
    }
    
    /**
     * Clear cache (useful khi template được update)
     */
    public void clearCache(String templateName) {
        templateCache.remove(templateName);
    }
    
    public void clearAllCache() {
        templateCache.clear();
    }
}
```

### Step 3: Update Carbone Service

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CarboneService {
    
    private final OkHttpClient client;
    private final Gson gson;
    private final CarboneTemplateService templateService;
    private final MinioFileService minioService;
    
    @Value("${carbone.base-url}")
    private String carboneBaseUrl;
    
    /**
     * Render report với template từ MinIO
     */
    public CarboneResponseData renderReport(
        Object data,
        String templateName,
        String reportName,
        String convertTo
    ) {
        try {
            // Step 1: Download template từ MinIO
            log.info("Downloading template from MinIO: {}", templateName);
            byte[] templateBytes = minioService.downloadTemplate(templateName);
            
            // Step 2: Upload template lên Carbone
            log.info("Uploading template to Carbone: {}", templateName);
            String templateId = templateService.uploadTemplate(templateBytes, templateName);
            
            // Step 3: Render document
            log.info("Rendering document with template ID: {}", templateId);
            String renderId = renderWithTemplateId(templateId, data, convertTo, reportName);
            
            // Step 4: Download rendered file
            log.info("Downloading rendered file: {}", renderId);
            byte[] renderedBytes = downloadRenderedFile(renderId);
            
            // Step 5: Upload lên MinIO
            log.info("Uploading rendered file to MinIO: {}", reportName);
            String contentType = getContentType(convertTo);
            String minioUrl = minioService.uploadReport(renderedBytes, reportName, contentType);
            
            // Step 6: Return response
            return CarboneResponseData.builder()
                .fileName(reportName)
                .url(minioUrl)
                .bucketName(minioService.getReportBucket())
                .build();
                
        } catch (Exception e) {
            log.error("Failed to render report", e);
            throw new RuntimeException("Failed to render report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Render document với template ID
     */
    private String renderWithTemplateId(
        String templateId,
        Object data,
        String convertTo,
        String reportName
    ) throws IOException {
        // Build request body theo Carbone API format
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", data);
        requestBody.put("convertTo", convertTo);
        requestBody.put("reportName", reportName);
        requestBody.put("timezone", "Asia/Ho_Chi_Minh");
        requestBody.put("lang", "vi");
        
        String jsonBody = gson.toJson(requestBody);
        log.debug("Render request body: {}", jsonBody);
        
        Request request = new Request.Builder()
            .url(carboneBaseUrl + "/render/" + templateId)
            .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
            .addHeader("Content-Type", "application/json")
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("Carbone render failed: {} - {}", response.code(), errorBody);
                throw new RuntimeException("Carbone render failed: " + response.code());
            }
            
            String responseBody = response.body().string();
            log.debug("Render response: {}", responseBody);
            
            // Parse response: {"success": true, "data": {"renderId": "..."}}
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            return json.getAsJsonObject("data").get("renderId").getAsString();
        }
    }
    
    /**
     * Download rendered file từ Carbone
     */
    private byte[] downloadRenderedFile(String renderId) throws IOException {
        Request request = new Request.Builder()
            .url(carboneBaseUrl + "/render/" + renderId)
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("Failed to download rendered file: {} - {}", response.code(), errorBody);
                throw new RuntimeException("Failed to download file: " + response.code());
            }
            
            return response.body().bytes();
        }
    }
    
    private String getContentType(String convertTo) {
        return switch (convertTo.toLowerCase()) {
            case "pdf" -> "application/pdf";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "odt" -> "application/vnd.oasis.opendocument.text";
            case "ods" -> "application/vnd.oasis.opendocument.spreadsheet";
            default -> "application/octet-stream";
        };
    }
}
```

### Step 4: Update Configuration

```yaml
# application-dev.yml
carbone:
  base-url: ${CARBONE_BASE_URL:http://localhost:8100}

minio:
  url: ${MINIO_URL:http://localhost:9000}
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin}
  bucket-templates: ${MINIO_BUCKET_TEMPLATES:springfood-input}
  bucket-reports: ${MINIO_BUCKET_REPORTS:springfood-reports}
```

### Step 5: Add MinIO Dependency

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.7</version>
</dependency>
```

### Step 6: Update Controller (Optional - nếu cần)

```java
@RestController
@RequestMapping("/api/statistical-reports")
@RequiredArgsConstructor
public class StatisticalReportResource {
    
    private final CarboneService carboneService;
    
    @PostMapping("/render")
    public ResponseEntity<CarboneResponseData> renderReport(
        @RequestParam String templateName,
        @RequestParam(required = false) String reportName,
        @RequestParam(defaultValue = "pdf") String convertTo,
        @RequestBody Map<String, Object> data
    ) {
        if (reportName == null || reportName.isBlank()) {
            reportName = "report_" + System.currentTimeMillis() + "." + convertTo;
        }
        
        CarboneResponseData result = carboneService.renderReport(
            data, 
            templateName, 
            reportName, 
            convertTo
        );
        
        return ResponseEntity.ok(result);
    }
}
```

## Testing

### 1. Upload template lên MinIO

```bash
# Sử dụng MinIO Console: http://localhost:9001
# Hoặc mc command:
mc cp template.xlsx myminio/springfood-input/
```

### 2. Test API

```bash
curl -X POST "http://localhost:8090/api/statistical-reports/render?templateName=template.xlsx&reportName=my_report.pdf&convertTo=pdf" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Report",
    "date": "2026-02-23",
    "items": [
      {"name": "Item 1", "value": 100},
      {"name": "Item 2", "value": 200}
    ]
  }'
```

### 3. Expected Response

```json
{
  "fileName": "my_report.pdf",
  "url": "http://localhost:9000/springfood-reports/reports/uuid/my_report.pdf?X-Amz-...",
  "bucketName": "springfood-reports"
}
```

### 4. Download file

```bash
curl -o report.pdf "<URL từ response>"
```

## Troubleshooting

### Issue 1: Template not found in MinIO

```
Error: The specified key does not exist
```

**Solution:** Upload template lên MinIO bucket `springfood-input`

### Issue 2: Carbone upload template failed

```
Error: Failed to upload template: 413
```

**Solution:** Template quá lớn, check `CARBONE_EE_MAXTEMPLATE_SIZE`

### Issue 3: Carbone render timeout

```
Error: Carbone render failed: 500
```

**Solution:** 
- Check Carbone logs: `docker logs springfood-carbone`
- Increase timeout nếu cần

### Issue 4: MinIO upload failed

```
Error: Access Denied
```

**Solution:** Check MinIO credentials và bucket permissions

## Performance Optimization

### 1. Template Caching

Template ID được cache trong memory, không cần upload lại mỗi lần render.

### 2. Connection Pooling

OkHttpClient đã có connection pooling mặc định.

### 3. Async Processing

Nếu render lâu, có thể implement async:

```java
@Async
public CompletableFuture<CarboneResponseData> renderReportAsync(...) {
    return CompletableFuture.completedFuture(renderReport(...));
}
```

## Next Steps

1. ✅ Implement MinioFileService
2. ✅ Implement CarboneTemplateService
3. ✅ Update CarboneService
4. ✅ Update configuration
5. ✅ Add MinIO dependency
6. ⏳ Test với real template
7. ⏳ Add error handling
8. ⏳ Add monitoring/metrics
9. ⏳ Add unit tests
10. ⏳ Update documentation

---

**Tác giả:** Kiro AI Assistant  
**Ngày:** 2026-02-23  
**Version:** 1.0
