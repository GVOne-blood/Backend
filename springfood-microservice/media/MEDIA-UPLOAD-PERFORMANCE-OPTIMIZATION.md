# Media Upload - Performance Optimization Strategy

## Tổng quan

Khi upload 10-20 files cùng lúc, cần tối ưu để:
- Tận dụng tối đa băng thông
- Giảm thời gian chờ của user
- Tránh timeout
- Xử lý lỗi từng file riêng biệt

## Chiến lược Multi-threading

### 1. CompletableFuture với Custom ThreadPool

**Ưu điểm**:
- Non-blocking, reactive
- Dễ compose và chain operations
- Exception handling tốt
- Có thể cancel individual tasks

**Implementation**:

```java
@Service
@Slf4j
public class ParallelFileUploadService {
    
    private final ExecutorService uploadExecutor;
    private final FileStorageService storageService;
    private final MediaFileRepository repository;
    
    public ParallelFileUploadService(
            FileStorageService storageService,
            MediaFileRepository repository) {
        this.storageService = storageService;
        this.repository = repository;
        
        // Custom thread pool cho upload
        this.uploadExecutor = new ThreadPoolExecutor(
            5,  // corePoolSize - tối thiểu 5 threads
            20, // maximumPoolSize - tối đa 20 threads (1 thread/file)
            60L, TimeUnit.SECONDS, // keepAliveTime
            new LinkedBlockingQueue<>(100), // queue capacity
            new ThreadFactoryBuilder()
                .setNameFormat("file-upload-%d")
                .setDaemon(false)
                .build(),
            new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
        );
    }
    
    /**
     * Upload multiple files in parallel
     * Chia nhỏ: mỗi file = 1 CompletableFuture
     */
    public CompletableFuture<BatchUploadResponse> uploadBatch(
            List<MultipartFile> files, 
            UploadModule module) {
        
        log.info("Starting batch upload of {} files", files.size());
        
        // Tạo CompletableFuture cho mỗi file
        List<CompletableFuture<FileUploadResult>> futures = files.stream()
            .map(file -> uploadSingleFileAsync(file, module))
            .collect(Collectors.toList());
        
        // Chờ tất cả complete (allOf không block main thread)
        CompletableFuture<Void> allUploads = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        
        // Khi tất cả xong, aggregate results
        return allUploads.thenApply(v -> {
            List<FileUploadResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
            
            return buildBatchResponse(results);
        });
    }
    
    /**
     * Upload 1 file async
     */
    private CompletableFuture<FileUploadResult> uploadSingleFileAsync(
            MultipartFile file, 
            UploadModule module) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Uploading file: {} on thread: {}", 
                    file.getOriginalFilename(), 
                    Thread.currentThread().getName());
                
                // 1. Validate file
                validateFile(file);
                
                // 2. Generate metadata
                String fileHash = generateHash(file);
                String storedName = generateStoredName(file);
                
                // 3. Upload to MinIO (blocking I/O)
                String fileUrl = storageService.uploadFile(file, storedName);
                
                // 4. Save to database
                MediaFile mediaFile = createMediaFile(file, fileUrl, fileHash, module);
                repository.save(mediaFile);
                
                return FileUploadResult.success(mediaFile);
                
            } catch (Exception e) {
                log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                return FileUploadResult.failure(file.getOriginalFilename(), e.getMessage());
            }
        }, uploadExecutor); // Sử dụng custom executor
    }
    
    @PreDestroy
    public void shutdown() {
        uploadExecutor.shutdown();
        try {
            if (!uploadExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                uploadExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            uploadExecutor.shutdownNow();
        }
    }
}
```

---

### 2. Parallel Streams (Alternative - Simpler)

**Ưu điểm**:
- Code ngắn gọn hơn
- Tự động quản lý thread pool (ForkJoinPool)
- Phù hợp cho CPU-bound tasks

**Nhược điểm**:
- Khó control thread pool size
- Không linh hoạt bằng CompletableFuture

```java
public BatchUploadResponse uploadBatchParallel(
        List<MultipartFile> files, 
        UploadModule module) {
    
    // Parallel stream tự động chia task
    List<FileUploadResult> results = files.parallelStream()
        .map(file -> uploadSingleFile(file, module))
        .collect(Collectors.toList());
    
    return buildBatchResponse(results);
}

// Config custom ForkJoinPool nếu cần
ForkJoinPool customPool = new ForkJoinPool(20);
customPool.submit(() -> 
    files.parallelStream()
        .map(file -> uploadSingleFile(file, module))
        .collect(Collectors.toList())
).get();
```

---

### 3. Reactive Approach với Project Reactor (Advanced)

**Ưu điểm**:
- Non-blocking I/O
- Backpressure handling
- Tích hợp tốt với WebFlux

