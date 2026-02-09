package com.theblood.springfood.media.excel.service;

import com.theblood.springfood.media.excel.util.ExcelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for exporting data to Excel files.
 * Supports creating new Excel files and filling templates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    private static final Pattern LOOP_START_PATTERN = Pattern.compile("\\#\\{forEach:([^}]+)\\}");
    private static final Pattern LOOP_END_PATTERN = Pattern.compile("\\#\\{/forEach\\}");

    /**
     * Export data to new Excel file
     *
     * @param headers    Column headers
     * @param data       List of data rows (each row is a list of values)
     * @param sheetName  Name of the sheet
     * @return Excel file as byte array
     */
    public byte[] exportToExcel(List<String> headers, 
                                 List<List<Object>> data, 
                                 String sheetName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(sheetName != null ? sheetName : "Sheet1");
            
            // Create header style
            CellStyle headerStyle = ExcelUtils.StyleUtils.createHeaderStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (List<Object> rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.size(); i++) {
                    Cell cell = row.createCell(i);
                    ExcelUtils.CellWriter.setValue(cell, rowData.get(i));
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export list of objects to Excel
     *
     * @param items      List of items to export
     * @param headers    Column headers
     * @param rowWriter  Function to write each item to a row
     * @param sheetName  Name of the sheet
     * @param <T>        Type of items
     * @return Excel file as byte array
     */
    public <T> byte[] exportToExcel(List<T> items,
                                     List<String> headers,
                                     BiConsumer<Row, T> rowWriter,
                                     String sheetName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(sheetName != null ? sheetName : "Sheet1");
            
            // Create header style
            CellStyle headerStyle = ExcelUtils.StyleUtils.createHeaderStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (T item : items) {
                Row row = sheet.createRow(rowNum++);
                rowWriter.accept(row, item);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export data using a template file
     * Supports placeholders like ${fieldName} and loops #{forEach:items}...#{/forEach}
     *
     * @param templateBytes Template file as byte array
     * @param data          Map of data to fill
     * @return Filled Excel file as byte array
     */
    public byte[] exportFromTemplate(byte[] templateBytes, 
                                      Map<String, Object> data) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(templateBytes);
             Workbook workbook = WorkbookFactory.create(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                processSheet(sheet, data);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export data using a template with list data (for table filling)
     *
     * @param templateBytes  Template file as byte array
     * @param headerData     Data for header placeholders
     * @param listData       List data for table rows
     * @param dataStartRow   Row index where data should start (0-indexed)
     * @param rowWriter      Function to write each item to a row
     * @param <T>            Type of list items
     * @return Filled Excel file as byte array
     */
    public <T> byte[] exportFromTemplateWithList(byte[] templateBytes,
                                                   Map<String, Object> headerData,
                                                   List<T> listData,
                                                   int dataStartRow,
                                                   BiConsumer<Row, T> rowWriter) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(templateBytes);
             Workbook workbook = WorkbookFactory.create(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Fill header placeholders
            for (int i = 0; i < dataStartRow; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    processRowPlaceholders(row, headerData);
                }
            }
            
            // Fill data rows
            int currentRow = dataStartRow;
            for (T item : listData) {
                Row row = sheet.getRow(currentRow);
                if (row == null) {
                    row = sheet.createRow(currentRow);
                }
                rowWriter.accept(row, item);
                currentRow++;
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Create empty template with headers
     */
    public byte[] createTemplate(List<String> headers, String sheetName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(sheetName != null ? sheetName : "Template");
            
            // Create header style
            CellStyle headerStyle = ExcelUtils.StyleUtils.createHeaderStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000); // Default width
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // Private methods

    private void processSheet(Sheet sheet, Map<String, Object> data) {
        List<Integer> rowsToProcess = new ArrayList<>();
        List<Integer> loopStartRows = new ArrayList<>();
        List<Integer> loopEndRows = new ArrayList<>();
        
        // First pass: identify loops and regular rows
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                if (isLoopStartRow(row)) {
                    loopStartRows.add(i);
                } else if (isLoopEndRow(row)) {
                    loopEndRows.add(i);
                } else {
                    rowsToProcess.add(i);
                }
            }
        }
        
        // Process loops (from bottom to top to maintain row indices)
        for (int i = loopStartRows.size() - 1; i >= 0; i--) {
            if (i < loopEndRows.size()) {
                processLoop(sheet, loopStartRows.get(i), loopEndRows.get(i), data);
            }
        }
        
        // Process regular placeholders
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                processRowPlaceholders(row, data);
            }
        }
    }

    private void processRowPlaceholders(Row row, Map<String, Object> data) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String cellValue = cell.getStringCellValue();
                String newValue = replacePlaceholders(cellValue, data);
                if (!cellValue.equals(newValue)) {
                    // Try to detect value type
                    setSmartCellValue(cell, newValue, data, cellValue);
                }
            }
        }
    }

    private void setSmartCellValue(Cell cell, String newValue, Map<String, Object> data, String originalValue) {
        // Extract placeholder name to get original value type
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(originalValue);
        if (matcher.find() && originalValue.equals("${" + matcher.group(1) + "}")) {
            // Single placeholder - use original type
            String key = matcher.group(1);
            Object value = getNestedValue(data, key);
            if (value != null) {
                ExcelUtils.CellWriter.setValue(cell, value);
                return;
            }
        }
        // Default: set as string
        cell.setCellValue(newValue);
    }

    private String replacePlaceholders(String template, Map<String, Object> data) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = getNestedValue(data, key);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    private Object getNestedValue(Map<String, Object> data, String key) {
        String[] parts = key.split("\\.");
        Object current = data;
        
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
            if (current == null) return null;
        }
        
        return current;
    }

    private boolean isLoopStartRow(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                if (LOOP_START_PATTERN.matcher(cell.getStringCellValue()).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isLoopEndRow(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                if (LOOP_END_PATTERN.matcher(cell.getStringCellValue()).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void processLoop(Sheet sheet, int startRow, int endRow, Map<String, Object> data) {
        Row loopStartRow = sheet.getRow(startRow);
        if (loopStartRow == null) return;
        
        // Find loop variable name
        String loopVar = null;
        for (int i = 0; i < loopStartRow.getLastCellNum(); i++) {
            Cell cell = loopStartRow.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                Matcher matcher = LOOP_START_PATTERN.matcher(cell.getStringCellValue());
                if (matcher.find()) {
                    loopVar = matcher.group(1);
                    break;
                }
            }
        }
        
        if (loopVar == null) return;
        
        Object listData = data.get(loopVar);
        if (!(listData instanceof List)) return;
        
        List<?> items = (List<?>) listData;
        Row templateRow = sheet.getRow(startRow + 1); // Row between loop markers
        
        if (templateRow == null) return;
        
        // Delete loop markers
        sheet.removeRow(loopStartRow);
        sheet.removeRow(sheet.getRow(endRow));
        
        // Shift rows and fill data
        int insertRow = startRow;
        for (Object item : items) {
            Row newRow = sheet.createRow(insertRow++);
            copyRowStyle(templateRow, newRow);
            
            if (item instanceof Map) {
                processRowPlaceholders(newRow, (Map<String, Object>) item);
            }
        }
    }

    private void copyRowStyle(Row sourceRow, Row targetRow) {
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            Cell sourceCell = sourceRow.getCell(i);
            Cell targetCell = targetRow.createCell(i);
            
            if (sourceCell != null) {
                targetCell.setCellStyle(sourceCell.getCellStyle());
                if (sourceCell.getCellType() == CellType.STRING) {
                    targetCell.setCellValue(sourceCell.getStringCellValue());
                }
            }
        }
    }
}
