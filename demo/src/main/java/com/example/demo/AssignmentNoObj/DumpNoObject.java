package com.example.demo.AssignmentNoObj;

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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class DumpNoObject {
    static final Path LOG_FILE_PATH = Paths.get("src/main/java/com/example/demo/Assignment/log.txt");
    static final Path output = Paths.get("src/main/java/com/example/demo/Assignment/res_no_obj.txt");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // Pattern để extract các phần của log line
    private static final Pattern LOG_PATTERN = Pattern.compile("^\\[(.*?)\\] \\[(.*?)\\] \\[(.*?)\\]- (.*)$");
    // Pattern riêng cho từng phần để search nhanh hơn
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("\\[(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\]");
    private static final Pattern LEVEL_PATTERN = Pattern.compile("\\] \\[([A-Z]+)\\] \\[");

    private static final int CONSUMER_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int BATCH_SIZE = 100;
    static AtomicInteger cnt = new AtomicInteger(0);
    static BlockingQueue<String> lineQueue = new LinkedBlockingQueue<>(10000);
    static BlockingQueue<String> writeQueue = new LinkedBlockingQueue<>(10000);
    private static final String POISON_PILL = "::END_OF_STREAM::";

    private DumpNoObject() {
    }

    // Kiểm tra log level trực tiếp trên string
    public static boolean checkLogLevel(String line, String targetLevel) {
        if (targetLevel == null) return true;

        // Tìm log level trong string format: ] [LEVEL] [
        Matcher matcher = LEVEL_PATTERN.matcher(line);
        if (matcher.find()) {
            return matcher.group(1).equalsIgnoreCase(targetLevel);
        }
        return false;
    }

    // Kiểm tra timestamp range trực tiếp trên string
    public static boolean checkTimestamp(String line, LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) return true;

        // Extract timestamp từ đầu line
        Matcher matcher = TIMESTAMP_PATTERN.matcher(line);
        if (matcher.find()) {
            try {
                LocalDateTime timestamp = LocalDateTime.parse(matcher.group(1), DATE_TIME_FORMATTER);
                return !timestamp.isBefore(from) && !timestamp.isAfter(to);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    // Kiểm tra message trực tiếp trên string
    public static boolean checkMessage(String line, String keyword) {
        if (keyword == null) return true;

        // Message bắt đầu sau "- " cuối cùng
        int messageStart = line.lastIndexOf("- ");
        if (messageStart != -1 && messageStart + 2 < line.length()) {
            String message = line.substring(messageStart + 2);
            return message.toLowerCase().contains(keyword.toLowerCase());
        }
        return false;
    }

    // Validate format của log line
    public static boolean isValidLogLine(String line) {
        if (line == null || line.trim().isEmpty()) return false;
        return LOG_PATTERN.matcher(line).matches();
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        // Test với điều kiện cụ thể
        String logLevelToSearch = "INFO";
        String messageKeywordToSearch = "User";
        LocalDateTime searchFrom = LocalDateTime.of(2023, 11, 25, 9, 0, 0);
        LocalDateTime searchTo = LocalDateTime.of(2023, 11, 25, 10, 0, 0);

        // Uncomment để test worst case
//        String logLevelToSearch = null;
//        String messageKeywordToSearch = null;
//        LocalDateTime searchFrom = null;
//        LocalDateTime searchTo = null;

        // Đếm số dòng đã đọc và xử lý
        AtomicLong linesRead = new AtomicLong(0);
        AtomicLong validLines = new AtomicLong(0);

        // Clear output file
        try {
            Files.writeString(output, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Cannot clear output file: " + e.getMessage());
        }

        // 1. Writer Thread - Ghi file theo batch
        Thread writerThread = new Thread(() -> {
            try (BufferedWriter writer = Files.newBufferedWriter(output, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                List<String> batch = new ArrayList<>(BATCH_SIZE);
                boolean done = false;

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
                        for (String logLine : batch) {
                            writer.write(logLine);
                            writer.newLine();
                        }
                        writer.flush();
                        batch.clear();
                    }
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Writer error: " + e.getMessage());
            }
        });

        // 2. Producer Thread - Đọc file
        Thread producer = new Thread(() -> {
            long streamStartTime = System.currentTimeMillis();
            try (Stream<String> lines = Files.lines(LOG_FILE_PATH)) {
                lines.forEach(line -> {
                    try {
                        lineQueue.put(line);
                        linesRead.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
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

        // 3. Consumer Threads - Filter trực tiếp trên string
        List<Thread> consumerThreads = new ArrayList<>();
        CountDownLatch consumersFinished = new CountDownLatch(CONSUMER_THREADS);

        for (int i = 0; i < CONSUMER_THREADS; i++) {
            Thread consumer = new Thread(() -> {
                long processStartTime = System.nanoTime();
                int localMatches = 0;
                int localProcessed = 0;

                try {
                    while (true) {
                        String line = lineQueue.take();

                        if (POISON_PILL.equals(line)) {
                            break;
                        }

                        localProcessed++;

                        // Validate format trước
                        if (!isValidLogLine(line)) {
                            continue;
                        }

                        validLines.incrementAndGet();

                        // Check các điều kiện trực tiếp trên string
                        if (checkLogLevel(line, logLevelToSearch) &&
                                checkTimestamp(line, searchFrom, searchTo) &&
                                checkMessage(line, messageKeywordToSearch)) {

                            writeQueue.put(line);
                            localMatches++;
                            cnt.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    long processTime = System.nanoTime() - processStartTime;
                    System.out.printf("Consumer thread finished - processed: %d, matches: %d, time: %.2f ms\n",
                            localProcessed, localMatches, processTime / 1_000_000.0);
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
        System.out.println("\nProducer thread time: " + stopRead + " ms");

        // Wait for all consumers
        consumersFinished.await();

        // Send poison pill to writer
        writeQueue.put(POISON_PILL);

        // Wait for writer
        writerThread.join();

        long stop = System.currentTimeMillis();
        System.out.println("\n=== KẾT QUẢ TỔNG QUAN ===");
        System.out.println("Total results: " + cnt.get());
        System.out.println("Total time: " + (stop - start) + " ms");
        System.out.println("Total lines read: " + linesRead.get());
        System.out.println("Valid log lines: " + validLines.get());
        System.out.println("Consumer threads: " + CONSUMER_THREADS);
        System.out.println("\n=== HIỆU SUẤT ===");
        System.out.printf("Processing speed: %.0f lines/second\n",
                linesRead.get() * 1000.0 / (stop - start));
        System.out.printf("Average time per line: %.3f μs\n",
                (stop - start) * 1000.0 / linesRead.get());
    }
}
