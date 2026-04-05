package com.theblood.springfood.media.service.dto;

import com.theblood.springfood.common.enums.FileType;
import com.theblood.springfood.media.domain.enums.UploadStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class FileResponse {

    String fileId;
    String fileName;
    String fileUrl;
    Long fileSize;
    FileType fileType;
    UploadStatus uploadStatus;
    String uploadDate;

}
