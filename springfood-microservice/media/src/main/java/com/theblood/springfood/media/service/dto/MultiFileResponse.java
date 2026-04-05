package com.theblood.springfood.media.service.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultiFileResponse {
    int total;
    int success;
    int fail;
    List<FileResponse> uploadedFile;
    List<UploadStatusResponse> failedFile;
}