```java
@Service
public class ReactiveFileUploadService {
    
    public Flux<FileUploadResult> uploadBatchReactive(
            List<MultipartFile> files, 
            UploadModule module) {
        
        return Flux.fromIterable(files)
            .flatMap(file -> uploadFileReactive(file, module), 
                10) // Concurrency = 10 files cùng lúc
            .onErrorContinue((error, file) -> {
                log.error("Failed to upload file", error);
            });
    }
    
    private Mono<FileUploadResult> uploadFileReactive(
            MultipartFile file, 
            UploadModule module) {
        
        return Mono.fromCallable(() -> uploadSingleFile(file, module))
            .subscribeOn(Schedulers.boundedElastic());
    }
}
```

---

## Chunked Upload cho Large Files

### Multipart Upload Strategy

Chia file lớn thành nhiều chunks nhỏ, upload parallel:

```java
@Service
public class ChunkedUploadService {
    
    private static final int CHUNK_SIZE = 5 * 1024 * 1024; // 5MB per chunk
    
    /**
     * Upload large file in chunks
     */
    public CompletableFuture<String> uploadLargeFile(
            MultipartFile file, 
            String objectName) {
        
        try {
            byte[] fileBytes = file.getBytes();
            int totalChunks = (int) Math.ceil((double) fileBytes.length / CHUNK_SIZE);
            
            log.info("Uploading file {} in {} chunks", objectName, totalChunks);
            
            // Tạo multipart upload
            String uploadId = minioClient.initiateMultipartUpload(
                InitiateMultipartUploadArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            ).result().uploadId();
            
            // Upload từng chunk parallel
            List<CompletableFuture<PartETag>> chunkFutures = new ArrayList<>();
            
            for (int i = 0; i < totalChunks; i++) {
                final int partNumber = i + 1;
                final int start = i * CHUNK_SIZE;
                final int end = Math.min(start + CHUNK_SIZE, fileBytes.length);
                final byte[] chunkData = Arrays.copyOfRange(fileBytes, start, end);
                
                CompletableFuture<PartETag> chunkFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return uploadChunk(uploadId, objectName, partNumber, chunkData);
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                }, uploadExecutor);
                
                chunkFutures.add(chunkFuture);
            }
            
            // Chờ tất cả chunks upload xong
            return CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<PartETag> parts = chunkFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
                    
                    // Complete multipart upload
                    return completeMultipartUpload(uploadId, objectName, parts);
                });
                
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload large file", e);
        }
    }
    
    private PartETag uploadChunk(
            String uploadId, 
            String objectName, 
            int partNumber, 
            byte[] data) throws Exception {
        
        log.debug("Uploading chunk {} on thread {}", 
            partNumber, Thread.currentThread().getName());
        
        return minioClient.uploadPart(
            UploadPartArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .data(new ByteArrayInputStream(data), data.length, -1)
                .build()
        );
    }
}
```

---

## Database Batch Insert Optimization

### JPA Batch Insert

```java
@Service
public class MediaFileService {
    
    @Transactional
    public void saveBatch(List<MediaFile> mediaFiles) {
        
        // Batch insert với JPA
        int batchSize = 50;
        for (int i = 0; i < mediaFiles.size(); i++) {
            repository.save(mediaFiles.get(i));
            
            if (i % batchSize == 0 && i > 0) {
                // Flush và clear để tránh OutOfMemory
                repository.flush();
                entityManager.clear();
            }
        }
    }
}
```

**application.yml config**:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
        batch_versioned_data: true
```

---

## Progress Tracking với WebSocket

### Real-time Progress Updates

```java
@Service
public class UploadProgressService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public void uploadWithProgress(
            List<MultipartFile> files, 
            String userId,
            UploadModule module) {
        
        AtomicInteger completed = new AtomicInteger(0);
        int total = files.size();
        
        List<CompletableFuture<FileUploadResult>> futures = files.stream()
            .map(file -> uploadWithProgressTracking(file, module, () -> {
                int current = completed.incrementAndGet();
                double progress = (current * 100.0) / total;
                
                // Send progress via WebSocket
                messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/upload-progress",
                    UploadProgress.builder()
                        .current(current)
                        .total(total)
                        .percentage(progress)
                        .build()
                );
            }))
            .collect(Collectors.toList());
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/upload-complete",
                    "All files uploaded successfully"
                );
            });
    }
}
```

---

## Rate Limiting & Circuit Breaker

### Resilience4j Integration

```java
@Service
public class ResilientUploadService {
    
    private final RateLimiter rateLimiter;
    private final CircuitBreaker circuitBreaker;
    
    public ResilientUploadService() {
        // Rate limiter: max 100 uploads per minute per user
        this.rateLimiter = RateLimiter.of("upload-limiter", 
            RateLimiterConfig.custom()
                .limitForPeriod(100)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofSeconds(5))
                .build()
        );
        
