package com.theblood.productservice.dto;

import com.theblood.minio.response.MinIOResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResult {

    MinIOResponse minioResponse;
    String originFileName;
    String message;
    String contentType;

}
