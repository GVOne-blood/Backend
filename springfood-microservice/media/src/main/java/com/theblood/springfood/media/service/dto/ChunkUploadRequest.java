package com.theblood.springfood.media.service.dto;

import lombok.Data;

@Data
public class ChunkUploadRequest {
    private String uploadId;        // ID từ initiate
    private Integer partNumber;     // Số thứ tự chunk (1-based)
    private Integer totalParts;     // Tổng số chunks
    private String fileName;        // Tên file gốc
    private byte[] chunkData;       // Data của n chunk này
}
