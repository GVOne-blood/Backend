package com.theblood.springfood.media.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChunkUploadResponse {
    private String uploadId;
    private Integer partNumber;
    private String etag;           // ETag từ MinIO
    private Boolean completed;     // Đã upload hết chưa?
    private Integer uploadedParts; // Số parts đã upload
    private Integer totalParts;
}
