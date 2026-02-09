package com.theblood.springfood.media.excel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Facade service for Excel operations.
 * Provides a unified interface for both import and export functionality.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelService {

    private final ExcelImportService importService;
    private final ExcelExportService exportService;

    // ============== IMPORT OPERATIONS ==============

    /**
     * Import data from Excel file
     *
     * @param file       The uploaded Excel file
     * @param rowMapper  Function to map each row to an object
     * @param headerRows Number of header rows to skip
     * @param <T>        The type of object to create
     * @return List of mapped objects
     */
    public <T> List<T> importFromExcel(MultipartFile file,
                                        Function<Row, T> rowMapper,
                                        int headerRows) throws IOException {
        return importService.importFromExcel(file, rowMapper, headerRows);
    }

    /**
     * Import data from Excel file with specific sheet
     */
    public <T> List<T> importFromExcel(MultipartFile file,
                                        Function<Row, T> rowMapper,
                                        int headerRows,
                                        int sheetIndex) throws IOException {
        return importService.importFromExcel(file, rowMapper, headerRows, sheetIndex);
    }

    /**
     * Import data from Excel bytes
     */
    public <T> List<T> importFromExcel(byte[] excelBytes,
                                        Function<Row, T> rowMapper,
                                        int headerRows) throws IOException {
        return importService.importFromExcel(excelBytes, rowMapper, headerRows);
    }

    /**
     * Import data with validation and error tracking
     */
    public <T> ExcelImportService.ImportResult<T> importWithValidation(
            MultipartFile file,
            Function<Row, T> rowMapper,
            int headerRows) throws IOException {
        return importService.importWithValidation(file, rowMapper, headerRows);
    }

    /**
     * Read headers from Excel file
     */
    public List<String> readHeaders(MultipartFile file, int headerRowIndex) throws IOException {
        return importService.readHeaders(file, headerRowIndex);
    }

    /**
     * Import data as map (header -> value)
     */
    public List<Map<String, String>> importAsMap(MultipartFile file, int headerRowIndex) throws IOException {
        return importService.importAsMap(file, headerRowIndex);
    }

    // ============== EXPORT OPERATIONS ==============

    /**
     * Export data to new Excel file
     */
    public byte[] exportToExcel(List<String> headers,
                                 List<List<Object>> data,
                                 String sheetName) throws IOException {
        return exportService.exportToExcel(headers, data, sheetName);
    }

    /**
     * Export list of objects to Excel
     */
    public <T> byte[] exportToExcel(List<T> items,
                                     List<String> headers,
                                     BiConsumer<Row, T> rowWriter,
                                     String sheetName) throws IOException {
        return exportService.exportToExcel(items, headers, rowWriter, sheetName);
    }

    /**
     * Export data using a template file
     */
    public byte[] exportFromTemplate(byte[] templateBytes,
                                      Map<String, Object> data) throws IOException {
        return exportService.exportFromTemplate(templateBytes, data);
    }

    /**
     * Export data using a template with list data
     */
    public <T> byte[] exportFromTemplateWithList(byte[] templateBytes,
                                                   Map<String, Object> headerData,
                                                   List<T> listData,
                                                   int dataStartRow,
                                                   BiConsumer<Row, T> rowWriter) throws IOException {
        return exportService.exportFromTemplateWithList(templateBytes, headerData, listData, dataStartRow, rowWriter);
    }

    /**
     * Create empty template with headers
     */
    public byte[] createTemplate(List<String> headers, String sheetName) throws IOException {
        return exportService.createTemplate(headers, sheetName);
    }
}
