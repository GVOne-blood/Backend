# Media Upload Service - API Design

## Tổng quan

Hệ thống upload file cho media service hỗ trợ:
- Avatar (single file)
- Ảnh (batch 10-20 files)
- Video (single/multiple)
- Văn phòng (PDF, DOCX, XLSX, PPTX)

## Hạ tầng hiện tại

### Storage
- **MinIO Object Storage**: localhost:9000
- **Bucket**: `images` (auto-created)
- **Console UI**: localhost:9002
- **Credentials**: minioadmin/minioadmin
- **Max file size**: 50MB (configured in application-dev.yml)

### Database
- **PostgreSQL**: localhost:5432
- **Database**: springfood
- **Table**: media_file

### Enums
- `FileType`: IMAGE, VIDEO, AUDIO, DOCUMENT, OTHER
- `UploadStatus`: UPLOADING, PROCESSING, COMPLETED, FAILED, CANCELLED, VALIDATING, REJECTED
- `UploadModule`: PRODUCT, CHAT, ORDER, USER, SHOP, REPORT, NOTIFICATION, MARKETING, SYSTEM, OTHER

---

## API Endpoints

### 1. Upload Single File (Avatar, Single Image)

**Endpoint**: `POST /api/media/upload/single`

**Use Cases**:
- Upload avatar
- Upload single product image
- Upload single document

**Request**:
```http
POST /api/media/upload/single
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (required)
- module: UploadModule (required) - USER, PRODUCT, SHOP, etc.
- description: String (optional)
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "uuid-string",
    "fileOriginalName": "avatar.jpg",
    "fileStoredName": "20240306_uuid_avatar.jpg",
    "fileType": "IMAGE",
    "fileSize": 1024,
    "fileUrl": "http://localhost:9000/images/20240306_uuid_avatar.jpg",
    "fileHash": "sha256-hash",
    "uploadStatus": "COMPLETED",
    "uploadModule": "USER",
    "createdDate": "2024-03-06T00:00:00Z"
  },
  "message": "File uploaded successfully"
}
```

---

### 2. Upload Multiple Files (Batch Images)

**Endpoint**: `POST /api/media/upload/batch`

**Use Cases**:
- Upload 10-20 product images
- Upload multiple documents
- Upload gallery images

**Request**:
```http
POST /api/media/upload/batch
Content-Type: multipart/form-data

Parameters:
- files: List<MultipartFile> (required, max 20 files)
- module: UploadModule (required)
- descriptions: List<String> (optional, same order as files)
```

**Response**:
```json
{
  "success": true,
  "data": {
    "totalFiles": 15,
    "successCount": 14,
    "failedCount": 1,
    "uploadedFiles": [
      {
        "id": "uuid-1",
        "fileOriginalName": "product1.jpg",
        "fileUrl": "http://localhost:9000/images/...",
        "uploadStatus": "COMPLETED"
      }
    ],
    "failedFiles": [
      {
        "fileName": "invalid.exe",
        "reason": "File type not allowed"
      }
    ]
  },
  "message": "Batch upload completed with 14/15 files"
}
```

---

### 3. Upload Video

**Endpoint**: `POST /api/media/upload/video`

**Use Cases**:
- Upload product demo video
- Upload marketing video

**Request**:
```http
POST /api/media/upload/video
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (required, max 50MB)
- module: UploadModule (required)
- description: String (optional)
- thumbnail: MultipartFile (optional) - video thumbnail
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "uuid-string",
    "fileOriginalName": "demo.mp4",
    "fileType": "VIDEO",
    "fileSize": 45000,
    "fileUrl": "http://localhost:9000/images/videos/demo.mp4",
    "thumbnailUrl": "http://localhost:9000/images/thumbnails/demo_thumb.jpg",
    "uploadStatus": "PROCESSING",
    "uploadModule": "PRODUCT"
  },
  "message": "Video uploaded, processing in background"
}
```

---

### 4. Get Upload Status

**Endpoint**: `GET /api/media/upload/status/{fileId}`

**Use Cases**:
- Check video processing status
- Poll upload progress

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "uuid-string",
    "uploadStatus": "PROCESSING",
    "progress": 75,
    "message": "Processing video..."
  }
}
```

---

### 5. Get File Info

**Endpoint**: `GET /api/media/files/{fileId}`

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "uuid-string",
    "fileOriginalName": "avatar.jpg",
    "fileStoredName": "20240306_uuid_avatar.jpg",
    "fileType": "IMAGE",
    "fileSize": 1024,
    "fileUrl": "http://localhost:9000/images/...",
    "uploadStatus": "COMPLETED",
    "uploadModule": "USER",
    "isActive": true,
    "createdBy": "user123",
    "createdDate": "2024-03-06T00:00:00Z"
  }
}
```

---

### 6. List Files by Module

**Endpoint**: `GET /api/media/files`

**Query Parameters**:
- `module`: UploadModule (optional)
- `fileType`: FileType (optional)
- `status`: UploadStatus (optional)
- `page`: int (default 0)
- `size`: int (default 20)
- `sort`: string (default "createdDate,desc")

