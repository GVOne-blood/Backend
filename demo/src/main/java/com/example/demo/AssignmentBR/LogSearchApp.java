package com.example.demo.AssignmentBR;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class LogSearchApp {

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // --- Bước 1: Khởi tạo và nạp dữ liệu ---
        Path logFilePath = Paths.get("src/main/java/com/example/demo/Assignment/log.txt"); // << THAY ĐỔI ĐƯỜNG DẪN FILE LOG CỦA BẠN Ở ĐÂY
        LogStore logStore;
        try {
            logStore = new LogStore(logFilePath);
        } catch (IOException e) {
            System.err.println("Lỗi nghiêm trọng: Không thể đọc file log tại " + logFilePath);
            e.printStackTrace();
            return;
        }

        // --- Bước 2: Vòng lặp tìm kiếm ---
        while (true) {
            System.out.println("\n--- TÌM KIẾM LOG --- (Nhấn Enter để bỏ qua tiêu chí)");

            String level = getInput("Nhập Log Level (INFO, WARN, ERROR): ");
            LocalDateTime startTime = getDateTimeInput("Nhập thời gian bắt đầu (yyyy-MM-dd HH:mm:ss): ");
            LocalDateTime endTime = getDateTimeInput("Nhập thời gian kết thúc (yyyy-MM-dd HH:mm:ss): ");
            String keyword = getInput("Nhập từ khóa trong message: ");

            // --- Bước 3: Thực hiện tìm kiếm ---
            System.out.println("Đang tìm kiếm...");
            long searchStart = System.currentTimeMillis();
            List<LogEntry> results = logStore.search(level, startTime, endTime, keyword);
            long searchEnd = System.currentTimeMillis();

            System.out.printf("Tìm thấy %d kết quả trong %d ms.%n", results.size(), (searchEnd - searchStart));

            // In ra 10 kết quả đầu tiên để xem trước
            results.stream().limit(10).forEach(System.out::println);
            if (results.size() > 10) {
                System.out.printf("... và %d kết quả khác.%n", results.size() - 10);
            }

            // --- Bước 4: Tùy chọn xuất file ---
            if (!results.isEmpty()) {
                String exportChoice = getInput("Bạn có muốn xuất kết quả ra file? (y/n): ");
                if (exportChoice.equalsIgnoreCase("y")) {
                    String fileName = getInput("Nhập tên file output (ví dụ: ket_qua.txt): ");
                    try {
                        logStore.exportResults(results, Paths.get(fileName));
                        System.out.println("Đã xuất kết quả thành công ra file: " + fileName);
                    } catch (IOException e) {
                        System.err.println("Lỗi khi xuất file: " + e.getMessage());
                    }
                }
            }

            // --- Bước 5: Tìm kiếm tiếp hay thoát ---
            String continueChoice = getInput("Tìm kiếm lần nữa? (y/n): ");
            if (!continueChoice.equalsIgnoreCase("y")) {
                break;
            }
        }

        scanner.close();
        System.out.println("Chương trình kết thúc.");
    }

    private static String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static LocalDateTime getDateTimeInput(String prompt) {
        while (true) {
            String input = getInput(prompt);
            if (input.isBlank()) {
                return null;
            }
            try {
                // Thêm ".000" nếu người dùng không nhập phần giây
                if (input.length() == 19) {
                    return LocalDateTime.parse(input, INPUT_FORMATTER);
                }
                return LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            } catch (DateTimeParseException e) {
                System.err.println("Định dạng thời gian không hợp lệ. Vui lòng thử lại.");
            }
        }
    }
}