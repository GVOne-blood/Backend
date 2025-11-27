package com.theblood.productservice.model;

import com.theblood.common.enums.FileStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_images")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductImages extends AbstractEntity {

    @Column(name = "product_id")
    UUID product_id;

    @Column(name = "bucket_name", nullable = false)
    String bucketName;

    @Column(name = "object_name")
    String objectName;

    @Column(name = "original_file_name")
    String originalFileName;

    @Column(name = "file_size")
    Long fileSize;

    @Column(name = "image_url")
    String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    FileStatus status;

    @Column(name = "uploaded_by")
    String uploadedBy;

}
