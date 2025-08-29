package com.example.demo.AssignmentBRTime;

import lombok.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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

// Class để lưu thống kê thời gian
class TimeStats {
    private final AtomicLong totalParseTime = new AtomicLong(0);
    private final AtomicLong totalLevelCheckTime = new AtomicLong(0);
    private final AtomicLong totalTimeCheckTime = new AtomicLong(0);
    private final AtomicLong totalMessageCheckTime = new AtomicLong(0);
    private final AtomicLong totalWriteTime = new AtomicLong(0);
    private final AtomicLong totalQueueTakeTime = new AtomicLong(0);
    private final AtomicLong totalQueuePutTime = new AtomicLong(0);
    private final AtomicInteger parseCount = new AtomicInteger(0);
    private final AtomicInteger levelCheckCount = new AtomicInteger(0);
    private final AtomicInteger timeCheckCount = new AtomicInteger(0);
    private final AtomicInteger messageCheckCount = new AtomicInteger(0);

    // Lưu thời gian thực tế cho mỗi thread
    private final ConcurrentHashMap<Long, Long> threadStartTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> threadEndTimes = new ConcurrentHashMap<>();

    public void recordThreadStart(long threadId) {
        threadStartTimes.put(threadId, System.nanoTime());
    }

    public void recordThreadEnd(long threadId) {
        threadEndTimes.put(threadId, System.nanoTime());
    }

    public void addParseTime(long nanos) {
        totalParseTime.addAndGet(nanos);
        parseCount.incrementAndGet();
    }

    public void addLevelCheckTime(long nanos) {
        totalLevelCheckTime.addAndGet(nanos);
        levelCheckCount.incrementAndGet();
    }

    public void addTimeCheckTime(long nanos) {
        totalTimeCheckTime.addAndGet(nanos);
        timeCheckCount.incrementAndGet();
    }

    public void addMessageCheckTime(long nanos) {
        totalMessageCheckTime.addAndGet(nanos);
        messageCheckCount.incrementAndGet();
    }

    public void addWriteTime(long nanos) {
        totalWriteTime.addAndGet(nanos);
    }

    public void addQueueTakeTime(long nanos) {
        totalQueueTakeTime.addAndGet(nanos);
    }

    public void addQueuePutTime(long nanos) {
        totalQueuePutTime.addAndGet(nanos);
    }

    public void printStats() {
        System.out.println("\n=== THỐNG KÊ THỜI GIAN THỰC THI ===");
        System.out.println("1. Stream API & Queue Operations:");
        System.out.printf("   - Queue Put (Files.lines().forEach): %.2f ms (avg: %.3f μs/line)\n",
                totalQueuePutTime.get() / 1_000_000.0,
                parseCount.get() > 0 ? totalQueuePutTime.get() / 1000.0 / parseCount.get() : 0);
        System.out.printf("   - Queue Take (tổng cộng): %.2f ms\n", totalQueueTakeTime.get() / 1_000_000.0);

        System.out.println("\n2. Parse Operations:");
        System.out.printf("   - parseLine(): %.2f ms cho %d dòng (avg: %.3f μs/line)\n",
                totalParseTime.get() / 1_000_000.0, parseCount.get(),
                parseCount.get() > 0 ? totalParseTime.get() / 1000.0 / parseCount.get() : 0);

        System.out.println("\n3. Filter Operations:");
        System.out.printf("   - searchByLogLevel(): %.2f ms cho %d checks (avg: %.3f μs/check)\n",
                totalLevelCheckTime.get() / 1_000_000.0, levelCheckCount.get(),
                levelCheckCount.get() > 0 ? totalLevelCheckTime.get() / 1000.0 / levelCheckCount.get() : 0);
        System.out.printf("   - isTimestampInRange(): %.2f ms cho %d checks (avg: %.3f μs/check)\n",
                totalTimeCheckTime.get() / 1_000_000.0, timeCheckCount.get(),
                timeCheckCount.get() > 0 ? totalTimeCheckTime.get() / 1000.0 / timeCheckCount.get() : 0);
        System.out.printf("   - searchByMessage(): %.2f ms cho %d checks (avg: %.3f μs/check)\n",
                totalMessageCheckTime.get() / 1_000_000.0, messageCheckCount.get(),
                messageCheckCount.get() > 0 ? totalMessageCheckTime.get() / 1000.0 / messageCheckCount.get() : 0);

        System.out.println("\n4. I/O Operations:");
        System.out.printf("   - File Write (tổng cộng): %.2f ms\n", totalWriteTime.get() / 1_000_000.0);

        // Tính thời gian thực tế (wall clock time) cho consumer threads
        if (!threadStartTimes.isEmpty() && !threadEndTimes.isEmpty()) {
            long earliestStart = threadStartTimes.values().stream().min(Long::compareTo).orElse(0L);
            long latestEnd = threadEndTimes.values().stream().max(Long::compareTo).orElse(0L);
            double actualProcessingTime = (latestEnd - earliestStart) / 1_000_000.0;

            System.out.println("\n5. Thời gian thực tế (Wall Clock Time):");
            System.out.printf("   - Thời gian xử lý thực tế của consumers: %.2f ms\n", actualProcessingTime);
            System.out.printf("   - Số lượng consumer threads: %d\n", threadStartTimes.size());

            // Tính CPU time tổng cộng
            long totalCpuTime = totalParseTime.get() + totalLevelCheckTime.get() +
                    totalTimeCheckTime.get() + totalMessageCheckTime.get();
            System.out.printf("   - Tổng CPU time (không tính I/O và queue): %.2f ms\n",
                    totalCpuTime / 1_000_000.0);

            // Tính hiệu suất song song
            if (actualProcessingTime > 0) {
                double parallelEfficiency = (totalCpuTime / 1_000_000.0) / actualProcessingTime / threadStartTimes.size() * 100;
                System.out.printf("   - Hiệu suất song song: %.1f%%\n", parallelEfficiency);
            }
        }

        System.out.println("\n6. Phân tích chi tiết:");
        long totalTime = totalParseTime.get() + totalLevelCheckTime.get() +
                totalTimeCheckTime.get() + totalMessageCheckTime.get() + totalWriteTime.get();
        if (totalTime > 0) {
            System.out.printf("   - Parse: %.1f%%\n", totalParseTime.get() * 100.0 / totalTime);
            System.out.printf("   - Level Check: %.1f%%\n", totalLevelCheckTime.get() * 100.0 / totalTime);
            System.out.printf("   - Time Check: %.1f%%\n", totalTimeCheckTime.get() * 100.0 / totalTime);
            System.out.printf("   - Message Check: %.1f%%\n", totalMessageCheckTime.get() * 100.0 / totalTime);
            System.out.printf("   - File Write: %.1f%%\n", totalWriteTime.get() * 100.0 / totalTime);
        }
    }
}

