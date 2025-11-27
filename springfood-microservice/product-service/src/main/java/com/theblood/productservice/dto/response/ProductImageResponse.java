package com.theblood.productservice.dto.response;

import com.theblood.minio.response.MinIOResponse;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductImageResponse {

    UUID productId;
    LocalDateTime saveAt;
    List<MinIOResponse> minIOResponses;

}
