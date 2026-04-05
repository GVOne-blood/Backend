package com.theblood.springfood.media.domain;

import com.theblood.springfood.common.enums.FileType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * Entity for tracking uploaded media files in MinIO.
 * Stores metadata about files for reference and management.
 */
@Entity
@Table(name = "media_file", schema = "media")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFile extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mediaFileSeqGenerator")
    @SequenceGenerator(name = "mediaFileSeqGenerator", sequenceName = "media_file_seq", allocationSize = 1)
    private Long id;

    /**
     * Original filename from upload
     */
    @NotNull
    @Size(max = 500)
    @Column(name = "filename", length = 500, nullable = false)
    private String filename;

    /**
     * Path in MinIO bucket (e.g., "documents/2024/03/file.pdf")
     */
    @NotNull
    @Size(max = 1000)
    @Column(name = "file_path", length = 1000, nullable = false, unique = true)
    private String filePath;

    /**
     * MinIO bucket name
     */
    @NotNull
    @Size(max = 100)
    @Column(name = "bucket_name", length = 100, nullable = false)
    private String bucketName;

    /**
     * MIME type (application/pdf, image/jpeg, etc.)
     */
    @Size(max = 100)
    @Column(name = "content_type", length = 100)
    private String contentType;

    /**
     * File size in bytes
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * File type: DOCUMENT, IMAGE, VIDEO, AUDIO, OTHER
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 50, nullable = false)
    private FileType fileType;

    /**
     * Source service that uploaded the file
     */
    @Size(max = 100)
    @Column(name = "source_service", length = 100)
    private String sourceService;

    /**
     * Reference ID in source service (e.g., product_id, order_id)
     */
    @Size(max = 255)
    @Column(name = "source_reference_id", length = 255)
    private String sourceReferenceId;

    /**
     * Presigned URL (temporary, regenerated on demand)
     */
    @Column(name = "presigned_url", length = 2000)
    private String presignedUrl;

    /**
     * URL expiry time
     */
    @Column(name = "url_expires_at")
    private Instant urlExpiresAt;

    /**
     * Whether file is active (soft delete)
     */
    @NotNull
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Additional metadata as JSON
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Determine file type from content type (delegates to FileType enum)
     */
    public static FileType determineFileTypeFromContentType(String contentType) {
        return FileType.fromMimeType(contentType);
    }
    
    /**
     * Determine file type from filename (delegates to FileType enum)
     */
    public static FileType determineFileTypeFromFilename(String filename) {
        return FileType.fromFilename(filename);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaFile)) return false;
        MediaFile that = (MediaFile) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "MediaFile{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                ", filePath='" + filePath + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", fileSize=" + fileSize +
                ", fileType=" + fileType +
                ", isActive=" + isActive +
                '}';
    }
}
