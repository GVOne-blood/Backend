package com.example.demo.AssignmentBR;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogStore {

    private final List<LogEntry> logEntries;

    // Định dạng timestamp trong file log của bạn
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // Biểu thức chính quy (Regex) để parse một dòng log một cách an toàn
    private static final Pattern LOG_PATTERN =
            Pattern.compile("^\\[(.*?)\\] \\[(.*?)\\] \\[(.*?)\\]- (.*)$");

    /**
     * Constructor: Nhận vào đường dẫn file log và thực hiện việc đọc, parse dữ liệu.
     *
     * @param logFilePath Đường dẫn đến file log.
     * @throws IOException Nếu có lỗi khi đọc file.
     */
    public LogStore(Path logFilePath) throws IOException {
        System.out.println("Bắt đầu nạp và phân tích file log. Vui lòng chờ...");
        long startTime = System.currentTimeMillis();

        try (Stream<String> lines = Files.lines(logFilePath, StandardCharsets.UTF_8)) {
            this.logEntries = lines
                    .parallel() // Tận dụng đa luồng để parse file nhanh hơn!
                    .map(LogStore::parseLine)
                    .filter(java.util.Objects::nonNull) // Lọc bỏ các dòng không parse được
                    .collect(Collectors.toList());
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("Đã nạp thành công %d dòng log trong %d ms.%n",
                logEntries.size(), (endTime - startTime));
    }

    /**
     * Phương thức tĩnh để parse một dòng text thành đối tượng LogEntry.
     *
     * @param line Dòng log thô.
     * @return Một đối tượng LogEntry, hoặc null nếu dòng không hợp lệ.
     */
    private static LogEntry parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        Matcher matcher = LOG_PATTERN.matcher(line);
        if (matcher.find()) {
            try {
                LocalDateTime timestamp = LocalDateTime.parse(matcher.group(1), DATE_TIME_FORMATTER);
                String level = matcher.group(2).trim();
                String service = matcher.group(3).trim();
                String message = matcher.group(4).trim();
                return new LogEntry(timestamp, level, service, message);
            } catch (Exception e) {
                // System.err.println("Không thể parse dòng: " + line);
                return null;
            }
        }
        return null;
    }

    /**
     * Tìm kiếm log theo các tiêu chí. Các tiêu chí null hoặc rỗng sẽ được bỏ qua.
     *
     * @param level          Log level (ví dụ: "INFO", "ERROR").
     * @param startTime      Thời gian bắt đầu tìm kiếm (có thể là null).
     * @param endTime        Thời gian kết thúc tìm kiếm (có thể là null).
     * @param messageKeyword Từ khóa để tìm kiếm trong message (tìm kiếm không phân biệt chữ hoa/thường).
     * @return Một List các LogEntry thỏa mãn điều kiện.
     */
    public List<LogEntry> search(String level, LocalDateTime startTime, LocalDateTime endTime, String messageKeyword) {
        Stream<LogEntry> stream = logEntries.stream().parallel(); // Xử lý tìm kiếm song song

        if (level != null && !level.isBlank()) {
            stream = stream.filter(entry -> entry.level().equalsIgnoreCase(level));
        }

        if (startTime != null) {
            stream = stream.filter(entry -> !entry.timestamp().isBefore(startTime));
        }

        if (endTime != null) {
            stream = stream.filter(entry -> !entry.timestamp().isAfter(endTime));
        }

        if (messageKeyword != null && !messageKeyword.isBlank()) {
            String lowerCaseKeyword = messageKeyword.toLowerCase();
            stream = stream.filter(entry -> entry.message().toLowerCase().contains(lowerCaseKeyword));
        }

        return stream.collect(Collectors.toList());
    }

    /**
     * Ghi kết quả tìm kiếm ra file text.
     *
     * @param results    List các LogEntry cần ghi.
     * @param outputPath Đường dẫn file output.
     * @throws IOException Nếu có lỗi khi ghi file.
     */
    public void exportResults(List<LogEntry> results, Path outputPath) throws IOException {
        if (results == null || results.isEmpty()) {
            System.out.println("Không có kết quả nào để xuất.");
            return;
        }

        // Chuyển đổi List<LogEntry> thành List<String> để ghi ra file
        List<String> linesToExport = results.stream()
                .map(entry -> String.format("[%s] [%s] [%s]- %s",
                        entry.timestamp().format(DATE_TIME_FORMATTER),
                        entry.level(),
                        entry.service(),
                        entry.message()))
                .collect(Collectors.toList());

        // Sử dụng Files.write để ghi toàn bộ các dòng vào file, đây là cách làm của NIO
        Files.write(outputPath, linesToExport, StandardCharsets.UTF_8);
    }
}
