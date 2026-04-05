package com.theblood.springfood.media.service;

import com.theblood.springfood.common.enums.FileType;
import com.theblood.springfood.media.domain.enums.UploadModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * File Validation Service
 * Validate file dựa trên UploadModule (service nào upload) và FileType (loại file)
 */
@Slf4j
@Service
public class FileValidationService {

    // Allowed FileTypes cho từng UploadModule
    private static final Map<UploadModule, Set<FileType>> ALLOWED_FILE_TYPES = new HashMap<>();

    static {
        // PRODUCT service: chỉ cho phép IMAGE
        ALLOWED_FILE_TYPES.put(UploadModule.PRODUCT, Set.of(FileType.IMAGE));

        // USER service: chỉ cho phép IMAGE (avatar)
        ALLOWED_FILE_TYPES.put(UploadModule.USER, Set.of(FileType.IMAGE));

        // SHOP service: chỉ cho phép IMAGE (logo, banner)
        ALLOWED_FILE_TYPES.put(UploadModule.SHOP, Set.of(FileType.IMAGE));

        // CHAT service: cho phép IMAGE, VIDEO, DOCUMENT
        ALLOWED_FILE_TYPES.put(UploadModule.CHAT, Set.of(
            FileType.IMAGE,
            FileType.VIDEO,
            FileType.DOCUMENT
        ));

        // ORDER service: cho phép DOCUMENT, IMAGE
        ALLOWED_FILE_TYPES.put(UploadModule.ORDER, Set.of(
            FileType.DOCUMENT,
            FileType.IMAGE
        ));

        // REPORT service: cho phép DOCUMENT, IMAGE
        ALLOWED_FILE_TYPES.put(UploadModule.REPORT, Set.of(
            FileType.DOCUMENT,
            FileType.IMAGE
        ));

        // NOTIFICATION service: cho phép IMAGE, DOCUMENT
        ALLOWED_FILE_TYPES.put(UploadModule.NOTIFICATION, Set.of(
            FileType.IMAGE,
            FileType.DOCUMENT
        ));

        // MARKETING service: cho phép IMAGE, VIDEO
        ALLOWED_FILE_TYPES.put(UploadModule.MARKETING, Set.of(
            FileType.IMAGE,
            FileType.VIDEO
        ));

        // SYSTEM: cho phép tất cả
        ALLOWED_FILE_TYPES.put(UploadModule.SYSTEM, Set.of(
            FileType.IMAGE,
            FileType.VIDEO,
            FileType.AUDIO,
            FileType.DOCUMENT,
            FileType.OTHER
        ));

        // OTHER: cho phép tất cả
        ALLOWED_FILE_TYPES.put(UploadModule.OTHER, Set.of(
            FileType.IMAGE,
            FileType.VIDEO,
            FileType.AUDIO,
            FileType.DOCUMENT,
            FileType.OTHER
        ));
    }

    @Value("${media.upload.max-file-size:104857600}") // 100MB default
    private long maxFileSize;

