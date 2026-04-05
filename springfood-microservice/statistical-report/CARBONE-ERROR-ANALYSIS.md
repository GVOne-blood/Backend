# Phân Tích Lỗi 404 - Carbone Integration

## Lỗi Hiện Tại

```
ERROR: Carbone server error - Status: 404, URL: http://localhost:8100/render
RuntimeException: Carbone server response with error 404
```

## Nguyên Nhân Gốc Rễ

### 1. Endpoint SAI

**Code hiện tại:**
```java
String response = sendToReportCore("/render", body);
```
→ Gửi đến: `http://localhost:8100/render`

**Carbone API yêu cầu:**
```
POST /render/{templateId}
```
→ Phải gửi đến: `http://localhost:8100/render/{templateId}`

**Kết luận:** Thiếu `{templateId}` trong URL → 404 Not Found

---

### 2. Request Body SAI

**Code hiện tại gửi:**
```json
{
  "fileName": "template.xlsx",
  "options": {
    "convertTo": "pdf",
    "reportName": "report.pdf",
    "timezone": "Asia/Ho_Chi_Minh",
    "lang": "vi",
    "responseType": "url"
  },
  "data": {
    "title": "My Report"
  }
}
```

**Carbone API yêu cầu:**
```json
{
  "data": {
    "title": "My Report"
  },
  "convertTo": "pdf",
  "reportName": "report.pdf",
  "timezone": "Asia/Ho_Chi_Minh",
  "lang": "vi"
}
```

**Vấn đề:**
- ❌ Field `fileName` không tồn tại trong Carbone API
- ❌ Field `options` không tồn tại, các options phải nằm ở root level
- ❌ Field `responseType` không tồn tại trong Carbone API

---

## So Sánh Với Thiết Kế Gốc

### Thiết Kế Gốc (Từ carbone-minio-export-api.md)

Thiết kế gốc cũng có **CÙNG LỖI** này! Họ đang gửi:

```java
String response = sendToReportCore("/render", body);
```

Với body:
```json
{
  "fileName": "template.xlsx",
  "options": {...},
  "data": {...}
}
```

**Kết luận:** Thiết kế gốc KHÔNG ĐÚNG với Carbone API chính thức!

---

## Carbone API Chính Thức (Từ carbone.io)

### Workflow Đúng

#### Option 1: Upload Template Trước (Recommended)

**Bước 1: Upload Template**
```bash
POST /template
Content-Type: multipart/form-data

# Upload file template.xlsx
# Response: { "success": true, "data": { "templateId": "abc123..." } }
```

**Bước 2: Render Document**
```bash
POST /render/{templateId}
Content-Type: application/json
Authorization: Bearer API_TOKEN

{
  "data": {
    "title": "My Report"
  },
  "convertTo": "pdf",
  "reportName": "report.pdf",
  "timezone": "Asia/Ho_Chi_Minh",
  "lang": "vi"
}

# Response: { "success": true, "data": { "renderId": "xyz789..." } }
```

**Bước 3: Download Report**
```bash
GET /render/{renderId}

# Response: File binary
```

#### Option 2: Single API Call (V5+)

```bash
POST /render/template?download=true
Content-Type: application/json
Authorization: Bearer API_TOKEN

{
  "template": "BASE64_ENCODED_TEMPLATE_CONTENT",
  "data": {
    "title": "My Report"
  },
  "convertTo": "pdf",
  "reportName": "report.pdf",
  "timezone": "Asia/Ho_Chi_Minh",
  "lang": "vi"
}

# Response: File binary (direct download)
```

---

## Tại Sao Thiết Kế Gốc Lại Sai?

### Giả Thuyết 1: Họ Dùng Carbone On-Premise Custom

Có thể dự án gốc đang dùng **Carbone On-Premise** với **custom API wrapper** không phải Carbone chính thức.

Ví dụ: Họ có thể tự build một wrapper service:
```
Client → Custom Wrapper API → Carbone Server
```

Wrapper API này có thể:
- Accept endpoint `/render` (không cần templateId)
- Accept field `fileName` trong body
- Tự động upload template lên Carbone
- Tự động download result và upload lên MinIO
- Return MinIO URL

### Giả Thuyết 2: Tài Liệu Cũ/Sai