public final class Dump {
    static final Path LOG_FILE_PATH = Paths.get("src/main/java/com/example/demo/Assignment/log.txt");
    static final Path output = Paths.get("src/main/java/com/example/demo/Assignment/res.txt");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Pattern LOG_PATTERN = Pattern.compile("^\\[(.*?)\\] \\[(.*?)\\] \\[(.*?)\\]- (.*)$");
    private static final int CONSUMER_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int BATCH_SIZE = 100; // Batch size for writing
    static AtomicInteger cnt = new AtomicInteger(0);
    static BlockingQueue<String> lineQueue = new LinkedBlockingQueue<>(10000);
    static BlockingQueue<String> writeQueue = new LinkedBlockingQueue<>(10000);
    private static final String POISON_PILL = "::END_OF_STREAM::";

    // Thống kê thời gian
    private static final TimeStats timeStats = new TimeStats();

    private Dump() {
    }

    public static Log parseLine(String line) {
        long startTime = System.nanoTime();
        try {
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
        } finally {
            timeStats.addParseTime(System.nanoTime() - startTime);
        }
    }

    public static boolean isTimestampInRange(LocalDateTime timestamp, LocalDateTime from, LocalDateTime to) {
        long startTime = System.nanoTime();
        try {
            if (to == null) return true;
            return !timestamp.isBefore(from) && !timestamp.isAfter(to);
        } finally {
            timeStats.addTimeCheckTime(System.nanoTime() - startTime);
        }
    }

    public static boolean searchByLogLevel(String type, Log log) {
        long startTime = System.nanoTime();
        try {
            if (type == null) return true;
            return log.getLevel().equalsIgnoreCase(type);
        } finally {
            timeStats.addLevelCheckTime(System.nanoTime() - startTime);
        }
    }

