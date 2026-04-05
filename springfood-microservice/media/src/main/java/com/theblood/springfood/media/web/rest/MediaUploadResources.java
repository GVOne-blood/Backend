package com.theblood.springfood.media.web.rest;

import com.theblood.springfood.media.service.FileValidationService;
import com.theblood.springfood.media.service.MediaUploadService;
import com.theblood.springfood.media.service.dto.FileRequest;
import com.theblood.springfood.media.service.dto.FileResponse;
import com.theblood.springfood.media.service.dto.MultiFileRequest;
import com.theblood.springfood.media.service.dto.MultiFileResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/media/up")
@RequiredArgsConstructor
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MediaUploadResources {

    MediaUploadService mediaUploadService;
    FileValidationService fileValidationService;

    //admin
    @PostMapping("")
    public ResponseEntity<?> uploadSingleFile() {
        return ResponseEntity.ok("Not implemented yet");
    }

    //admin
    @PostMapping("/multiple")
    public ResponseEntity<?> uploadMultipleFiles() {
        return ResponseEntity.ok("Not implemented yet");
    }

    //user
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@ModelAttribute FileRequest fileRequest) {

        fileValidationService.validateFile(fileRequest.getMultipartFile(), fileRequest.getUploadModule());
        FileResponse res = mediaUploadService.uploadSingleFile(fileRequest);
        return ResponseEntity.ok(res);
    }

    //product, comment, review, restaurant image
    @PostMapping("/product")
    public ResponseEntity<?> uploadProductImage(@ModelAttribute MultiFileRequest fileRequest) {
        int fileCount = fileRequest.getMultipartFile().length;
        for (int i = 0; i < fileCount; i++) {
            fileValidationService.validateFile(fileRequest.getMultipartFile()[i], fileRequest.getUploadModule());
        }
        MultiFileResponse res = mediaUploadService.uploadMultipleFiles(fileRequest);
        return ResponseEntity.ok(res);
    }

}
