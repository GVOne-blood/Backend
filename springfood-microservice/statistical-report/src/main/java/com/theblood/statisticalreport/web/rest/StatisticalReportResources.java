package com.theblood.statisticalreport.web.rest;

import com.theblood.statisticalreport.service.carbone.CarboneCloudService;
import com.theblood.statisticalreport.service.carbone.CarboneService;
import com.theblood.statisticalreport.service.carbone.dto.CarboneResponseData;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistical-reports")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticalReportResources {

    @Autowired(required = false)
    CarboneService carboneService;

    @Autowired(required = false)
    CarboneCloudService carboneCloudService;

    @Value("${carbone.cloud.enabled:false}")
    boolean carboneCloudEnabled;

    /**
     * Render report từ template đã upload lên MinIO.
     * 
     * Tự động chọn giữa Carbone Cloud hoặc Self-hosted dựa trên config.
     *
     * @param templateFileName tên file template trên MinIO (vd: "report_template.xlsx")
     * @param reportName       tên file output (vd: "monthly_report.pdf"), optional
     * @param convertTo        định dạng output: "pdf", "xlsx", "docx"
     * @param data             dữ liệu JSON điền vào template
     */
    @PostMapping("/render")
    public ResponseEntity<CarboneResponseData> renderReport(
        @RequestParam String templateFileName,
        @RequestParam(required = false) String reportName,
        @RequestParam(defaultValue = "pdf") String convertTo,
        @RequestBody Map<String, Object> data
    ) {
        CarboneResponseData result;
        
        // Chọn service dựa trên config
        if (carboneCloudEnabled) {
            if (carboneCloudService == null) {
                throw new IllegalStateException("Carbone Cloud is enabled but service is not available. Check your API token.");
            }
            if (reportName != null && !reportName.isBlank()) {
                result = carboneCloudService.renderReport(data, templateFileName, reportName, convertTo);
            } else {
                result = carboneCloudService.renderReport(data, templateFileName, convertTo);
            }
        } else {
            if (carboneService == null) {
                throw new IllegalStateException("Carbone self-hosted is not available. Check your configuration.");
            }
            if (reportName != null && !reportName.isBlank()) {
                result = carboneService.renderReport(data, templateFileName, reportName, convertTo);
            } else {
                result = carboneService.renderReport(data, templateFileName, convertTo);
            }
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Clear template cache (chỉ áp dụng cho Carbone Cloud)
     */
    @DeleteMapping("/cache/template/{templateName}")
    public ResponseEntity<Void> clearTemplateCache(@PathVariable String templateName) {
        if (carboneCloudEnabled && carboneCloudService != null) {
            carboneCloudService.clearTemplateCache(templateName);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Clear all template cache (chỉ áp dụng cho Carbone Cloud)
     */
    @DeleteMapping("/cache/template")
    public ResponseEntity<Void> clearAllTemplateCache() {
        if (carboneCloudEnabled && carboneCloudService != null) {
            carboneCloudService.clearAllTemplateCache();
        }
        return ResponseEntity.noContent().build();
    }
}
