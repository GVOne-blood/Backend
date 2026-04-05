package com.theblood.minio.example;

import com.theblood.minio.core.impl.MinIOClientCustomImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Example Controller demonstrating MinIO library usage
 * This is just an example - copy to your project and modify as needed
 * <p>
 * NOTE: This resources is in main/java for reference only.
 * It will not be auto-loaded unless you scan this package.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class MinioExampleController {

    private final MinIOClientCustomImpl minioClient;

    /**
     * Upload file
     * POST /api/files/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "") String folder) {

        try {
            String fileName = file.getOriginalFilename();
            String objectKey = folder.isEmpty() ? fileName : folder + "/" + fileName;

            String url = minioClient.upload(file, objectKey);

            return ResponseEntity.ok("File uploaded successfully: " + url);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    /**
     * Download file
     * GET /api/files/download/{objectKey}
     */
    @GetMapping("/download/{objectKey}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String objectKey) {
        try {
            InputStream inputStream = minioClient.getObject(objectKey);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectKey + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get presigned URL for temporary access
     * GET /api/files/presigned-url/{objectKey}
     */
    @GetMapping("/presigned-url/{objectKey}")
    public ResponseEntity<String> getPresignedUrl(
            @PathVariable String objectKey,
            @RequestParam(value = "expiry", defaultValue = "3600") int expirySeconds) {

        try {
            String url = minioClient.getPresignedUrl(objectKey, expirySeconds);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to generate URL: " + e.getMessage());
        }
    }

    /**
     * Delete file
     * DELETE /api/files/{objectKey}
     */
    @DeleteMapping("/{objectKey}")
    public ResponseEntity<String> deleteFile(@PathVariable String objectKey) {
        boolean deleted = minioClient.deleteObject(objectKey);

        if (deleted) {
            return ResponseEntity.ok("File deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to delete file");
        }
    }

    /**
     * Check if file exists
     * GET /api/files/exists/{objectKey}
     */
    @GetMapping("/exists/{objectKey}")
    public ResponseEntity<Boolean> fileExists(@PathVariable String objectKey) {
        boolean exists = minioClient.objectExists(objectKey);
        return ResponseEntity.ok(exists);
    }

    /**
     * Move/Rename file
     * POST /api/files/move
     */
    @PostMapping("/move")
    public ResponseEntity<String> moveFile(
            @RequestParam("source") String sourceKey,
            @RequestParam("destination") String destKey) {

        boolean moved = minioClient.moveObject(sourceKey, destKey);

        if (moved) {
            return ResponseEntity.ok("File moved successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to move file");
        }
    }
}
