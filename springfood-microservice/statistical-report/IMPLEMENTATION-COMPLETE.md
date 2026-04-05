# Carbone Integration - Implementation Complete ✅

## Tóm Tắt Thay Đổi

### 1. Dependencies Added
- ✅ MinIO module dependency trong `pom.xml`

### 2. Configuration Updated
- ✅ `application-dev.yml`: Thêm MinIO config với 2 buckets riêng
  - `template-bucket`: springfood-input (chứa templates)
  - `report-bucket`: springfood-reports (chứa reports đã render)

### 3. New Services Created
- ✅ `CarboneTemplateService`: Quản lý upload template và cache template ID
- ✅ `CarboneService` (refactored): Workflow hoàn chỉnh với MinIO

### 4. Workflow Mới

```
1. Download template từ MinIO (springfood-input bucket)
2. Upload template lên Carbone → nhận template ID (cached)
3. Render với template ID + data → nhận render ID
4. Download file đã render từ Carbone
5. Upload lên MinIO (springfood-reports bucket)
6. Trả về presigned URL (7 days expiry)
```

## Cách Sử Dụng

### Bước 1: Upload Template lên MinIO

Sử dụng MinIO Console (http://localhost:9001):
1. Login với `minioadmin` / `minioadmin`
2. Vào bucket `springfood-input`
3. Upload file template (vd: `monthly_report.xlsx`)

Hoặc dùng mc command:
```bash
mc cp monthly_report.xlsx myminio/springfood-input/
```

### Bước 2: Test API

```bash
curl -X POST "http://localhost:8090/api/statistical-reports/render?templateFileName=monthly_report.xlsx&reportName=my_report.pdf&convertTo=pdf" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Monthly Sales Report",
    "date": "2026-02-23",
    "summary": {
      "totalSales": 1000000,
      "totalOrders": 150
    },
    "items": [
      {"product": "Product A", "quantity": 50, "revenue": 500000},
      {"product": "Product B", "quantity": 100, "revenue": 500000}
    ]
  }'
```

### Bước 3: Expected Response

```json
{
  "fileName": "my_report.pdf",
  "url": "http://localhost:9000/springfood-reports/reports/uuid-here/my_report.pdf?X-Amz-Algorithm=...",
  "bucketName": "springfood-reports"
}
```

### Bước 4: Download File

```bash
curl -o report.pdf "<URL từ response>"
```

## API Endpoints

### POST /api/statistical-reports/render

**Parameters:**
- `templateFileName` (required): Tên template trong MinIO bucket `springfood-input`
- `reportName` (optional): Tên file output, nếu không có sẽ auto-generate
- `convertTo` (default: "pdf"): Format output ("pdf", "xlsx", "docx", "odt", "ods")

**Request Body:** JSON data để merge vào template

**Response:** CarboneResponseData với MinIO URL

## Template Syntax

Template sử dụng Carbone syntax:

```
{d.title}                          // Simple field
{d.date}                           // Date field
{d.summary.totalSales}             // Nested object
{d.items[i].product}               // Array iteration
{d.items[i].quantity}              // Array field
{d.items[i].revenue:formatN(0)}    // Format number
```

Xem thêm: https://carbone.io/documentation.html

## Troubleshooting

### Issue 1: Template not found

```
Error: Template not found in MinIO: monthly_report.xlsx
```

**Solution:** Upload template lên bucket `springfood-input`

### Issue 2: Carbone upload failed

```
Error: Failed to upload template: 413
```

**Solution:** Template quá lớn (>20MB), check Carbone limits

### Issue 3: Carbone render failed

```
Error: Carbone render failed: 500
```

**Solution:** 
- Check Carbone logs: `docker logs springfood-carbone`
- Verify template syntax
- Check data structure matches template

### Issue 4: MinIO upload failed

```
Error: Failed to upload to MinIO
```

**Solution:**
- Check MinIO is running: `docker ps | findstr minio`
- Verify bucket exists: `springfood-reports`
- Check MinIO credentials in config

## Performance Notes

### Template Caching
- Template ID được cache trong memory
- Lần đầu: Upload template → Lấy ID → Cache
- Lần sau: Dùng cached ID → Không upload lại
- Clear cache: Restart service hoặc gọi `templateService.clearCache()`

### Presigned URL
- URL có hiệu lực 7 ngày
- Sau 7 ngày cần generate URL mới
- File vẫn tồn tại trong MinIO, chỉ URL hết hạn

## Next Steps

### 1. Test với Real Template
- [ ] Tạo template Excel/Word với Carbone syntax
- [ ] Upload lên MinIO
- [ ] Test render với real data

### 2. Add Error Handling
- [ ] Custom exception classes
- [ ] Better error messages
- [ ] Retry logic cho network errors

### 3. Add Monitoring
- [ ] Metrics cho render time
- [ ] Success/failure rate
- [ ] Template cache hit rate

### 4. Add Unit Tests
- [ ] Test CarboneTemplateService
- [ ] Test CarboneService
- [ ] Mock MinIO và Carbone calls

### 5. Documentation
- [ ] API documentation (Swagger)
- [ ] Template creation guide
- [ ] Deployment guide

## Configuration Reference

### Environment Variables

```properties
# Carbone
CARBONE_BASE_URL=http://localhost:8100

# MinIO
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_TEMPLATE_BUCKET=springfood-input
MINIO_REPORT_BUCKET=springfood-reports
```

### Docker Compose

Carbone và MinIO đã được config trong `docker-compose.yml`:

```yaml
carbone:
  image: carbone/carbone-ee:latest
  ports:
    - "8100:4000"

minio:
  image: minio/minio
  ports:
    - "9000:9000"
    - "9001:9001"
```

## Code Structure

```
statistical-report/
├── src/main/java/.../service/carbone/
│   ├── CarboneService.java              # Main service (refactored)
│   ├── CarboneTemplateService.java      # Template management (new)
│   └── dto/
│       ├── CarboneResponseData.java
│       ├── CarboneOption.java
│       └── ...
├── src/main/resources/config/
│   └── application-dev.yml              # Updated config
└── pom.xml                              # Added minio dependency
```

## Summary

✅ **Hoàn thành:**
- MinIO integration với module có sẵn
- Template caching mechanism
- Complete workflow: MinIO → Carbone → MinIO
- Presigned URL generation
- Error handling và logging

⏳ **Cần làm tiếp:**
- Testing với real templates
- Unit tests
- Documentation
- Monitoring/metrics

---

**Implementation Date:** 2026-02-23  
**Status:** ✅ COMPLETE - Ready for Testing  
**Next:** Upload template và test API
