package com.theblood.springfood.media.service.dto;

import com.theblood.springfood.media.domain.enums.UploadModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultiFileRequest {

    MultipartFile[] multipartFile;
    boolean isDifferentType;
    UploadModule uploadModule;
    String description;

}
