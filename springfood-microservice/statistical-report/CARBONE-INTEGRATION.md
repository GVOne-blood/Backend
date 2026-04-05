# Carbone Integration Guide

## Tổng quan

Statistical Report service đã được tích hợp với Carbone Server để generate reports từ templates.

Service hỗ trợ 2 modes:
1. **Local Templates**: Template từ filesystem (development/fixed templates)
2. **MinIO Templates**: Template từ MinIO bucket (production/dynamic templates)

## Carbone API Flow

1. **Upload Template** → Nhận template ID từ Carbone
2. **Render Document** → Gửi data + template ID → Nhận render ID  
3. **Download Result** → Sử dụng render ID để download file

**Lưu ý:** Template ID là ID nội bộ của Carbone server, khác với MinIO object name.

## Cấu hình

### Docker Compose

Carbone server đã được cấu hình trong `docker-compose.yml`:

```yaml
carbone:
  image: carbone/carbone-ee:latest
  container_name: springfood-carbone
  ports:
    - "8100:4000"  # External:Internal
  environment:
    CARBONE_STORAGE_PATH: /app/storage
```

### Application Config

File: `statistical-report/src/main/resources/config/application-dev.yml`

```yaml
carbone:
  base-url: ${CARBONE_BASE_URL:http://localhost:8100}
  template-path: ${CARBONE_TEMPLATE_PATH:statistical-report/src/main/resources/templates/reports}
  minio-bucket: ${CARBONE_MINIO_BUCKET:springfood-templates}
```

**Cách hoạt động:**
- Nếu `minio-bucket` được config → Service sẽ download template từ MinIO
- Nếu `minio-bucket` không được config → Service sẽ dùng template từ local filesystem

### Environment Variables

File: `.env`

```properties
CARBONE_BASE_URL=http://localhost:8100
CARBONE_MINIO_BUCKET=springfood-templates
```

## Mode 1: Local Templates (Development)

### Setup

1. Đặt template files vào thư mục:
```
statistical-report/src/main/resources/templates/reports/
```

2. Không config `CARBONE_MINIO_BUCKET` hoặc để trống

### Usage

```bash
curl -X POST "http://localhost:8090/api/statistical-reports/render?templateFileName=monthly_report.odt&convertTo=pdf" \
  -H "Content-Type: application/json" \
  -d '{"title": "Report", "data": [...]}'
```

## Mode 2: MinIO Templates (Production)

### Setup

1. Upload template lên MinIO bucket `springfood-templates`:

```bash
# Sử dụng MinIO Console (http://localhost:9001)
# Hoặc mc command line:
mc cp monthly_report.odt myminio/springfood-templates/
```

2. Config `CARBONE_MINIO_BUCKET=springfood-templates` trong `.env`

### Workflow

```
User uploads template → MinIO bucket (springfood-templates)
                              ↓
Service nhận request render → Download template từ MinIO
                              ↓
                        Upload lên Carbone server
                              ↓
                        Nhận template ID (cached)
                              ↓
                        Render với data
                              ↓
                        Trả về download URL
```

### Usage

```bash
# Template "monthly_report.odt" đã được upload lên MinIO trước đó
curl -X POST "http://localhost:8090/api/statistical-reports/render?templateFileName=monthly_report.odt&convertTo=pdf" \
  -H "Content-Type: application/json" \
  -d '{"title": "Report", "data": [...]}'
```

**Lưu ý:** 
- `templateFileName` là tên object trong MinIO bucket
- Service tự động download từ MinIO, không cần path đầy đủ
- Template ID được cache để tránh upload lại

## API Reference

### Endpoint: POST /api/statistical-reports/render

**Parameters:**
- `templateFileName` (required): Tên file template
  - Mode Local: tên file trong `templates/reports/`
  - Mode MinIO: tên object trong MinIO bucket
- `reportName` (optional): Tên file output (vd: "report_january")
- `convertTo` (default: "pdf"): Format output ("pdf", "xlsx", "docx")

**Request Body:** JSON data để điền vào template

**Response:**

```json
{
  "url": "http://localhost:8100/render/abc123def456",
  "fileName": "report.pdf",
  "bucketName": "springfood-templates"  // Chỉ có khi dùng MinIO mode
}
```

