package com.theblood.statisticalreport.service.carbone;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.theblood.minio.core.impl.MinIOClientCustomImpl;
import com.theblood.statisticalreport.service.carbone.dto.CarboneResponseData;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Carbone Cloud API Service
 * 
 * Tích hợp với Carbone Cloud (https://carbone.io) để render reports.
 * Carbone Cloud là SaaS solution, không cần self-host server.
 * 
 * Workflow:
 * 1. Download template từ MinIO
 * 2. Upload template lên Carbone Cloud (cached by template ID)
 * 3. Render với data
 * 4. Download rendered file
 * 5. Upload lên MinIO
 * 
 * @see <a href="https://carbone.io/api-reference.html">Carbone Cloud API</a>
 */
@Service
@ConditionalOnProperty(name = "carbone.cloud.enabled", havingValue = "true")
public class CarboneCloudService {

    private static final Logger log = LoggerFactory.getLogger(CarboneCloudService.class);
    private static final String CARBONE_CLOUD_API = "https://api.carbone.io";

    @Autowired
    @Qualifier("carboneHttpClient")
    private OkHttpClient httpClient;

    @Autowired
    @Qualifier("carboneGson")
    private Gson gson;

    @Value("${carbone.cloud.api-token}")
    private String apiToken;

    @Value("${carbone.cloud.api-version:4}")
    private String apiVersion;

    @Autowired
    @Qualifier("templateMinioClient")
    private MinIOClientCustomImpl templateMinioClient;

    @Autowired
    @Qualifier("reportMinioClient")
    private MinIOClientCustomImpl reportMinioClient;

    @Value("${minio.template-bucket:springfood-input}")
    private String templateBucket;

    @Value("${minio.report-bucket:springfood-carbone-out}")
    private String reportBucket;

    // Cache: templateName -> templateId
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    /**
     * Main method: Render report với template từ MinIO
     */
    public CarboneResponseData renderReport(
        Object data,
        String templateName,
        String reportName,
        String convertTo
    ) {
        try {
            log.info("Starting Carbone Cloud render - Template: {}, Output: {}, Format: {}",
                templateName, reportName, convertTo);

            // Step 1: Download template từ MinIO
            byte[] templateBytes = downloadTemplateFromMinio(templateName);

            // Step 2: Upload template lên Carbone Cloud (hoặc lấy từ cache)
            String templateId = uploadTemplate(templateBytes, templateName);

            // Step 3: Render document
            byte[] renderedBytes = renderWithTemplateId(templateId, data, convertTo);

            // Step 4: Upload lên MinIO
            String minioUrl = uploadReportToMinio(renderedBytes, reportName, convertTo);

            log.info("Render completed successfully. File available at: {}", minioUrl);
            return CarboneResponseData.builder()
                .fileName(reportName)
                .url(minioUrl)
                .bucketName(reportBucket)
                .build();

        } catch (Exception e) {
            log.error("Failed to render report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to render report: " + e.getMessage(), e);
        }
    }

    /**
     * Overload method với reportName tự động
     */
    public CarboneResponseData renderReport(Object data, String templateName, String convertTo) {
        String reportName = generateReportName(templateName, convertTo);
        return renderReport(data, templateName, reportName, convertTo);
    }

    /**
     * Upload template lên Carbone Cloud
     * Template ID được cache để tránh upload lại
     */
    private String uploadTemplate(byte[] templateBytes, String templateName) throws IOException {
        // Check cache
        String cachedId = templateCache.get(templateName);
        if (cachedId != null) {
            log.debug("Using cached template ID for: {}", templateName);
            return cachedId;
        }

        log.info("Uploading template to Carbone Cloud: {}", templateName);

        // Build multipart request
        RequestBody fileBody = RequestBody.create(
            templateBytes,
            MediaType.parse("application/octet-stream")
        );

        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("template", templateName, fileBody)
            .build();

        Request request = new Request.Builder()
            .url(CARBONE_CLOUD_API + "/template")
            .post(requestBody)
            .addHeader("Authorization", "Bearer " + apiToken)
            .addHeader("carbone-version", apiVersion)
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
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
     * Render document với template ID
     * Carbone Cloud API có 2 bước:
     * 1. POST /render/{templateId} → lấy renderId
     * 2. GET /render/{renderId} → download file
     */
    private byte[] renderWithTemplateId(
        String templateId,
        Object data,
        String convertTo
    ) throws IOException {
        log.info("Rendering with Carbone Cloud - Template ID: {}, Format: {}", templateId, convertTo);

        // Unwrap data if needed
        Object unwrappedData = unwrapData(data);

        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", unwrappedData);
        requestBody.put("convertTo", convertTo);

        String jsonBody = gson.toJson(requestBody);
        log.debug("Render request body: {}", jsonBody);

        // Step 1: Request render và lấy renderId
        Request renderRequest = new Request.Builder()
            .url(CARBONE_CLOUD_API + "/render/" + templateId)
            .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
            .addHeader("Authorization", "Bearer " + apiToken)
            .addHeader("carbone-version", apiVersion)
            .addHeader("Content-Type", "application/json")
            .build();

        String renderId;
        try (Response response = httpClient.newCall(renderRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("Carbone Cloud render request failed: {} - {}", response.code(), errorBody);
                throw new RuntimeException("Carbone Cloud render request failed: " + response.code());
            }

            String responseBody = response.body().string();
            log.debug("Render response: {}", responseBody);

            // Parse response: {"success": true, "data": {"renderId": "..."}}
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            renderId = json.getAsJsonObject("data").get("renderId").getAsString();
            log.info("Got render ID: {}", renderId);
        }

        // Step 2: Download rendered file
        log.info("Downloading rendered file with ID: {}", renderId);
        Request downloadRequest = new Request.Builder()
            .url(CARBONE_CLOUD_API + "/render/" + renderId)
            .get()
            .addHeader("Authorization", "Bearer " + apiToken)
            .addHeader("carbone-version", apiVersion)
            .build();

        try (Response response = httpClient.newCall(downloadRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("Failed to download rendered file: {} - {}", response.code(), errorBody);
                throw new RuntimeException("Failed to download rendered file: " + response.code());
            }

            byte[] fileBytes = response.body().bytes();
            log.info("Downloaded rendered file: {} bytes", fileBytes.length);
            return fileBytes;
        }
    }

    /**
     * Download template từ MinIO
     */
    private byte[] downloadTemplateFromMinio(String templateName) throws IOException {
        try {
            log.debug("Downloading template: {} from bucket: {}", templateName, templateBucket);

            InputStream inputStream = templateMinioClient.getObject(templateName);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            byte[] templateBytes = buffer.toByteArray();
            log.debug("Downloaded template: {} bytes", templateBytes.length);
            return templateBytes;

        } catch (Exception e) {
            log.error("Failed to download template from MinIO: {}", templateName, e);
            throw new IOException("Template not found in MinIO: " + templateName, e);
        }
    }

    /**
     * Upload rendered file lên MinIO
     */
    private String uploadReportToMinio(byte[] fileBytes, String fileName, String convertTo) {
        try {
            log.info("Uploading report to bucket '{}': {}", reportBucket, fileName);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(fileBytes);

            var response = reportMinioClient.upload(outputStream, fileName);

            // Get presigned URL (7 days expiry)
            String url = reportMinioClient.getPresignedUrl(fileName, 7 * 24 * 3600);

            log.info("Report uploaded successfully: {}", url);
            return url;

        } catch (Exception e) {
            log.error("Failed to upload report to MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload to MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Unwrap data if it's wrapped in a single-key map
     */
    private Object unwrapData(Object data) {
        if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            if (dataMap.size() == 1) {
                Object firstValue = dataMap.values().iterator().next();
                log.debug("Unwrapping single-key map. Key: {}", dataMap.keySet().iterator().next());
                return firstValue;
            }
        }
        return data;
    }

    /**
     * Generate report name từ template name
     */
    private String generateReportName(String templateName, String convertTo) {
        String baseName = templateName.replaceFirst("[.][^.]+$", "");
        return String.format("%s_%d.%s", baseName, System.currentTimeMillis(), convertTo);
    }

    /**
     * Clear template cache (useful khi template được update)
     */
    public void clearTemplateCache(String templateName) {
        templateCache.remove(templateName);
        log.info("Cleared template cache for: {}", templateName);
    }

    /**
     * Clear all template cache
     */
    public void clearAllTemplateCache() {
        templateCache.clear();
        log.info("Cleared all template cache");
    }
}