Tài liệu `carbone-minio-export-api.md` có thể:
- Được viết dựa trên Carbone version cũ
- Được viết dựa trên custom implementation
- Có lỗi trong documentation

---

## Giải Pháp

### Solution 1: Sử dụng Carbone API Chính Thức (Recommended)

#### 1.1 Thêm Template Management

```java
@Service
@RequiredArgsConstructor
public class CarboneTemplateService {
    private final OkHttpClient client;
    private final Gson gson;
    
    @Value("${carbone.base-url}")
    private String carboneBaseUrl;
    
    // Cache: templateFileName -> templateId
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();
    
    /**
     * Upload template và lấy template ID
     */
    public String uploadTemplate(File templateFile) throws IOException {
        // Check cache
        String cached = templateCache.get(templateFile.getName());
        if (cached != null) {
            return cached;
        }
        
        // Upload template
        RequestBody fileBody = RequestBody.create(
            Files.readAllBytes(templateFile.toPath()),
            MediaType.parse("application/octet-stream")
        );
        
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("template", templateFile.getName(), fileBody)
            .build();
        
        Request request = new Request.Builder()
            .url(carboneBaseUrl + "/template")
            .post(requestBody)
            .build();
        
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new RuntimeException("Failed to upload template: " + response.code());
        }
        
        // Parse response
        JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
        String templateId = json.getAsJsonObject("data").get("templateId").getAsString();
        
        // Cache it
        templateCache.put(templateFile.getName(), templateId);
        
        return templateId;
    }
    
    /**
     * Upload template từ byte array
     */
    public String uploadTemplate(byte[] templateBytes, String fileName) throws IOException {
        // Similar implementation
    }
}
```

#### 1.2 Update CarboneService

```java
@Service
@RequiredArgsConstructor
public class CarboneService {
    private final OkHttpClient client;
    private final Gson gson;
    private final CarboneTemplateService templateService;
    
    @Value("${carbone.base-url}")
    private String carboneBaseUrl;
    
    @Value("${carbone.template-path}")
    private String templatePath;
    
    /**
     * Render report với template file
     */
    public CarboneResponseData renderReport(
        Object data, 
        String templateFileName, 
        String reportName, 
        String convertTo
    ) {
        try {
            // 1. Upload template và lấy template ID
            File templateFile = new File(templatePath, templateFileName);
            String templateId = templateService.uploadTemplate(templateFile);
            
            // 2. Build request body (ĐÚNG format)
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("data", data);
            requestBody.put("convertTo", convertTo);
            requestBody.put("reportName", reportName);
            requestBody.put("timezone", "Asia/Ho_Chi_Minh");
            requestBody.put("lang", "vi");
            
            String jsonBody = gson.toJson(requestBody);
            
            // 3. Render document
            String renderId = renderWithTemplateId(templateId, jsonBody);
            
            // 4. Download result
            return downloadReport(renderId);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to render report", e);
        }
    }
    
    private String renderWithTemplateId(String templateId, String jsonBody) throws IOException {
        Request request = new Request.Builder()
            .url(carboneBaseUrl + "/render/" + templateId)  // ← ĐÚNG endpoint
            .post(RequestBody.create(jsonBody.getBytes()))
            .addHeader("Content-Type", "application/json")
            .build();
        
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new RuntimeException("Carbone render failed: " + response.code());
        }
        
        JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
        return json.getAsJsonObject("data").get("renderId").getAsString();
    }
    
    private CarboneResponseData downloadReport(String renderId) throws IOException {
        Request request = new Request.Builder()
            .url(carboneBaseUrl + "/render/" + renderId)
            .get()
            .build();
        
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new RuntimeException("Failed to download report: " + response.code());
        }
        
        // Get file bytes
        byte[] fileBytes = response.body().bytes();
        
        // TODO: Upload to MinIO
        // String minioUrl = minioService.uploadFile(fileBytes, reportName, contentType);
        
        return CarboneResponseData.builder()
            .url("/render/" + renderId)  // Temporary
            .fileName(getFileNameFromHeaders(response))
            .build();
    }
}
```

### Solution 2: Sử dụng Single API Call (V5+)

