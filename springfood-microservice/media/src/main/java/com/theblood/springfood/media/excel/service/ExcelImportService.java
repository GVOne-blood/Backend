package com.theblood.springfood.media.excel.service;

import com.theblood.springfood.media.excel.util.ExcelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

/**
 * Service for importing data from Excel files.
 * Supports reading Excel files (.xlsx, .xls) and converting rows to DTOs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    /**
     * Import data from Excel file and convert to list of objects
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
        validateFile(file);
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            
            return processWorkbook(workbook, rowMapper, headerRows, 0);
        }
    }

    /**
     * Import data from Excel file with specific sheet index
     */
    public <T> List<T> importFromExcel(MultipartFile file, 
                                        Function<Row, T> rowMapper, 
                                        int headerRows,
                                        int sheetIndex) throws IOException {
        validateFile(file);
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            
            return processWorkbook(workbook, rowMapper, headerRows, sheetIndex);
        }
    }

    /**
     * Import data from Excel bytes
     */
    public <T> List<T> importFromExcel(byte[] excelBytes, 
                                        Function<Row, T> rowMapper, 
                                        int headerRows) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(excelBytes);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            
            return processWorkbook(workbook, rowMapper, headerRows, 0);
        }
    }

    /**
     * Import data with validation and error tracking
     */
    public <T> ImportResult<T> importWithValidation(MultipartFile file,
                                                     Function<Row, T> rowMapper,
                                                     int headerRows) throws IOException {
        validateFile(file);
        
        List<T> successItems = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            
            for (int i = headerRows; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                
                if (row == null || ExcelUtils.RowUtils.isRowEmpty(row)) {
                    continue;
                }
                
                try {
                    T item = rowMapper.apply(row);
                    if (item != null) {
                        successItems.add(item);
                    }
                } catch (Exception e) {
                    errors.add(new ImportError(i + 1, e.getMessage()));
                    log.warn("Error processing row {}: {}", i + 1, e.getMessage());
                }
            }
        }
        
        return new ImportResult<>(successItems, errors, successItems.size(), errors.size());
    }

    /**
     * Read headers from Excel file
     */
    public List<String> readHeaders(MultipartFile file, int headerRowIndex) throws IOException {
        validateFile(file);
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(headerRowIndex);
            
            if (headerRow == null) {
                return Collections.emptyList();
            }
            
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                headers.add(ExcelUtils.CellReader.asString(cell));
            }
            return headers;
        }
    }

    /**
     * Read data as map (header -> value) for each row
     */
    public List<Map<String, String>> importAsMap(MultipartFile file, int headerRowIndex) throws IOException {
        validateFile(file);
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(headerRowIndex);
            
            if (headerRow == null) {
                return Collections.emptyList();
            }
            
            // Read headers
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                String header = ExcelUtils.CellReader.asString(cell);
                headers.add(header != null ? header : "Column_" + i);
            }
            
            // Read data rows
            List<Map<String, String>> result = new ArrayList<>();
            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                
                if (row == null || ExcelUtils.RowUtils.isRowEmpty(row)) {
                    continue;
                }
                
                Map<String, String> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    rowData.put(headers.get(j), ExcelUtils.CellReader.asString(cell));
                }
                result.add(rowData);
            }
            
            return result;
        }
    }

    // Private methods

    private <T> List<T> processWorkbook(Workbook workbook, 
                                         Function<Row, T> rowMapper, 
                                         int headerRows,
                                         int sheetIndex) {
        List<T> result = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        
        for (int i = headerRows; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            
            if (row == null || ExcelUtils.RowUtils.isRowEmpty(row)) {
                continue;
            }
            
            try {
                T item = rowMapper.apply(row);
                if (item != null) {
                    result.add(item);
                }
            } catch (Exception e) {
                log.warn("Error processing row {}: {}", i + 1, e.getMessage());
            }
        }
        
        return result;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || 
            (!filename.toLowerCase().endsWith(".xlsx") && 
             !filename.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("Invalid file format. Only .xlsx and .xls files are supported");
        }
    }

    // Inner classes for result tracking

    public record ImportResult<T>(
        List<T> items,
        List<ImportError> errors,
        int successCount,
        int errorCount
    ) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public boolean isSuccess() {
            return errors.isEmpty();
        }
    }

    public record ImportError(
        int rowNumber,
        String message
    ) {}
}
