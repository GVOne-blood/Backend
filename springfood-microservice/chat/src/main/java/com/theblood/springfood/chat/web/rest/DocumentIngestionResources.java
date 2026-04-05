package com.theblood.springfood.chat.web.rest;

import com.theblood.springfood.chat.service.rag.DocumentIngestionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("rag")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
public class DocumentIngestionResources {

    DocumentIngestionService documentIngestionService;

    @PostMapping("/file-import")
    public ResponseEntity<?> importDataRag(@RequestBody MultipartFile file) {
        // TODO: Implement file import logic
        return ResponseEntity.ok("File import endpoint - implementation pending");
    }
}
