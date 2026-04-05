package com.theblood.springfood.media.service.impl;

import com.theblood.springfood.client.service.LoggingService;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import com.theblood.springfood.common.dto.request.UserContextHolder;
import com.theblood.springfood.common.enums.ActionType;
import com.theblood.springfood.common.enums.FileType;
import com.theblood.springfood.media.domain.MediaFile;
import com.theblood.springfood.media.domain.enums.UploadModule;
import com.theblood.springfood.media.domain.enums.UploadStatus;
import com.theblood.springfood.media.repository.MediaRepository;
import com.theblood.springfood.media.service.MediaUploadService;
import com.theblood.springfood.media.service.MinioUploadService;
import com.theblood.springfood.media.service.dto.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MediaUploadServiceImpl implements MediaUploadService {

    private final MediaRepository mediaRepository;
    private final MinioUploadService minioUploadService;
    private final LoggingService loggingService;
    private final KafkaUploadService kafkaUploadService;

    @Value("${minio.bucket:springfood-media}")
    String bucketName;

    @Value("${minio.endpoint:http://localhost:9000}")
    String minioEndpoint;

    @Override
    @Transactional
    public FileResponse uploadSingleFile(FileRequest fileRequest) {

        CustomUserPrincipal userContext = UserContextHolder.getContext();
        MediaFile mediaFile = new MediaFile();
        mediaFile.setUploadStatus(UploadStatus.UPLOADING);
        try {
            // Detect FileType từ content type
            FileType detectedFileType = detectFileType(fileRequest.getMultipartFile().getContentType());

            // Calculate file hash (SHA-256)
            byte[] fileBytes = fileRequest.getMultipartFile().getBytes();
            String fileHash = calculateSHA256(fileBytes);

            // Set basic info
            mediaFile.setFileOriginalName(fileRequest.getMultipartFile().getOriginalFilename());
            mediaFile.setFileSize(fileRequest.getMultipartFile().getSize());
            mediaFile.setFileType(detectedFileType);
            mediaFile.setFileHash(fileHash);
            mediaFile.setDescription(fileRequest.getDescription());
            mediaFile.setBucketName(bucketName);
            mediaFile.setUploadModule(fileRequest.getUploadModule());

            // Upload to MinIO
            String storedFileName = minioUploadService.uploadFile(
                fileRequest.getMultipartFile().getInputStream(),
                bucketName,
                fileRequest.getMultipartFile().getOriginalFilename(),
                fileRequest.getMultipartFile().getSize(),
                fileRequest.getMultipartFile().getContentType()
            );

            // Set upload result
            mediaFile.setFileStoredName(storedFileName);
            mediaFile.setFilePath("/" + bucketName + "/" + storedFileName);
            mediaFile.setFileUrl(minioEndpoint + "/" + bucketName + "/" + storedFileName);
            mediaFile.setUploadStatus(UploadStatus.COMPLETED);

            // Save to DB (chỉ save 1 lần)
            mediaFile = mediaRepository.save(mediaFile);

            log.info("File uploaded successfully: {}", storedFileName);

            loggingService.createLogAction(
                ActionType.UPLOAD.name(),
                null,
                "Uploaded file: " + mediaFile.getFileOriginalName() + " (size: " + mediaFile.getFileSize() + " bytes, type: " + mediaFile.getFileType() + ") to module: " + mediaFile.getUploadModule(),
                "upload avatar",
                "MEDIA_FILE",
                mediaFile.getId(),
                userContext.getUserIdString(),
                userContext.getUsername(),
                userContext.getShopId(),
                null,
                null
            );

            return FileResponse.builder()
                .fileId(mediaFile.getId())
                .fileName(mediaFile.getFileOriginalName())
                .fileUrl(mediaFile.getFileUrl())
                .fileSize(mediaFile.getFileSize())
                .fileType(mediaFile.getFileType())
                .uploadStatus(mediaFile.getUploadStatus())
                .build();

        } catch (Exception e) {
            log.error("Exception in uploadSingleFile() with cause = '{}' and exception = '{}'",
                e.getCause() != null ? e.getCause() : "NULL", e.getMessage(), e);

            mediaFile.setUploadStatus(UploadStatus.FAILED);
            mediaRepository.save(mediaFile);

            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Detect FileType từ MIME type
     */
    private FileType detectFileType(String contentType) {
        if (contentType == null) {
            return FileType.OTHER;
        }

        String lowerContentType = contentType.toLowerCase();

        if (lowerContentType.startsWith("image/")) {
            return FileType.IMAGE;
        } else if (lowerContentType.startsWith("video/")) {
            return FileType.VIDEO;
        } else if (lowerContentType.startsWith("audio/")) {
            return FileType.AUDIO;
        } else if (lowerContentType.contains("pdf") ||
            lowerContentType.contains("document") ||
            lowerContentType.contains("msword") ||
            lowerContentType.contains("spreadsheet") ||
            lowerContentType.contains("presentation") ||
            lowerContentType.contains("text/")) {
            return FileType.DOCUMENT;
        }

        return FileType.OTHER;
    }

    /**
     * Calculate SHA-256 hash
     */
    private String calculateSHA256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    @Override
    public MultiFileResponse uploadMultipleFiles(MultiFileRequest fileRequests) {

        CustomUserPrincipal userContext = UserContextHolder.getContext();
        List<FileResponse> uploadedFiles = new ArrayList<>();
        List<UploadStatusResponse> failedFiles = new ArrayList<>();

        List<MediaFile> mediaFiles = new ArrayList<>();
        UploadModule uploadModule = fileRequests.getUploadModule();
        int successCount = 0;
        long totalSizeCount = 0;
        String description = fileRequests.getDescription();
        String uploadDate = String.format(LocalDateTime.now().toString(), "yyyyMMddHHmmss");

        for (int i = 0; i < fileRequests.getMultipartFile().length; i++) {
            MediaFile mediaFile = new MediaFile();
            try {
                MultipartFile fileRequest = fileRequests.getMultipartFile()[i];
                // Detect FileType từ content type
                FileType detectedFileType = detectFileType(fileRequest.getContentType());

                // Calculate file hash (SHA-256)
                byte[] fileBytes = fileRequest.getBytes();
                String fileHash = calculateSHA256(fileBytes);

                // Set basic info
                mediaFile.setFileOriginalName(fileRequest.getOriginalFilename());
                mediaFile.setFileSize(fileRequest.getSize());
                mediaFile.setFileType(detectedFileType);
                mediaFile.setFileHash(fileHash);
                mediaFile.setDescription(description);
                mediaFile.setBucketName(bucketName);
                mediaFile.setUploadModule(uploadModule);

                // Upload to MinIO
                String storedFileName = minioUploadService.uploadFile(
                    fileRequest.getInputStream(),
                    bucketName,
                    fileRequest.getOriginalFilename(),
                    fileRequest.getSize(),
                    fileRequest.getContentType()
                );
                // Set upload result
                mediaFile.setFileStoredName(storedFileName + uploadDate);
                mediaFile.setFilePath("/" + bucketName + "/" + storedFileName);
                mediaFile.setFileUrl(minioEndpoint + "/" + bucketName + "/" + storedFileName);
                mediaFile.setUploadStatus(UploadStatus.COMPLETED);
                successCount++;
                totalSizeCount += mediaFile.getFileSize();
                mediaFiles.add(mediaFile);
                log.info("File uploaded successfully: {}", storedFileName);


            } catch (Exception e) {
                log.error("Exception in uploadSingleFile() with cause = '{}' and exception = '{}'",
                    e.getCause() != null ? e.getCause() : "NULL", e.getMessage(), e);
                mediaFile.setUploadStatus(UploadStatus.FAILED);
                mediaRepository.save(mediaFile);
                failedFiles.add(UploadStatusResponse.builder()
                    .uploadStatus(UploadStatus.FAILED)
                    .fileName(mediaFile.getFileOriginalName())
                    .reason(e.getMessage())
                    .build());

                throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
            }
            FileResponse fileResponse = FileResponse.builder()
                .uploadDate(uploadDate)
                .fileUrl(mediaFile.getFileUrl())
                .uploadStatus(UploadStatus.COMPLETED)
                .fileSize(mediaFile.getFileSize())
                .fileName(mediaFile.getFileOriginalName())
                .build();
            uploadedFiles.add(fileResponse);
        }
        mediaRepository.saveAll(mediaFiles);

        loggingService.createLogAction(
            ActionType.UPLOAD.name(),
            null,
            "Uploaded " + successCount + "  file " + " (total size: " + totalSizeCount + " bytes, different type: " + fileRequests.isDifferentType() + ") to module: " + uploadModule,
            "upload avatar",
            "MEDIA_FILE",
            mediaFiles.toString(),
            userContext.getUserIdString(),
            userContext.getUsername(),
            userContext.getShopId(),
            null,
            null
        );
        //send message to kafka topic

        MultiFileResponse res = MultiFileResponse.builder()
            .total(mediaFiles.size())
            .success(successCount)
            .fail(mediaFiles.size() - successCount)
            .uploadedFile(uploadedFiles)
            .failedFile(failedFiles)
            .build();
        kafkaUploadService.mergeProductImage(res);
        return res;
    }
}
