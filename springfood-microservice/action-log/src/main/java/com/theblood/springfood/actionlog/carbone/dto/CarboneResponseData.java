package com.theblood.springfood.actionlog.carbone.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CarboneResponseData {
    String etag;
    String versionId;
    String fileName;
    String bucketName;
    String url;
    String viewUrl;
}