    /**
     * Validate file trước khi upload
     */
    public void validateFile(MultipartFile file, UploadModule module) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }

        // 1. Detect FileType từ filename và MIME type
        FileType detectedType = detectFileType(file);

        // 2. Validate file size
        validateFileSize(file.getSize(), module, detectedType);

        // 3. Validate FileType có được phép cho UploadModule này không
        validateFileType(detectedType, module);

        // 4. Validate extension có match với FileType không
        validateExtension(originalFilename, detectedType);

        log.info("File validation passed: {} (module: {}, type: {}, size: {} bytes)",
            originalFilename, module, detectedType, file.getSize());
    }

    /**
     * Detect FileType từ file
     * CHỈ chấp nhận các file type được hệ thống hỗ trợ
     */
    private FileType detectFileType(MultipartFile file) {
        // Ưu tiên detect từ MIME type
        String mimeType = file.getContentType();
        if (mimeType != null && !mimeType.isEmpty()) {
            FileType typeFromMime = FileType.fromMimeType(mimeType);
            if (typeFromMime != FileType.OTHER) {
                return typeFromMime;
            }
        }

        // Fallback: detect từ filename
        String filename = file.getOriginalFilename();
        if (filename != null) {
            FileType typeFromFilename = FileType.fromFilename(filename);
            if (typeFromFilename != FileType.OTHER) {
                return typeFromFilename;
            }
        }

        // Nếu không detect được -> reject
        throw new IllegalArgumentException(
            "File type không được hỗ trợ. "
        );
    }

    /**
     * Validate file size
     */
    private void validateFileSize(long fileSize, UploadModule module, FileType fileType) {
        if (fileSize <= 0) {
            throw new IllegalArgumentException("File size phải lớn hơn 0");
        }

        long moduleMaxSize = getMaxFileSizeForModule(module, fileType);

        if (fileSize > moduleMaxSize) {
            throw new IllegalArgumentException(
                String.format("File quá lớn. Max size cho %s (%s): %d MB (file của bạn: %d MB)",
                    module.getServiceName(), fileType.getDisplayName(),
                    moduleMaxSize / (1024 * 1024), fileSize / (1024 * 1024))
            );
        }
    }

    /**
     * Validate FileType có được phép cho UploadModule không
     */
    private void validateFileType(FileType fileType, UploadModule module) {
        // Không cho phép OTHER type
        if (fileType == FileType.OTHER) {
            throw new IllegalArgumentException(
                "File type không được hỗ trợ. "
            );
        }

        Set<FileType> allowedTypes = ALLOWED_FILE_TYPES.get(module);

        if (allowedTypes == null || !allowedTypes.contains(fileType)) {
            throw new IllegalArgumentException(
                String.format("File type '%s' không được phép cho %s. Allowed types: %s",
                    fileType.getDisplayName(), module.getServiceName(), allowedTypes)
            );
        }
    }

    /**
     * Validate extension có match với FileType không
     */
    private void validateExtension(String filename, FileType fileType) {
        String extension = getFileExtension(filename);

        if (!fileType.supportsExtension(extension)) {
            throw new IllegalArgumentException(
                String.format("Extension '%s' không hợp lệ cho file type '%s'. Allowed: %s",
                    extension, fileType.getDisplayName(), Arrays.toString(fileType.getExtensions()))
            );
        }
    }

    /**
     * Get max file size cho từng module và file type
     */
    private long getMaxFileSizeForModule(UploadModule module, FileType fileType) {
        // Size limits dựa trên module và file type
        return switch (module) {
            case USER -> 5 * 1024 * 1024L;        // 5MB (avatar)
            case PRODUCT -> 10 * 1024 * 1024L;    // 10MB (product images)
            case SHOP -> 10 * 1024 * 1024L;       // 10MB (shop logo/banner)
            case CHAT -> switch (fileType) {
                case IMAGE -> 20 * 1024 * 1024L;   // 20MB
                case VIDEO -> 100 * 1024 * 1024L;  // 100MB
                case DOCUMENT -> 50 * 1024 * 1024L; // 50MB
                default -> 20 * 1024 * 1024L;
            };
            case ORDER -> 20 * 1024 * 1024L;      // 20MB
            case REPORT -> 50 * 1024 * 1024L;     // 50MB
            case NOTIFICATION -> 10 * 1024 * 1024L; // 10MB
            case MARKETING -> switch (fileType) {
                case VIDEO -> 200 * 1024 * 1024L;  // 200MB
                default -> 20 * 1024 * 1024L;      // 20MB
            };
            default -> maxFileSize;                // Default from config
        };
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Get allowed FileTypes for module
     */
    public Set<FileType> getAllowedFileTypes(UploadModule module) {
        return ALLOWED_FILE_TYPES.getOrDefault(module, Collections.emptySet());
    }

    /**
     * Check if FileType is allowed for module
     */
    public boolean isFileTypeAllowed(FileType fileType, UploadModule module) {
        Set<FileType> allowedTypes = ALLOWED_FILE_TYPES.get(module);
        return allowedTypes != null && allowedTypes.contains(fileType);
    }
}
