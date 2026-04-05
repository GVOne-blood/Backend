package com.theblood.springfood.media.service.dto;

import com.theblood.springfood.media.domain.enums.UploadStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadStatusResponse {

    String fileName;
    UploadStatus uploadStatus;
    String reason;

}
