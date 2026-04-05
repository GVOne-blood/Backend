package com.theblood.springfood.media.service;

import com.theblood.springfood.media.service.dto.FileRequest;
import com.theblood.springfood.media.service.dto.FileResponse;
import com.theblood.springfood.media.service.dto.MultiFileRequest;
import com.theblood.springfood.media.service.dto.MultiFileResponse;

public interface MediaUploadService {

    FileResponse uploadSingleFile(FileRequest fileRequest);

    MultiFileResponse uploadMultipleFiles(MultiFileRequest fileRequests);
}
