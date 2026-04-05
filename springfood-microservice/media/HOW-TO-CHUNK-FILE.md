# Cách Chia File Thành Chunks - Chi Tiết

## 1. Khái Niệm Cơ Bản

### File là gì?
```
File 50MB = 52,428,800 bytes
[byte 0][byte 1][byte 2]...[byte 52,428,799]
```

### Chunk là gì?
```
Chunk = 1 phần nhỏ của file
Ví dụ: Chunk size = 5MB = 5,242,880 bytes

File 50MB chia thành 10 chunks:
Chunk 1: byte 0 → 5,242,879 (5MB)
Chunk 2: byte 5,242,880 → 10,485,759 (5MB)
Chunk 3: byte 10,485,760 → 15,728,639 (5MB)
...
Chunk 10: byte 47,185,920 → 52,428,799 (5MB còn lại)
```

## 2. Backend: Nhận File Từ Client

### Cách 1: Client Tự Chia Chunks (RECOMMENDED)

**Client chia file thành chunks trước khi gửi**

```java
// Backend chỉ cần nhận từng chunk
@PostMapping("/upload/chunk")
public ResponseEntity<?> uploadChunk(
    @RequestParam("chunkNumber") int chunkNumber,
    @RequestParam("chunkData") MultipartFile chunkData) {
    
    // chunkData đã là 1 chunk sẵn (5MB)
    // Không cần chia nữa, chỉ cần upload lên MinIO
    
    byte[] bytes = chunkData.getBytes();
    // bytes.length = 5MB (hoặc nhỏ hơn nếu là chunk cuối)
    
    return ResponseEntity.ok("Chunk uploaded");
}
```

**Ưu điểm:**
- Backend đơn giản, chỉ nhận và forward lên MinIO
- Client control được chunk size
- Giảm tải cho server

### Cách 2: Backend Nhận File Nguyên, Tự Chia Chunks

**Client gửi file nguyên, backend chia chunks**

```java
@PostMapping("/upload/file")
public ResponseEntity<?> uploadFile(
    @RequestParam("file") MultipartFile file) {
    
    // File 50MB
    long fileSize = file.getSize(); // 52,428,800 bytes
    long chunkSize = 5 * 1024 * 1024; // 5MB
    
    // Tính số chunks
    int totalChunks = (int) Math.ceil((double) fileSize / chunkSize);
    // totalChunks = 10
    
    // Đọc file thành byte array
    byte[] fileBytes = file.getBytes();
    
    // Chia thành chunks
    for (int i = 0; i < totalChunks; i++) {
        int start = i * (int) chunkSize;
        int end = Math.min(start + (int) chunkSize, fileBytes.length);
        
        // Tạo chunk
        byte[] chunkBytes = Arrays.copyOfRange(fileBytes, start, end);
        
        // Upload chunk lên MinIO
        uploadChunkToMinIO(chunkBytes, i + 1);
    }
    
    return ResponseEntity.ok("File uploaded");
}
```

**Nhược điểm:**
- File 50MB phải load hết vào memory → Tốn RAM
- Không tận dụng được parallel upload từ client
- Server phải xử lý nhiều hơn

## 3. Chi Tiết: Chia File Thành Chunks

### 3.1. Công Thức Tính

```java
long fileSize = 52428800; // 50MB
long chunkSize = 5242880;  // 5MB

// Số chunks
int totalChunks = (int) Math.ceil((double) fileSize / chunkSize);
// = Math.ceil(52428800 / 5242880)
// = Math.ceil(10.0)
// = 10 chunks

// Vị trí từng chunk
for (int i = 0; i < totalChunks; i++) {
    int chunkNumber = i + 1; // 1-based
    
    long startByte = i * chunkSize;
    long endByte = Math.min(startByte + chunkSize, fileSize);
    long currentChunkSize = endByte - startByte;
    
    System.out.println(String.format(
        "Chunk %d: byte %d → %d (%d bytes)",
        chunkNumber, startByte, endByte - 1, currentChunkSize
    ));
}
```

**Output:**
```
Chunk 1: byte 0 → 5242879 (5242880 bytes = 5MB)
Chunk 2: byte 5242880 → 10485759 (5242880 bytes = 5MB)
Chunk 3: byte 10485760 → 15728639 (5242880 bytes = 5MB)
Chunk 4: byte 15728640 → 20971519 (5242880 bytes = 5MB)
Chunk 5: byte 20971520 → 26214399 (5242880 bytes = 5MB)
Chunk 6: byte 26214400 → 31457279 (5242880 bytes = 5MB)
Chunk 7: byte 31457280 → 36700159 (5242880 bytes = 5MB)
Chunk 8: byte 36700160 → 41943039 (5242880 bytes = 5MB)
Chunk 9: byte 41943040 → 47185919 (5242880 bytes = 5MB)
Chunk 10: byte 47185920 → 52428799 (5242880 bytes = 5MB)
```

### 3.2. Code Chia Chunks Từ MultipartFile

