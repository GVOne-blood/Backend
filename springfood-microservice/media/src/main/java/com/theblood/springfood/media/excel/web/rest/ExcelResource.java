package com.theblood.springfood.media.excel.web.rest;

import com.theblood.springfood.media.excel.service.ExcelImportService;
import com.theblood.springfood.media.excel.service.ExcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * REST resources for Excel operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
@Tag(name = "Excel", description = "Excel Import/Export operations")
public class ExcelResource {

    private final ExcelService excelService;

    /**
     * Read headers from an Excel file
     */
    @PostMapping("/headers")
    @Operation(summary = "Read headers from Excel file")
    public ResponseEntity<List<String>> readHeaders(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "headerRow", defaultValue = "0") int headerRow) {
        try {
            List<String> headers = excelService.readHeaders(file, headerRow);
            return ResponseEntity.ok(headers);
        } catch (IOException e) {
            log.error("Error reading headers from Excel file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Preview data from an Excel file (as map)
     */
    @PostMapping("/preview")
    @Operation(summary = "Preview Excel file data")
    public ResponseEntity<List<Map<String, String>>> previewExcel(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "headerRow", defaultValue = "0") int headerRow) {
        try {
            List<Map<String, String>> data = excelService.importAsMap(file, headerRow);
            return ResponseEntity.ok(data);
        } catch (IOException e) {
            log.error("Error previewing Excel file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create an empty template
     */
    @PostMapping("/template")
    @Operation(summary = "Create Excel template with headers")
    public ResponseEntity<byte[]> createTemplate(
        @RequestBody TemplateRequest request) {
        try {
            byte[] template = excelService.createTemplate(
                request.headers(),
                request.sheetName() != null ? request.sheetName() : "Template"
            );

            String filename = request.filename() != null ? request.filename() : "template.xlsx";

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodeFilename(filename) + "\"")
                .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(template);
        } catch (IOException e) {
            log.error("Error creating template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export data to Excel
     */
    @PostMapping("/export")
    @Operation(summary = "Export data to Excel")
    public ResponseEntity<byte[]> exportToExcel(@RequestBody ExportRequest request) {
        try {
            byte[] excelBytes = excelService.exportToExcel(
                request.headers(),
                request.data(),
                request.sheetName()
            );

            String filename = request.filename() != null ? request.filename() : "export.xlsx";

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodeFilename(filename) + "\"")
                .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
        } catch (IOException e) {
            log.error("Error exporting to Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export data using template
     */
    @PostMapping("/export-from-template")
    @Operation(summary = "Export data using Excel template")
    public ResponseEntity<byte[]> exportFromTemplate(
        @RequestParam("template") MultipartFile template,
        @RequestParam("data") String dataJson) {
        try {
            // Parse JSON data
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> data = mapper.readValue(dataJson, Map.class);

            byte[] excelBytes = excelService.exportFromTemplate(
                template.getBytes(),
                data
            );

            String filename = "export_" + System.currentTimeMillis() + ".xlsx";

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodeFilename(filename) + "\"")
                .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
        } catch (IOException e) {
            log.error("Error exporting from template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper method to encode filename for Content-Disposition header
    private String encodeFilename(String filename) {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
    }

    // Request DTOs

    public record TemplateRequest(
        List<String> headers,
        String sheetName,
        String filename
    ) {
    }

    public record ExportRequest(
        List<String> headers,
        List<List<Object>> data,
        String sheetName,
        String filename
    ) {
    }
}
