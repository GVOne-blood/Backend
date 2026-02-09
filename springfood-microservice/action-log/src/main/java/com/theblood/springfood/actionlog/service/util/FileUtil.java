package com.theblood.springfood.actionlog.service.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

@Slf4j
public class FileUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void downloadFile(String fileUrl, String savePath) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (InputStream in = connection.getInputStream(); OutputStream out = Files.newOutputStream(Path.of(savePath))) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            LOG.info("Download completed: " + savePath);
        } catch (IOException e) {
            LOG.error("Cannot download file: " + fileUrl);
            throw new RuntimeException(e);
        }
    }

    public static void cleanUpFile(File file) {
        file.delete();
    }

    public static List<String> readRowAsString(File file) throws Exception {
        List<String> rowDatas = new ArrayList<>();
        InputStream inputStream = Files.newInputStream(file.toPath());
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            rows.next();
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                String rowData = FileUtil.convertRowToJson(currentRow, sheet.getRow(0));
                rowDatas.add(rowData);
            }
        }
        return rowDatas;
    }

    public static String convertRowToJson(Row row, Row headerRow) throws Exception {
        Map<String, Object> rowData = new HashMap<>();

        for (Cell cell : row) {
            String columnName = headerRow.getCell(cell.getColumnIndex()).getStringCellValue();
            Object cellValue = getCellValue(cell);
            rowData.put(columnName, cellValue);
        }

        return objectMapper.writeValueAsString(rowData);
    }

    private static Object getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> cell.getNumericCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> null;
        };
    }
}
