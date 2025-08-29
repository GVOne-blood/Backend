package com.example.demo.AssignmentBR;

import lombok.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
class Log {
    public LocalDateTime timestamp;
    public String level;
    public String service;
    public String message;
}


public final class Dump {
    static final Path LOG_FILE_PATH = Paths.get("src/main/java/com/example/demo/Assignment/log.txt");
    static final Path output = Paths.get("src/main/java/com/example/demo/Assignment/res.txt");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Pattern LOG_PATTERN = Pattern.compile("^\\[(.*?)\\] \\[(.*?)\\] \\[(.*?)\\]- (.*)$");
    private static final int parts = 18;
    static AtomicInteger cnt = new AtomicInteger(0);
    static BlockingQueue<String> lineQueue = new LinkedBlockingQueue<>(10000);
    // Dấu hiệu đặc biệt để báo cho Consumer biết đã hết dữ liệu
    private static final String POISON_PILL = "::END_OF_STREAM::";

    private Dump() {
    }

    public static Log parseLine(String line) {
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
                return new Log(timestamp, level, service, message);
            } catch (Exception e) {
                // Bỏ qua các dòng không parse được một cách lặng lẽ
                return null;
            }
        }
        return null;
    }

    public static boolean isTimestampInRange(LocalDateTime timestamp, LocalDateTime from, LocalDateTime to) {
        if (to == null) return true;
        return !timestamp.isBefore(from) && !timestamp.isAfter(to);
    }

    public static boolean searchByLogLevel(String type, Log log) {
        if (type == null) return true;
        return log.getLevel().equalsIgnoreCase(type);
    }

    public static boolean searchByMessage(String key, Log log) {
        if (key == null) return true;
        return log.getMessage().contains(key);
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

//        String logLevelToSearch = "INFO";
//        String messageKeywordToSearch = "User";
//        LocalDateTime searchFrom = LocalDateTime.of(2023, 11, 25, 9, 0, 0);
//        LocalDateTime searchTo = LocalDateTime.of(2023, 11, 25, 10, 0, 0);

        String logLevelToSearch = null;
        String messageKeywordToSearch = null;
        LocalDateTime searchFrom = null;
        LocalDateTime searchTo = null;

        // 2. Tạo và khởi chạy luồng Producer (đọc file)
        Thread producer = new Thread(() -> {
            try (Stream<String> lines = Files.lines(LOG_FILE_PATH)) {
                lines.forEach(line -> {
                    try {
                        lineQueue.put(line); // Đưa dòng vào queue, sẽ block nếu queue đầy
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Khôi phục trạng thái ngắt
                    }
                });
            } catch (IOException e) {
                System.err.println("Lỗi khi đọc file: " + e.getMessage());
            } finally {
                for (int i = 0; i < parts; i++) {
                    try {
                        lineQueue.put(POISON_PILL);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        List<Thread> consumerThreads = new ArrayList<>();
        for (int i = 0; i < parts; i++) {
            Thread consumer = new Thread(() -> {
                while (true) {
                    try {
                        String line = lineQueue.take();
                        if (POISON_PILL.equals(line)) {
                            break;
                        }
                        Log log = parseLine(line);
                        if (log != null) {
                            if (searchByLogLevel(logLevelToSearch, log) &&
                                    isTimestampInRange(log.getTimestamp(), searchFrom, searchTo) &&
                                    searchByMessage(messageKeywordToSearch, log)) {
                                //Files.write(output, log.toString());
                                Files.writeString(output, log.toString(), StandardOpenOption.APPEND);
                                cnt.incrementAndGet();
                            }

                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            });
            // Khởi tạo luồng ghi file

            consumerThreads.add(consumer); // Thêm luồng vào danh sách
            consumer.start(); // Khởi chạy luồng
        }
        long startRead = System.currentTimeMillis();


        producer.start();
        producer.join();
        long stopRead = System.currentTimeMillis() - startRead;
        System.out.println("Read file take : " + stopRead + " ms ");
        for (Thread consumer : consumerThreads) {
            consumer.join();
        }


        long stop = System.currentTimeMillis();
        System.out.println("Total result : " + cnt);
        System.out.println("Total time : " + (stop - start) + " ms");
    }
}