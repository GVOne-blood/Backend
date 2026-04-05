package com.theblood.statisticalreport.service.carbone;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.theblood.minio.core.impl.MinIOClientCustomImpl;
import com.theblood.statisticalreport.service.carbone.dto.CarboneBody;
import com.theblood.statisticalreport.service.carbone.dto.CarboneOption;
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

@Service
@ConditionalOnProperty(name = "carbone.base-url")
public class CarboneService {

    private static final Logger log = LoggerFactory.getLogger(CarboneService.class);

    @Autowired
    @Qualifier("carboneHttpClient")
    OkHttpClient carboneHttpClient;

    @Value("${carbone.base-url}")
    private String carboneBaseUrl;

    @Autowired
    @Qualifier("carboneGson")
    private Gson carboneGson;

    @Autowired
    private CarboneTemplateService templateService;

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

    /**
     * Main method: Render report với template từ MinIO
     *
     * @param data         Dữ liệu để merge vào template
     * @param templateName Tên template trong MinIO bucket (vd: "report_template.xlsx")
     * @param reportName   Tên file output (vd: "monthly_report.pdf")
     * @param convertTo    Format output: "pdf", "xlsx", "docx", etc.
     * @return CarboneResponseData chứa URL download từ MinIO
     */
    public CarboneResponseData renderReport(
        Object data,
        String templateName,
        String reportName,
        String convertTo
    ) {
        try {
            log.info("Starting render process - Template: {}, Output: {}, Format: {}",
                templateName, reportName, convertTo);

            // Log data structure for debugging
            log.debug("Input data structure: {}", carboneGson.toJson(data));

            // Step 1: Download template từ MinIO
            log.debug("Downloading template from MinIO bucket: {}", templateBucket);
            byte[] templateBytes = downloadTemplateFromMinio(templateName);

            // Step 2: Upload template lên Carbone và lấy template ID
            String templateId = templateService.uploadTemplate(templateBytes, templateName);

            // Step 3: Render document với Carbone
            String renderId = renderWithTemplateId(templateId, data, convertTo, reportName);

            // Step 4: Download rendered file từ Carbone
            byte[] renderedBytes = downloadRenderedFile(renderId);

            // Step 5: Upload lên MinIO
            String minioUrl = uploadReportToMinio(renderedBytes, reportName, convertTo);

            // Step 6: Return response
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
     * Download template từ MinIO
     */
    private byte[] downloadTemplateFromMinio(String templateName) throws IOException {
        try {
            log.debug("Downloading template: {} from bucket: {}", templateName, templateBucket);

            // Use templateMinioClient which is configured for template bucket
            InputStream inputStream = templateMinioClient.getObject(templateName);

            // Convert InputStream to byte array
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
     * Render document với template ID
     */
    private String renderWithTemplateId(
        String templateId,
        Object data,
        String convertTo,
        String reportName
    ) throws IOException {
        log.info("=== RENDER DEBUG ===");
        log.info("Template ID: {}", templateId);
        log.info("Data received: {}", carboneGson.toJson(data));
        log.info("Convert to: {}", convertTo);
        log.info("Report name: {}", reportName);

        // Unwrap data if it's wrapped in a Map with single key
        // If client sends {"d": {...}}, we want to send just {...} to Carbone
        Object unwrappedData = data;
        if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            if (dataMap.size() == 1) {
                Object firstValue = dataMap.values().iterator().next();
                log.info("Unwrapping single-key map. Key: {}", dataMap.keySet().iterator().next());
                unwrappedData = firstValue;
            }
        }

        // Build request body theo Carbone API format
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", unwrappedData);
        requestBody.put("convertTo", convertTo);

        String jsonBody = carboneGson.toJson(requestBody);
        log.info("Request body to Carbone: {}", jsonBody);
        log.info("===================");

        Request request = new Request.Builder()
            .url(carboneBaseUrl + "/render/" + templateId)
            .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
            .addHeader("Content-Type", "application/json")
            .build();

        try (Response response = carboneHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("Carbone render failed: {} - {}", response.code(), errorBody);
                throw new RuntimeException("Carbone render failed: " + response.code());
            }

            String responseBody = response.body().string();
            log.debug("Render response: {}", responseBody);

            // Parse response: {"success": true, "data": {"renderId": "..."}}
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            String renderId = json.getAsJsonObject("data").get("renderId").getAsString();

            log.debug("Render ID: {}", renderId);
            return renderId;
        }
    }

    /**
     * Download rendered file từ Carbone
     */
    private byte[] downloadRenderedFile(String renderId) throws IOException {
        log.debug("Downloading rendered file: {}", renderId);

        Request request = new Request.Builder()
            .url(carboneBaseUrl + "/render/" + renderId)
            .get()
            .build();

        try (Response response = carboneHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("Failed to download rendered file: {} - {}", response.code(), errorBody);
                throw new RuntimeException("Failed to download file: " + response.code());
            }

            byte[] fileBytes = response.body().bytes();
            log.debug("Downloaded rendered file: {} bytes", fileBytes.length);
            return fileBytes;
        }
    }

    /**
     * Upload rendered file lên MinIO report bucket
     */
    private String uploadReportToMinio(byte[] fileBytes, String fileName, String convertTo) {
        try {
            log.info("Uploading report to bucket '{}': {}", reportBucket, fileName);

            // Convert byte[] to ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(fileBytes);

            // Upload directly to report bucket (no subfolder)
            var response = reportMinioClient.upload(outputStream, fileName);

            // Get presigned URL (7 days expiry)
            String url = reportMinioClient.getPresignedUrl(fileName, 7 * 24 * 3600);

            log.info("Report uploaded successfully: {}", url);
            return url;

        } catch (Exception e) {
            log.error("Failed to upload report '{}' to bucket '{}': {}", fileName, reportBucket, e.getMessage(), e);
            throw new RuntimeException("Failed to upload to MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Generate report name từ template name
     */
    private String generateReportName(String templateName, String convertTo) {
        // Remove extension from template name
        String baseName = templateName.replaceFirst("[.][^.]+$", "");
        // Add timestamp and new extension
        return String.format("%s_%d.%s", baseName, System.currentTimeMillis(), convertTo);
    }

    /**
     * Get content type từ format
     */
    private String getContentType(String convertTo) {
        return switch (convertTo.toLowerCase()) {
            case "pdf" -> "application/pdf";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "odt" -> "application/vnd.oasis.opendocument.text";
            case "ods" -> "application/vnd.oasis.opendocument.spreadsheet";
            default -> "application/octet-stream";
        };
    }

    // ========== Legacy methods (deprecated, keep for backward compatibility) ==========

    @Deprecated
    protected String buildJsonBodyRender(String fileTemplate, CarboneOption carboneOption, Object data) {
        CarboneBody carboneBodyDTO = CarboneBody.builder()
            .fileName(fileTemplate)
            .options(carboneOption)
            .data(data)
            .build();
        return carboneGson.toJson(carboneBodyDTO);
    }

    @Deprecated
    protected String buildJsonBodyConvert(String fileTemplate, CarboneOption carboneOption) {
        CarboneBody carboneBodyDTO = CarboneBody.builder()
            .fileName(fileTemplate)
            .options(carboneOption)
            .build();
        return carboneGson.toJson(carboneBodyDTO);
    }

    @Deprecated
    protected String buildJsonBodyMultiRender(CarboneOption carboneOption, Object dataset) {
        CarboneBody carboneBodyDTO = CarboneBody.builder()
            .options(carboneOption)
            .dataset(dataset)
            .build();
        return carboneGson.toJson(carboneBodyDTO);
    }
}