```java
public CarboneResponseData renderReportDirect(
    Object data,
    String templateFileName,
    String reportName,
    String convertTo
) throws IOException {
    // 1. Read template file
    File templateFile = new File(templatePath, templateFileName);
    byte[] templateBytes = Files.readAllBytes(templateFile.toPath());
    String templateBase64 = Base64.getEncoder().encodeToString(templateBytes);
    
    // 2. Build request body
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("template", templateBase64);
    requestBody.put("data", data);
    requestBody.put("convertTo", convertTo);
    requestBody.put("reportName", reportName);
    requestBody.put("timezone", "Asia/Ho_Chi_Minh");
    requestBody.put("lang", "vi");
    
    String jsonBody = gson.toJson(requestBody);
    
    // 3. Call API with download=true
    Request request = new Request.Builder()
        .url(carboneBaseUrl + "/render/template?download=true")  // ← ĐÚNG endpoint
        .post(RequestBody.create(jsonBody.getBytes()))
        .addHeader("Content-Type", "application/json")
        .build();
    
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
        throw new RuntimeException("Carbone render failed: " + response.code());
    }
    
    // 4. Get file directly
    byte[] fileBytes = response.body().bytes();
    
    // 5. Upload to MinIO
    // String minioUrl = minioService.uploadFile(fileBytes, reportName, contentType);
    
    return CarboneResponseData.builder()
        .fileName(reportName)
        .build();
}
```

### Solution 3: Tìm Custom Wrapper API

Nếu dự án gốc thực sự có custom wrapper, cần:

1. Tìm source code của wrapper API
2. Deploy wrapper API
3. Point `carbone.base-url` đến wrapper thay vì Carbone trực tiếp

---

## Khuyến Nghị

### Ưu tiên 1: Implement Solution 2 (Single API Call)

**Lý do:**
- Đơn giản nhất, chỉ 1 API call
- Không cần quản lý template ID cache
- Phù hợp với use case hiện tại (template từ filesystem)

**Steps:**
1. Update `CarboneService.renderReport()` để gửi template dưới dạng base64
2. Fix endpoint thành `/render/template?download=true`
3. Fix request body structure (bỏ `fileName` và `options`)
4. Test với Carbone server

### Ưu tiên 2: Implement Solution 1 (Standard Workflow)

**Lý do:**
- Chuẩn theo Carbone best practices
- Template được cache, không cần upload lại
- Scalable hơn cho production

**Steps:**
1. Tạo `CarboneTemplateService` với template cache
2. Update `CarboneService` để sử dụng template ID
3. Implement 3-step workflow: upload → render → download
4. Add MinIO integration để lưu file

### Ưu tiên 3: Tìm Custom Wrapper (Nếu tồn tại)

**Steps:**
1. Hỏi team dự án gốc về wrapper API
2. Check source code dự án gốc xem có wrapper không
3. Nếu có, deploy và sử dụng

---

## Testing

### Test 1: Verify Carbone Server

```bash
# Check Carbone server status
curl http://localhost:8100/status

# Expected: {"success": true, "version": "..."}
```

### Test 2: Test Upload Template

```bash
curl -X POST http://localhost:8100/template \
  -F "template=@template.xlsx"

# Expected: {"success": true, "data": {"templateId": "..."}}
```

### Test 3: Test Render

```bash
curl -X POST http://localhost:8100/render/{templateId} \
  -H "Content-Type: application/json" \
  -d '{
    "data": {"title": "Test"},
    "convertTo": "pdf"
  }'

# Expected: {"success": true, "data": {"renderId": "..."}}
```

---

## Kết Luận

**Lỗi 404 xảy ra vì:**
1. ❌ Endpoint sai: `/render` thay vì `/render/{templateId}` hoặc `/render/template`
2. ❌ Request body sai: có `fileName` và `options` không hợp lệ
3. ❌ Thiết kế gốc không đúng với Carbone API chính thức

**Giải pháp:**
- Implement lại theo Carbone API chính thức
- Sử dụng Solution 2 (Single API Call) cho đơn giản
- Hoặc Solution 1 (Standard Workflow) cho production-ready

**Next Steps:**
1. Chọn solution (recommend Solution 2)
2. Implement code changes
3. Test với Carbone server
4. Add MinIO integration
5. Update documentation

---

**Người phân tích:** Kiro AI Assistant  
**Ngày:** 2026-02-23  
**Version:** 1.0