    public static boolean searchByMessage(String key, Log log) {
        long startTime = System.nanoTime();
        try {
            if (key == null) return true;
            return log.getMessage().contains(key);
        } finally {
            timeStats.addMessageCheckTime(System.nanoTime() - startTime);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        // Test với điều kiện cụ thể
//        String logLevelToSearch = "INFO";
//        String messageKeywordToSearch = "User";
//        LocalDateTime searchFrom = LocalDateTime.of(2023, 11, 25, 9, 0, 0);
//        LocalDateTime searchTo = LocalDateTime.of(2023, 11, 25, 10, 0, 0);

        // Uncomment để test worst case
        String logLevelToSearch = null;
        String messageKeywordToSearch = null;
        LocalDateTime searchFrom = null;
        LocalDateTime searchTo = null;

        // Đếm số dòng đã đọc
        AtomicLong linesRead = new AtomicLong(0);

        // Clear output file
        try {
            Files.writeString(output, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Cannot clear output file: " + e.getMessage());
        }

        // 1. Writer Thread - Chuyên ghi file theo batch
        Thread writerThread = new Thread(() -> {
            try (BufferedWriter writer = Files.newBufferedWriter(output, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                List<String> batch = new ArrayList<>(BATCH_SIZE);
                boolean done = false;
                long totalWriteTime = 0;

                while (!done) {
                    String line = writeQueue.poll(50, TimeUnit.MILLISECONDS);

                    if (line != null) {
                        if (POISON_PILL.equals(line)) {
                            done = true;
                        } else {
                            batch.add(line);
                        }
                    }

                    // Ghi batch khi đầy hoặc timeout
                    if (!batch.isEmpty() && (batch.size() >= BATCH_SIZE || done || line == null)) {
                        long writeStart = System.nanoTime();
                        for (String logLine : batch) {
                            writer.write(logLine);
                            writer.newLine();
                        }
                        writer.flush();
                        totalWriteTime += (System.nanoTime() - writeStart);
                        batch.clear();
                    }
                }
                timeStats.addWriteTime(totalWriteTime);
            } catch (IOException | InterruptedException e) {
                System.err.println("Writer error: " + e.getMessage());
            }
        });

        // 2. Producer Thread - Đọc file
        Thread producer = new Thread(() -> {
            long streamStartTime = System.currentTimeMillis();
            try (Stream<String> lines = Files.lines(LOG_FILE_PATH)) {
                lines.forEach(line -> {
                    long putStartTime = System.nanoTime();
                    try {
                        lineQueue.put(line);
                        linesRead.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        timeStats.addQueuePutTime(System.nanoTime() - putStartTime);
                    }
                });
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
            } finally {
                System.out.printf("Stream forEach() time: %.2f ms for %d lines\n",
                        (System.currentTimeMillis() - streamStartTime) / 1.0, linesRead.get());
                // Send poison pills for all consumers
                for (int i = 0; i < CONSUMER_THREADS; i++) {
                    try {
                        lineQueue.put(POISON_PILL);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });

        // 3. Consumer Threads - Parse và filter
        List<Thread> consumerThreads = new ArrayList<>();
        CountDownLatch consumersFinished = new CountDownLatch(CONSUMER_THREADS);

        for (int i = 0; i < CONSUMER_THREADS; i++) {
            final int threadIndex = i;
            Thread consumer = new Thread(() -> {
                long threadId = Thread.currentThread().getId();
                timeStats.recordThreadStart(threadId);

                try {
                    while (true) {
                        long takeStartTime = System.nanoTime();
                        String line = lineQueue.take();
                        timeStats.addQueueTakeTime(System.nanoTime() - takeStartTime);

                        if (POISON_PILL.equals(line)) {
                            break;
                        }

                        Log log = parseLine(line);
                        if (log != null) {
                            if (searchByLogLevel(logLevelToSearch, log) &&
                                    isTimestampInRange(log.getTimestamp(), searchFrom, searchTo) &&
                                    searchByMessage(messageKeywordToSearch, log)) {

                                writeQueue.put(log.toString());
                                cnt.incrementAndGet();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    timeStats.recordThreadEnd(threadId);
                    consumersFinished.countDown();
                }
            });
            consumer.setName("Consumer-" + i);
            consumerThreads.add(consumer);
            consumer.start();
        }

        // Start all threads
        writerThread.start();
        long startRead = System.currentTimeMillis();
        producer.start();

        // Wait for producer
        producer.join();
        long stopRead = System.currentTimeMillis() - startRead;
        System.out.println("Producer thread time: " + stopRead + " ms");

        // Wait for all consumers
        consumersFinished.await();

        // Send poison pill to writer
        writeQueue.put(POISON_PILL);

        // Wait for writer
        writerThread.join();

        long stop = System.currentTimeMillis();
        System.out.println("\n=== KẾT QUẢ TỔNG QUAN ===");
        System.out.println("Total results: " + cnt);
        System.out.println("Total time: " + (stop - start) + " ms");
        System.out.println("Total lines read: " + linesRead.get());
        System.out.println("Consumer threads: " + CONSUMER_THREADS);

        // In thống kê chi tiết
        timeStats.printStats();
    }
}