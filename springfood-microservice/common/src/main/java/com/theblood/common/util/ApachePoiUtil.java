package com.theblood.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
public class ApachePoiUtil {

    public static class CellUtils {
        public static UUID getCellValueAsUUID(Cell cell) {
            if (cell == null) return null;
            try {
                return UUID.fromString(cell.toString());
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
                return null;
            }
        }

        public static BigDecimal getCellValueAsBigDecimal(Cell cell) {
            if (cell == null) return null;
            try {
                return new BigDecimal(cell.toString());
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
                return null;
            }
        }

        public static Integer getCellValueAsInteger(Cell cell) {
            if (cell == null) {
                return null;
            }

            // Kiểm tra loại Cell để lấy dữ liệu đúng cách
            switch (cell.getCellType()) {
                case NUMERIC:
                    // Excel lưu số là double (ví dụ 10.0), cần ép kiểu về int
                    return (int) cell.getNumericCellValue();

                case STRING:
                    try {
                        String value = cell.getStringCellValue().trim();
                        if (value.isEmpty()) return null;
                        return Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        // Log warning nếu cần
                        return null;
                    }

                case FORMULA:
                    // Trường hợp ô đó là công thức, lấy giá trị đã tính toán
                    try {
                        return (int) cell.getNumericCellValue();
                    } catch (Exception e) {
                        return null;
                    }

                case BLANK:
                default:
                    return null;
            }
        }

        public static LocalDate getCellValueAsLocalDate(Cell cell) {
            if (cell == null) return null;
            try {
                return LocalDate.parse(cell.toString());
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
                return null;
            }
        }
    }

//
//    public static String getCellValueAsString(Cell cell) {
//        if (cell == null) return null;
//        try {
//            cell.getN
//            return cell.getStringCellValue();
//        }
//    }
}