### Download File

```bash
curl -o report.pdf "http://localhost:8100/render/abc123def456"
```

## Template Syntax

Carbone sử dụng syntax đặc biệt trong templates:

### Basic Variables
```
{d.title}
{d.month}
{d.totalSales}
```

### Loops
```
{d.items[i].product}
{d.items[i].quantity}
{d.items[i].revenue}
```

### Formatters
```
{d.totalSales:formatN(2)}  // Format number với 2 chữ số thập phân
{d.date:formatD(DD/MM/YYYY)}  // Format date
```

Xem thêm: https://carbone.io/documentation.html

## Code Structure

### CarboneService Methods

```java
// Auto-detect mode (MinIO or Local)
CarboneResponseData renderReport(Object data, String templateFileName, String convertTo)

// Explicit MinIO mode
CarboneResponseData renderReportFromMinIO(Object data, String templateName, String convertTo)

// Explicit Local mode
CarboneResponseData renderReportFromLocal(Object data, String templateFileName, String convertTo)

// With custom report name
CarboneResponseData renderReport(Object data, String templateFileName, String reportName, String convertTo)

// Clear cache
void clearTemplateCache()
```

### Template ID Cache

Service cache template IDs để tối ưu performance:
- Key: Template filename
- Value: Carbone template ID
- Cache được clear khi restart service

## So sánh 2 Modes

| Feature | Local Templates | MinIO Templates |
|---------|----------------|-----------------|
| Template Storage | Filesystem | MinIO Bucket |
| Upload Template | Manual (code deploy) | Dynamic (via MinIO) |
| Template Update | Cần redeploy service | Upload mới lên MinIO |
| Use Case | Fixed templates, development | Dynamic templates, production |
| Performance | Nhanh hơn (no download) | Chậm hơn (download từ MinIO) |
| Scalability | Không scale (mỗi instance cần copy) | Scale tốt (shared storage) |

## Troubleshooting

### 1. Carbone Server không chạy

```bash
docker ps | grep carbone
docker logs springfood-carbone
```

### 2. Template không tìm thấy (Local Mode)

```bash
ls -la statistical-report/src/main/resources/templates/reports/
```

### 3. Template không tìm thấy (MinIO Mode)

```bash
# Check MinIO bucket
mc ls myminio/springfood-templates/

# Hoặc qua MinIO Console: http://localhost:9001
```

### 4. MinIO connection error

Kiểm tra MinIO config trong `common` module và đảm bảo MinIO đang chạy:
```bash
docker ps | grep minio
curl http://localhost:9000/minio/health/live
```

### 5. Template ID cache issues

Clear cache bằng cách restart service hoặc gọi `clearTemplateCache()` method.

## Testing

### Test Carbone Server

```bash
# Health check
curl http://localhost:8100/

# Expected: {"success":true,"code":200,"message":"OK","version":"5.1.1"}
```

### Test MinIO Mode

1. Upload template lên MinIO:
```bash
mc cp test_template.odt myminio/springfood-templates/
```

2. Verify upload:
```bash
mc ls myminio/springfood-templates/
```

3. Test render:
```bash
curl -X POST "http://localhost:8090/api/statistical-reports/render?templateFileName=test_template.odt&convertTo=pdf" \
  -H "Content-Type: application/json" \
  -d '{"title": "Test Report"}'
```

4. Download result:
```bash
curl -o test.pdf "<URL từ response>"
```

## Production Recommendations

1. **Sử dụng MinIO Mode** cho production để:
   - Template có thể update mà không cần redeploy
   - Shared storage giữa các instances
   - Dễ quản lý templates qua MinIO Console

2. **Template Versioning**: Đặt tên template có version
   ```
   monthly_report_v1.odt
   monthly_report_v2.odt
   ```

3. **Monitoring**: Monitor Carbone server logs và MinIO access logs

4. **Backup**: Backup MinIO bucket `springfood-templates` thường xuyên

## References

- Carbone Documentation: https://carbone.io/documentation.html
- Carbone API Reference: https://carbone.io/api-reference.html
- MinIO Documentation: https://min.io/docs/minio/linux/index.html
