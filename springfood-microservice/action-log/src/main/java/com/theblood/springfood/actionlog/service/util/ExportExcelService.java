package com.theblood.springfood.actionlog.service.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExportExcelService {
    public byte[] export(String title, String subtitle,
                         String date, String sheetName,
                         List<String> headers, List<List<Object>> data,
                         int[] customWidths) throws IOException {

        try (var workbook = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet(sheetName);

            // Styles
            var titleStyle = createTitleStyle(workbook);
            var subtitleStyle = createSubtitleStyle(workbook);
            var headerStyle = createHeaderStyle(workbook);
            var dataStyle = createDataStyle(workbook);

            int row = 0;

            // Title
            var titleRow = sheet.createRow(row++);
            titleRow.createCell(0).setCellValue(title);
            titleRow.getCell(0).setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.size() - 1));

            // Subtitle
            var subtitleRow = sheet.createRow(row++);
            subtitleRow.createCell(0).setCellValue(subtitle);
            subtitleRow.getCell(0).setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.size() - 1));

            // Date
            var dateRow = sheet.createRow(row++);
            dateRow.createCell(0).setCellValue(date);
            dateRow.getCell(0).setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, headers.size() - 1));

            // Empty row
            sheet.createRow(row++);

            // Headers
            var headerRow = sheet.createRow(row++);
            for (int i = 0; i < headers.size(); i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Data
            for (var dataRow : data) {
                var excelRow = sheet.createRow(row++);
                for (int i = 0; i < dataRow.size(); i++) {
                    var cell = excelRow.createCell(i);
                    var value = dataRow.get(i);

                    if (value instanceof Number num) {
                        cell.setCellValue(num.doubleValue());
                    } else if (value != null) {
                        cell.setCellValue(value.toString());
                    }
                    cell.setCellStyle(dataStyle);
                }
            }

            // Custom column widths
            setCustomColumnWidths(sheet, customWidths);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void setDefaultColumnWidths(Sheet sheet, int columnCount) {
        sheet.setColumnWidth(0, 1500);  // STT
        sheet.setColumnWidth(1, 4000);  // Mã
        if (columnCount > 2) {
            sheet.setColumnWidth(2, 12000); // Tên
        }
        for (int i = 3; i < columnCount; i++) {
            sheet.setColumnWidth(i, 4000);
        }
    }

    private void setCustomColumnWidths(Sheet sheet, int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i]);
        }
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        var style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        var font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setFontName("Times New Roman");
        style.setFont(font);
        return style;
    }

    private CellStyle createSubtitleStyle(Workbook workbook) {
        var style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        var font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Times New Roman");
        style.setFont(font);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        var style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        var font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        var style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        var font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        return style;
    }
}
