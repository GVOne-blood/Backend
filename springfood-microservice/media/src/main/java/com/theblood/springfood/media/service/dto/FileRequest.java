package com.theblood.springfood.media.service.dto;


import com.theblood.springfood.media.domain.enums.UploadModule;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileRequest {
    MultipartFile multipartFile;
    UploadModule uploadModule;
    String description;

}
