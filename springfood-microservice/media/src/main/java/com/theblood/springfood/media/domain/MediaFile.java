package com.theblood.springfood.media.domain;

import com.theblood.springfood.common.enums.FileType;
import com.theblood.springfood.media.domain.enums.UploadModule;
import com.theblood.springfood.media.domain.enums.UploadStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "media_file")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class MediaFile extends AbstractAuditingEntity<String> {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(generator = "UUID")
    @org.hibernate.annotations.GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "file_original_name", nullable = false, length = 255)
    private String fileOriginalName;

    @Column(name = "file_stored_name", nullable = false, length = 255)
    private String fileStoredName;

    @Column(name = "file_type", nullable = false, length = 100)
    private FileType fileType;

    //unit: KB
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    @Column(name = "file_hash", nullable = false, length = 64)
    private String fileHash;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "stored_bucket_name", length = 255)
    private String bucketName;

    @Column(name = "upload_status")
    private UploadStatus uploadStatus;

    @Column(name = "upload_module")
    private UploadModule uploadModule;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive = true;


}