**Response**:
```json
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

---

### 7. Delete File

**Endpoint**: `DELETE /api/media/files/{fileId}`

**Response**:
```json
{
  "success": true,
  "message": "File deleted successfully"
}
```

---

### 8. Soft Delete (Deactivate)

**Endpoint**: `PATCH /api/media/files/{fileId}/deactivate`

**Response**:
```json
{
  "success": true,
  "message": "File deactivated successfully"
}
```

---

## File Validation Rules

### Allowed File Types

**Images**:
- Extensions: `.jpg`, `.jpeg`, `.png`, `.gif`, `.webp`, `.svg`
- Max size: 10MB
- MIME types: `image/jpeg`, `image/png`, `image/gif`, `image/webp`, `image/svg+xml`

**Videos**:
- Extensions: `.mp4`, `.avi`, `.mov`, `.wmv`, `.flv`, `.webm`
- Max size: 50MB
- MIME types: `video/mp4`, `video/avi`, `video/quicktime`, `video/x-ms-wmv`

**Documents**:
- Extensions: `.pdf`, `.doc`, `.docx`, `.xls`, `.xlsx`, `.ppt`, `.pptx`, `.txt`
- Max size: 20MB
- MIME types: `application/pdf`, `application/msword`, `application/vnd.openxmlformats-officedocument.*`

**Audio**:
- Extensions: `.mp3`, `.wav`, `.ogg`, `.m4a`
- Max size: 20MB
- MIME types: `audio/mpeg`, `audio/wav`, `audio/ogg`

---

## Error Responses

### Validation Error
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "File type not allowed",
    "details": {
      "fileName": "malicious.exe",
      "allowedTypes": ["jpg", "png", "pdf"]
    }
  }
}
```

### File Too Large
```json
{
  "success": false,
  "error": {
    "code": "FILE_TOO_LARGE",
    "message": "File size exceeds maximum allowed size",
    "details": {
      "fileSize": 60000000,
      "maxSize": 50000000
    }
  }
}
```

### Storage Error
```json
{
  "success": false,
  "error": {
    "code": "STORAGE_ERROR",
    "message": "Failed to upload file to storage",
    "details": {
      "reason": "MinIO connection timeout"
    }
  }
}
```

---

## Security Considerations

1. **Authentication**: All endpoints require JWT token
2. **File Validation**: 
   - Check file extension
   - Verify MIME type
   - Scan file content (magic bytes)
   - Virus scanning (optional)
3. **Rate Limiting**: Max 100 uploads per user per hour
4. **File Size Limits**: Enforced at application and nginx level
5. **Sanitization**: Remove special characters from filenames

---

## Implementation Components

### Required Classes

1. **DTOs**:
   - `FileUploadRequest`
   - `FileUploadResponse`
   - `BatchUploadRequest`
   - `BatchUploadResponse`
   - `FileInfoResponse`

2. **Services**:
   - `MediaFileService` - Business logic
   - `FileStorageService` - MinIO integration
   - `FileValidationService` - Validation logic
   - `FileProcessingService` - Async processing (video thumbnails, etc.)

3. **Repository**:
   - `MediaFileRepository` - JPA repository

4. **Controller**:
   - `MediaFileController` - REST endpoints

5. **Utils**:
   - `FileTypeDetector` - Detect file type from content
   - `FileHashGenerator` - Generate SHA-256 hash
   - `FileNameSanitizer` - Clean filenames

---

## Async Processing Flow

### Video Upload Flow
```
1. Client uploads video → UPLOADING status
2. Save to MinIO → PROCESSING status
3. Background job:
   - Generate thumbnail
   - Extract metadata
   - Validate video format
4. Update status → COMPLETED/FAILED
5. Notify client via WebSocket (optional)
```

### Batch Upload Flow
```
1. Receive 15 files
2. Validate all files first
3. Upload valid files in parallel (ThreadPoolExecutor)
4. Track progress for each file
5. Return summary response
```

---

## Configuration

### application-dev.yml additions needed:
```yaml
minio:
  enabled: true
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: images
  base-url: http://localhost:9000
  auto-create-bucket: true

media:
  upload:
    max-file-size: 52428800 # 50MB
    max-batch-size: 20
    allowed-image-types: jpg,jpeg,png,gif,webp,svg
    allowed-video-types: mp4,avi,mov,wmv,webm
    allowed-document-types: pdf,doc,docx,xls,xlsx,ppt,pptx
    enable-virus-scan: false
    async-processing: true
```

---

## Next Steps

1. Create DTOs for request/response
2. Implement FileValidationService
3. Implement FileStorageService (MinIO integration)
4. Implement MediaFileService (business logic)
5. Complete MediaFileController
6. Add async processing with @Async
7. Add unit tests
8. Add integration tests
9. Document API with Swagger/OpenAPI

---

## Example Usage

### cURL Examples

**Single Upload**:
```bash
curl -X POST http://localhost:8099/api/media/upload/single \
  -H "Authorization: Bearer <token>" \
  -F "file=@avatar.jpg" \
  -F "module=USER" \
  -F "description=User avatar"
```

**Batch Upload**:
```bash
curl -X POST http://localhost:8099/api/media/upload/batch \
  -H "Authorization: Bearer <token>" \
  -F "files=@image1.jpg" \
  -F "files=@image2.jpg" \
  -F "files=@image3.jpg" \
  -F "module=PRODUCT"
```

**Video Upload**:
```bash
curl -X POST http://localhost:8099/api/media/upload/video \
  -H "Authorization: Bearer <token>" \
  -F "file=@demo.mp4" \
  -F "thumbnail=@thumb.jpg" \
  -F "module=MARKETING"
```