        // Circuit breaker: open after 5 failures
        this.circuitBreaker = CircuitBreaker.of("minio-breaker",
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .build()
        );
    }
    
    public FileUploadResult uploadWithResilience(MultipartFile file) {
        
        Supplier<FileUploadResult> uploadSupplier = () -> uploadFile(file);
        
        // Apply rate limiter + circuit breaker
        Supplier<FileUploadResult> decoratedSupplier = Decorators
            .ofSupplier(uploadSupplier)
            .withRateLimiter(rateLimiter)
            .withCircuitBreaker(circuitBreaker)
            .withRetry(Retry.ofDefaults("upload-retry"))
            .decorate();
        
        return Try.ofSupplier(decoratedSupplier)
            .recover(throwable -> FileUploadResult.failure(
                file.getOriginalFilename(), 
                throwable.getMessage()
            ))
            .get();
    }
}
```

---

## Configuration Tuning

### Optimal Thread Pool Sizing

```yaml
media:
  upload:
    # Thread pool configuration
    thread-pool:
      core-size: 5          # Số thread tối thiểu
      max-size: 20          # Số thread tối đa (= max concurrent uploads)
      queue-capacity: 100   # Queue size cho pending tasks
      keep-alive: 60        # Seconds
      
    # Batch processing
    batch:
      max-files: 20         # Max files per batch
      chunk-size: 5242880   # 5MB chunks for large files
      parallel-chunks: 10   # Max parallel chunks
      
    # Performance
    performance:
      enable-parallel: true
      enable-chunked: true  # For files > 10MB
      enable-compression: false
      
    # Timeouts
    timeout:
      upload: 300           # 5 minutes per file
      batch: 900            # 15 minutes per batch
```

### MinIO Client Tuning

```java
@Configuration
public class MinioOptimizationConfig {
    
    @Bean
    public MinioClient optimizedMinioClient(MinioProperties props) {
        
        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(
                20,  // maxIdleConnections
                5,   // keepAliveDuration
                TimeUnit.MINUTES
            ))
            .build();
        
        return MinioClient.builder()
            .endpoint(props.getEndpoint())
            .credentials(props.getAccessKey(), props.getSecretKey())
            .httpClient(httpClient)
            .build();
    }
}
```

---

## Performance Benchmarks

### Expected Performance

**Single-threaded (Sequential)**:
- 20 files × 2MB each = 40MB
- Upload time: ~20 seconds (2MB/s)
- Throughput: 1 file/second

**Multi-threaded (10 threads)**:
- 20 files × 2MB each = 40MB
- Upload time: ~4 seconds (10MB/s)
- Throughput: 5 files/second
- **Improvement: 5x faster**

**Chunked Upload (Large file 100MB)**:
- Sequential: ~50 seconds
- Chunked (10 parallel): ~10 seconds
- **Improvement: 5x faster**

---

## Monitoring & Metrics

### Prometheus Metrics

```java
@Component
public class UploadMetrics {
    
    private final Counter uploadCounter;
    private final Timer uploadTimer;
    private final Gauge activeUploads;
    
    public UploadMetrics(MeterRegistry registry) {
        this.uploadCounter = Counter.builder("media.upload.total")
            .tag("status", "success")
            .register(registry);
            
        this.uploadTimer = Timer.builder("media.upload.duration")
            .register(registry);
            
        this.activeUploads = Gauge.builder("media.upload.active", 
            uploadExecutor, ThreadPoolExecutor::getActiveCount)
            .register(registry);
    }
    
    public void recordUpload(Runnable uploadTask) {
        uploadTimer.record(uploadTask);
        uploadCounter.increment();
    }
}
```

---

## Best Practices Summary

1. **Chia nhỏ tasks**: Mỗi file = 1 independent task
2. **Custom ThreadPool**: Control được số lượng concurrent uploads
3. **CompletableFuture**: Non-blocking, dễ compose
4. **Chunked Upload**: Cho files > 10MB
5. **Batch Database Insert**: Giảm số lượng queries
6. **Progress Tracking**: Real-time feedback cho user
7. **Circuit Breaker**: Tránh cascade failures
8. **Rate Limiting**: Protect hệ thống khỏi abuse
9. **Monitoring**: Track performance metrics
10. **Graceful Shutdown**: Đợi pending uploads complete

---

## Recommended Approach

**Cho 10-20 files upload**:
```
✅ CompletableFuture + Custom ThreadPool (20 threads)
✅ Parallel upload to MinIO
✅ Batch insert to database (50 records/batch)
✅ WebSocket progress tracking
✅ Circuit breaker cho MinIO
✅ Rate limiting per user
```

Approach này cân bằng giữa performance, reliability và maintainability.