```java
public List<byte[]> splitFileIntoChunks(MultipartFile file, long chunkSize) 
        throws IOException {
    
    List<byte[]> chunks = new ArrayList<>();
    
    // Đọc toàn bộ file
    byte[] fileBytes = file.getBytes();
    long fileSize = fileBytes.length;
    
    // Chia thành chunks
    int offset = 0;
    while (offset < fileSize) {
        // Tính size của chunk hiện tại
        int currentChunkSize = (int) Math.min(chunkSize, fileSize - offset);
        
        // Copy bytes từ offset → offset + currentChunkSize
        byte[] chunk = Arrays.copyOfRange(fileBytes, offset, offset + currentChunkSize);
        
        chunks.add(chunk);
        offset += currentChunkSize;
    }
    
    return chunks;
}

// Sử dụng
MultipartFile file = ...; // 50MB
long chunkSize = 5 * 1024 * 1024; // 5MB

List<byte[]> chunks = splitFileIntoChunks(file, chunkSize);
// chunks.size() = 10
// chunks.get(0).length = 5242880 (5MB)
// chunks.get(9).length = 5242880 (5MB)
```

### 3.3. Code Chia Chunks Từ InputStream (Tiết kiệm RAM)

```java
public void processFileInChunks(MultipartFile file, long chunkSize) 
        throws IOException {
    
    try (InputStream inputStream = file.getInputStream()) {
        byte[] buffer = new byte[(int) chunkSize];
        int chunkNumber = 1;
        int bytesRead;
        
        // Đọc từng chunk
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            // buffer chứa chunk data
            // bytesRead = số bytes thực tế đọc được
            
            byte[] chunk;
            if (bytesRead < buffer.length) {
                // Chunk cuối nhỏ hơn chunkSize
                chunk = Arrays.copyOf(buffer, bytesRead);
            } else {
                chunk = buffer;
            }
            
            // Xử lý chunk
            uploadChunkToMinIO(chunk, chunkNumber);
            
            chunkNumber++;
        }
    }
}
```

**Ưu điểm:**
- Không load toàn bộ file vào RAM
- Xử lý từng chunk một
- Phù hợp với file rất lớn (GB)

## 4. Upload Chunk Lên MinIO

### 4.1. MinIO Multipart Upload API

```java
public String uploadChunkToMinIO(
        byte[] chunkData, 
        int chunkNumber,
        String uploadId,
        String objectKey) throws Exception {
    
    io.minio.MinioClient minioClient = getMinioClient();
    
    // Convert byte[] thành InputStream
    ByteArrayInputStream inputStream = new ByteArrayInputStream(chunkData);
    
    // Upload chunk
    UploadPartResponse response = minioClient.uploadPart(
        UploadPartArgs.builder()
            .bucket("springfood-media")
            .object(objectKey)
            .uploadId(uploadId)  // Từ bước init
            .partNumber(chunkNumber)  // 1, 2, 3, ...
            .stream(inputStream, chunkData.length, -1)
            .build()
    );
    
    // Lấy ETag (checksum)
    String etag = response.etag();
    
    return etag;
}
```

### 4.2. Flow Hoàn Chỉnh

```java
@Service
public class ChunkUploadService {
    
    public void uploadFileWithChunks(MultipartFile file) throws Exception {
        long chunkSize = 5 * 1024 * 1024; // 5MB
        String objectKey = "videos/" + UUID.randomUUID() + ".mp4";
        
        // BƯỚC 1: Init multipart upload
        String uploadId = initMultipartUpload(objectKey);
        
        // BƯỚC 2: Chia file thành chunks và upload
        List<Part> parts = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream()) {
            byte[] buffer = new byte[(int) chunkSize];
            int chunkNumber = 1;
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] chunk = (bytesRead < buffer.length) 
                    ? Arrays.copyOf(buffer, bytesRead) 
                    : buffer;
                
                // Upload chunk
                String etag = uploadChunkToMinIO(chunk, chunkNumber, uploadId, objectKey);
                
                // Lưu part info
                parts.add(new Part(chunkNumber, etag));
                
                chunkNumber++;
            }
        }
        
        // BƯỚC 3: Complete multipart upload
        completeMultipartUpload(uploadId, objectKey, parts);
    }
    
    private String initMultipartUpload(String objectKey) throws Exception {
        return minioClient.initiateMultipartUpload(
            InitiateMultipartUploadArgs.builder()
                .bucket("springfood-media")
                .object(objectKey)
                .build()
        );
    }
    
    private void completeMultipartUpload(
            String uploadId, 
            String objectKey, 
            List<Part> parts) throws Exception {
        
        minioClient.completeMultipartUpload(
            CompleteMultipartUploadArgs.builder()
                .bucket("springfood-media")
                .object(objectKey)
                .uploadId(uploadId)
                .parts(parts.toArray(new Part[0]))
                .build()
        );
    }
}
```

## 5. Tóm Tắt

### Client Chia Chunks (Recommended)

```
Client:
1. Đọc file 50MB
2. Chia thành 10 chunks × 5MB
3. Gửi từng chunk lên server

Server:
1. Nhận chunk
2. Forward lên MinIO
3. Trả về ETag
```

### Server Chia Chunks

```
Client:
1. Gửi file 50MB nguyên

Server:
1. Nhận file 50MB
2. Chia thành 10 chunks × 5MB
3. Upload từng chunk lên MinIO
4. Trả về kết quả
```

### Công Thức Quan Trọng

```java
// Số chunks
totalChunks = Math.ceil(fileSize / chunkSize)

// Vị trí chunk thứ i (0-based)
startByte = i * chunkSize
endByte = Math.min(startByte + chunkSize, fileSize)
currentChunkSize = endByte - startByte

// Chia byte array
chunk = Arrays.copyOfRange(fileBytes, startByte, endByte)
```

Đây là phần XỬ LÝ CHIA CHUNK chi tiết nhất rồi đó!
