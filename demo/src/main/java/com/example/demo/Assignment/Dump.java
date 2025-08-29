package com.example.demo.Assignment;

import lombok.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static final Pattern LOG_PATTERN =
            Pattern.compile("^\\[(.*?)\\] \\[(.*?)\\] \\[(.*?)\\]- (.*)$");

    // Số lượng dòng mỗi batch
    private static final int BATCH_SIZE = 1000;

    private Dump() {
        // Lớp tiện ích không nên được khởi tạo
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
                return null;
            }
        }
        return null;
    }

    public static boolean isTimestampInRange(LocalDateTime timestamp, LocalDateTime from, LocalDateTime to) {
        return !timestamp.isBefore(from) && !timestamp.isAfter(to);
    }

    public static boolean searchByLogLevel(String type, Log log) {
        return log.getLevel().equalsIgnoreCase(type);
    }

    public static boolean searchByMessage(String key, Log log) {
        return log.getMessage().toLowerCase().contains(key.toLowerCase());
    }

    // Task xử lý một batch dòng
    static class BatchProcessor implements Callable<Void> {
        private final String[] lines;
        private final int startIndex;
        private final int endIndex;
        private final String logLevel;
        private final String messageKeyword;
        private final LocalDateTime searchFrom;
        private final LocalDateTime searchTo;
        private final ConcurrentLinkedQueue<Log> results;

        public BatchProcessor(String[] lines, int startIndex, int endIndex,
                              String logLevel, String messageKeyword,
                              LocalDateTime searchFrom, LocalDateTime searchTo,
                              ConcurrentLinkedQueue<Log> results) {
            this.lines = lines;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.logLevel = logLevel;
            this.messageKeyword = messageKeyword;
            this.searchFrom = searchFrom;
            this.searchTo = searchTo;
            this.results = results;
        }

        @Override
        public Void call() {
            for (int i = startIndex; i < endIndex && i < lines.length; i++) {
                Log log = parseLine(lines[i]);
                if (log != null && matchesAllCriteria(log)) {
                    results.offer(log);
                }
            }
            return null;
        }

        private boolean matchesAllCriteria(Log log) {
            // Kiểm tra tất cả điều kiện - chỉ trả về true khi thỏa mãn TẤT CẢ
            boolean matchLevel = (logLevel == null || logLevel.isEmpty() ||
                    searchByLogLevel(logLevel, log));

            boolean matchTime = (searchFrom == null || searchTo == null ||
                    isTimestampInRange(log.getTimestamp(), searchFrom, searchTo));

            boolean matchMessage = (messageKeyword == null || messageKeyword.isEmpty() ||
                    searchByMessage(messageKeyword, log));

            return matchLevel && matchTime && matchMessage;
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        // Các tiêu chí tìm kiếm (để null hoặc empty nếu không muốn lọc theo tiêu chí đó)
        String logLevelToSearch = "INFO";  // null hoặc "" để bỏ qua
        String messageKeywordToSearch = "User";  // null hoặc "" để bỏ qua
        LocalDateTime searchFrom = null;//LocalDateTime.of(2023, 11, 25, 9, 0, 0);  // null để bỏ qua
        LocalDateTime searchTo = null; //LocalDateTime.of(2023, 11, 25, 10, 0, 0);   // null để bỏ qua

        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        ConcurrentLinkedQueue<Log> results = new ConcurrentLinkedQueue<>();

        try (BufferedReader reader = Files.newBufferedReader(LOG_FILE_PATH)) {
            // Đọc theo batch để cân bằng giữa bộ nhớ và hiệu năng
            String[] batch = new String[BATCH_SIZE * numThreads];
            int batchIndex = 0;
            String line;
            AtomicLong totalLines = new AtomicLong(0);

            while ((line = reader.readLine()) != null) {
                batch[batchIndex++] = line;
                totalLines.incrementAndGet();

                // Khi batch đầy, xử lý song song
                if (batchIndex >= batch.length) {
                    processBatch(executor, batch, batchIndex, logLevelToSearch,
                            messageKeywordToSearch, searchFrom, searchTo, results);
                    batchIndex = 0;
                }
            }

            // Xử lý batch cuối cùng nếu còn
            if (batchIndex > 0) {
                processBatch(executor, batch, batchIndex, logLevelToSearch,
                        messageKeywordToSearch, searchFrom, searchTo, results);
            }

            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.MINUTES);

            // In kết quả
            System.out.println("=== KẾT QUẢ TÌM KIẾM ===");
            System.out.println("Tiêu chí:");
            if (logLevelToSearch != null && !logLevelToSearch.isEmpty()) {
                System.out.println("- Log Level: " + logLevelToSearch);
            }
            if (searchFrom != null && searchTo != null) {
                System.out.println("- Thời gian: từ " + searchFrom + " đến " + searchTo);
            }
            if (messageKeywordToSearch != null && !messageKeywordToSearch.isEmpty()) {
                System.out.println("- Message chứa: " + messageKeywordToSearch);
            }
            System.out.println("\nTìm thấy " + results.size() + " kết quả:");
            System.out.println("------------------------");

            //   results.forEach(System.out::println);

            System.out.println("\nTổng số dòng đã xử lý: " + totalLines.get());

        } catch (IOException | InterruptedException e) {
            System.err.println("Lỗi: " + e.getMessage());
        }

        long stop = System.currentTimeMillis();
        System.out.println("\nTotal time: " + (stop - start) + " ms");
    }

    private static void processBatch(ExecutorService executor, String[] batch, int batchSize,
                                     String logLevel, String messageKeyword,
                                     LocalDateTime searchFrom, LocalDateTime searchTo,
                                     ConcurrentLinkedQueue<Log> results) {
        int numThreads = Runtime.getRuntime().availableProcessors() + 20;
        int linesPerThread = batchSize / numThreads;
        CompletableFuture<?>[] futures = new CompletableFuture[numThreads];

        for (int i = 0; i < numThreads; i++) {
            int startIdx = i * linesPerThread;
            int endIdx = (i == numThreads - 1) ? batchSize : (i + 1) * linesPerThread;

            futures[i] = CompletableFuture.runAsync(
                    () -> new BatchProcessor(batch, startIdx, endIdx, logLevel,
                            messageKeyword, searchFrom, searchTo, results).call(),
                    executor
            );
        }

        // Đợi tất cả threads trong batch hoàn thành
        CompletableFuture.allOf(futures).join();
    }
}