# Chunked Upload - Code Examples

## 1. Backend Service (Java)

### ChunkedUploadService.java

```java
package com.theblood.springfood.media.service;

import com.theblood.springfood.media.domain.MediaFile;
import com.theblood.springfood.media.domain.enums.UploadStatus;
import com.theblood.springfood.media.repository.MediaFileRepository;
import com.theblood.springfood.media.service.dto.*;
import io.minio.*;
import io.minio.messages.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChunkedUploadService {

    private final MediaFileRepository mediaFileRepository;
    private final ConcurrentHashMap<String, MediaFile> uploadSessions = new ConcurrentHashMap<>();
    
    @Value("${minio.bucket:springfood-media}")
    private String bucket;
    
    @Value("${minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;
    
    private static final long CHUNK_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * BƯỚC 1: Khởi tạo chunked upload
     */
    @Transactional
    public ChunkUploadInitResponse initiateChunkedUpload(ChunkUploadInitRequest request) {
        // Implementation in next section
    }
}
```

Xem file đầy đủ: `media/src/main/java/com/theblood/springfood/media/service/ChunkedUploadService.java`

## 2. Backend Controller (Java)

