package com.theblood.springfood.media.excel.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

/**
 * Utility class for Apache POI Excel operations.
 * Provides helper methods for reading and writing cell values.
 */
@Slf4j
public final class ExcelUtils {

    private ExcelUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Cell reading utilities
     */
    public static class CellReader {

        private CellReader() {}

        /**
         * Get cell value as String
         */
        public static String asString(Cell cell) {
            if (cell == null) return null;
            
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue().trim();
                case NUMERIC -> {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        yield cell.getLocalDateTimeCellValue().toString();
                    }
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value)) {
                        yield String.valueOf((long) value);
                    }
                    yield String.valueOf(value);
                }
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                case FORMULA -> {
                    try {
                        yield cell.getStringCellValue();
                    } catch (Exception e) {
                        yield String.valueOf(cell.getNumericCellValue());
                    }
                }
                case BLANK -> null;
                default -> null;
            };
        }

        /**
         * Get cell value as Integer
         */
        public static Integer asInteger(Cell cell) {
            if (cell == null) return null;

            return switch (cell.getCellType()) {
                case NUMERIC -> (int) cell.getNumericCellValue();
                case STRING -> {
                    try {
                        String value = cell.getStringCellValue().trim();
                        if (value.isEmpty()) yield null;
                        yield Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        log.warn("Cannot parse Integer from cell: {}", cell.getStringCellValue());
                        yield null;
                    }
                }
                case FORMULA -> {
                    try {
                        yield (int) cell.getNumericCellValue();
                    } catch (Exception e) {
                        yield null;
                    }
                }
                default -> null;
            };
        }

        /**
         * Get cell value as Long
         */
        public static Long asLong(Cell cell) {
            if (cell == null) return null;

            return switch (cell.getCellType()) {
                case NUMERIC -> (long) cell.getNumericCellValue();
                case STRING -> {
                    try {
                        String value = cell.getStringCellValue().trim();
                        if (value.isEmpty()) yield null;
                        yield Long.parseLong(value);
                    } catch (NumberFormatException e) {
                        log.warn("Cannot parse Long from cell: {}", cell.getStringCellValue());
                        yield null;
                    }
                }
                case FORMULA -> {
                    try {
                        yield (long) cell.getNumericCellValue();
                    } catch (Exception e) {
                        yield null;
                    }
                }
                default -> null;
            };
        }

        /**
         * Get cell value as Double
         */
        public static Double asDouble(Cell cell) {
            if (cell == null) return null;

            return switch (cell.getCellType()) {
                case NUMERIC -> cell.getNumericCellValue();
                case STRING -> {
                    try {
                        String value = cell.getStringCellValue().trim();
                        if (value.isEmpty()) yield null;
                        yield Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        log.warn("Cannot parse Double from cell: {}", cell.getStringCellValue());
                        yield null;
                    }
                }
                case FORMULA -> {
                    try {
                        yield cell.getNumericCellValue();
                    } catch (Exception e) {
                        yield null;
                    }
                }
                default -> null;
            };
        }

        /**
         * Get cell value as BigDecimal
         */
        public static BigDecimal asBigDecimal(Cell cell) {
            if (cell == null) return null;
            
            try {
                Double value = asDouble(cell);
                return value != null ? BigDecimal.valueOf(value) : null;
            } catch (Exception e) {
                log.warn("Cannot parse BigDecimal from cell", e);
                return null;
            }
        }

        /**
         * Get cell value as UUID
         */
        public static UUID asUUID(Cell cell) {
            if (cell == null) return null;
            
            try {
                String value = asString(cell);
                return value != null ? UUID.fromString(value) : null;
            } catch (IllegalArgumentException e) {
                log.warn("Cannot parse UUID from cell: {}", asString(cell));
                return null;
            }
        }

        /**
         * Get cell value as LocalDate
         */
        public static LocalDate asLocalDate(Cell cell) {
            if (cell == null) return null;

            return switch (cell.getCellType()) {
                case NUMERIC -> {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        yield cell.getLocalDateTimeCellValue().toLocalDate();
                    }
                    yield null;
                }
                case STRING -> {
                    try {
                        String value = cell.getStringCellValue().trim();
                        if (value.isEmpty()) yield null;
                        yield LocalDate.parse(value);
                    } catch (Exception e) {
                        log.warn("Cannot parse LocalDate from cell: {}", cell.getStringCellValue());
                        yield null;
                    }
                }
                default -> null;
            };
        }

        /**
         * Get cell value as LocalDateTime
         */
        public static LocalDateTime asLocalDateTime(Cell cell) {
            if (cell == null) return null;

            return switch (cell.getCellType()) {
                case NUMERIC -> {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        yield cell.getLocalDateTimeCellValue();
                    }
                    yield null;
                }
                case STRING -> {
                    try {
                        String value = cell.getStringCellValue().trim();
                        if (value.isEmpty()) yield null;
                        yield LocalDateTime.parse(value);
                    } catch (Exception e) {
                        log.warn("Cannot parse LocalDateTime from cell: {}", cell.getStringCellValue());
                        yield null;
                    }
                }
                default -> null;
            };
        }

        /**
         * Get cell value as Boolean
         */
        public static Boolean asBoolean(Cell cell) {
            if (cell == null) return null;

            return switch (cell.getCellType()) {
                case BOOLEAN -> cell.getBooleanCellValue();
                case NUMERIC -> cell.getNumericCellValue() != 0;
                case STRING -> {
                    String value = cell.getStringCellValue().trim().toLowerCase();
                    yield "true".equals(value) || "1".equals(value) || "yes".equals(value) || "có".equals(value);
                }
                default -> null;
            };
        }
    }

    /**
     * Cell writing utilities
     */
    public static class CellWriter {

        private CellWriter() {}

        /**
         * Set cell value with auto type detection
         */
        public static void setValue(Cell cell, Object value) {
            if (cell == null || value == null) {
                if (cell != null) cell.setBlank();
                return;
            }

            if (value instanceof String s) {
                cell.setCellValue(s);
            } else if (value instanceof Number n) {
                cell.setCellValue(n.doubleValue());
            } else if (value instanceof Boolean b) {
                cell.setCellValue(b);
            } else if (value instanceof LocalDate ld) {
                cell.setCellValue(ld);
            } else if (value instanceof LocalDateTime ldt) {
                cell.setCellValue(ldt);
            } else if (value instanceof Date d) {
                cell.setCellValue(d);
            } else if (value instanceof UUID uuid) {
                cell.setCellValue(uuid.toString());
            } else {
                cell.setCellValue(value.toString());
            }
        }

        /**
         * Set cell value as String
         */
        public static void setString(Cell cell, String value) {
            if (cell == null) return;
            if (value == null) {
                cell.setBlank();
            } else {
                cell.setCellValue(value);
            }
        }

        /**
         * Set cell value as Number
         */
        public static void setNumber(Cell cell, Number value) {
            if (cell == null) return;
            if (value == null) {
                cell.setBlank();
            } else {
                cell.setCellValue(value.doubleValue());
            }
        }

        /**
         * Set cell value as Date
         */
        public static void setDate(Cell cell, LocalDate value) {
            if (cell == null) return;
            if (value == null) {
                cell.setBlank();
            } else {
                cell.setCellValue(value);
            }
        }

        /**
         * Set cell value as DateTime
         */
        public static void setDateTime(Cell cell, LocalDateTime value) {
            if (cell == null) return;
            if (value == null) {
                cell.setBlank();
            } else {
                cell.setCellValue(value);
            }
        }
    }

    /**
     * Row utilities
     */
    public static class RowUtils {

        private RowUtils() {}

        /**
         * Get or create cell at index
         */
        public static Cell getOrCreateCell(Row row, int cellIndex) {
            Cell cell = row.getCell(cellIndex);
            return cell != null ? cell : row.createCell(cellIndex);
        }

        /**
         * Check if row is empty
         */
        public static boolean isRowEmpty(Row row) {
            if (row == null) return true;
            
            for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    String value = CellReader.asString(cell);
                    if (value != null && !value.isEmpty()) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * Style utilities
     */
    public static class StyleUtils {

        private StyleUtils() {}

        /**
         * Create header style
         */
        public static CellStyle createHeaderStyle(Workbook workbook) {
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setAlignment(HorizontalAlignment.CENTER);
            return style;
        }

        /**
         * Create date style
         */
        public static CellStyle createDateStyle(Workbook workbook, String pattern) {
            CellStyle style = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            style.setDataFormat(createHelper.createDataFormat().getFormat(pattern));
            return style;
        }

        /**
         * Create currency style
         */
        public static CellStyle createCurrencyStyle(Workbook workbook) {
            CellStyle style = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            style.setDataFormat(createHelper.createDataFormat().getFormat("#,##0"));
            return style;
        }
    }
}
