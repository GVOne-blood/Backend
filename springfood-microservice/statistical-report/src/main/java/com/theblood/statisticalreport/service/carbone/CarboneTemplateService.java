package com.theblood.statisticalreport.service.carbone;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service để quản lý template với Carbone Server
 * - Upload template và cache template ID
 * - Tránh upload lại template mỗi lần render
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CarboneTemplateService {

    @Qualifier("carboneHttpClient")
    private final OkHttpClient client;

    @Value("${carbone.base-url}")
    private String carboneBaseUrl;

    // Cache: templateName -> templateId
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    /**
     * Upload template lên Carbone và lấy template ID
     * Template ID được cache để tránh upload lại
     */
    public String uploadTemplate(byte[] templateBytes, String templateName) throws IOException {
        // Check cache
        String cachedId = templateCache.get(templateName);
        if (cachedId != null) {
            log.debug("Using cached template ID for: {}", templateName);
            return cachedId;
        }

        log.info("Uploading template to Carbone: {}", templateName);

        // Upload template
        RequestBody fileBody = RequestBody.create(
            templateBytes,
            MediaType.parse("application/octet-stream")
        );

        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("template", templateName, fileBody)
            .build();

        Request request = new Request.Builder()
            .url(carboneBaseUrl + "/template")
            .post(requestBody)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("Failed to upload template: {} - {}", response.code(), errorBody);
                throw new RuntimeException("Failed to upload template: " + response.code());
            }

            String responseBody = response.body().string();
            log.debug("Upload template response: {}", responseBody);

            // Parse response: {"success": true, "data": {"templateId": "..."}}
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            String templateId = json.getAsJsonObject("data").get("templateId").getAsString();

            // Cache it
            templateCache.put(templateName, templateId);
            log.info("Template uploaded successfully: {} -> {}", templateName, templateId);

            return templateId;
        }
    }

    /**
     * Clear cache cho một template cụ thể
     * Sử dụng khi template được update
     */
    public void clearCache(String templateName) {
        templateCache.remove(templateName);
        log.info("Cleared template cache for: {}", templateName);
    }

    /**
     * Clear toàn bộ cache
     */
    public void clearAllCache() {
        templateCache.clear();
        log.info("Cleared all template cache");
    }
}
