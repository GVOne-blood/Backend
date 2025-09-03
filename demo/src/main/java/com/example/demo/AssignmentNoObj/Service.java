package com.example.demo.AssignmentNoObj;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Service {

    private int BATCH_SIZE;
    private Pattern TIMESTAMP_PATTERN;
    private DateTimeFormatter DATETIME_FORMAT;
    private int CONSUMER_THREAD;
    private LinkedBlockingQueue<String> lineQueue;
    private LinkedBlockingQueue<String> writeQueue;

    private AtomicInteger linesRead = new AtomicInteger(0);
    private AtomicInteger validLines = new AtomicInteger(0);
    private AtomicInteger linesFound = new AtomicInteger(0);

    private static final String END_GAME = "::END_OF_STREAM::";
    Repository repo = new Repository();

    public Service(int batchSize, int consumerThreads, Pattern timestampPattern, DateTimeFormatter dateTimeFormatter, LinkedBlockingQueue<String> lineQueue, LinkedBlockingQueue<String> writeQueue) {
        this.BATCH_SIZE = batchSize;
        this.CONSUMER_THREAD = consumerThreads;
        this.TIMESTAMP_PATTERN = timestampPattern;
        this.DATETIME_FORMAT = dateTimeFormatter;
        this.lineQueue = lineQueue;
        this.writeQueue = writeQueue;
    }

    public void search(LocalDateTime from, LocalDateTime to, String logLevel, String message, CountDownLatch finished) {
        ExecutorService threadPool = Executors.newFixedThreadPool(CONSUMER_THREAD);

        for (int i = 0; i < CONSUMER_THREAD; i++) {
            threadPool.submit(() -> {
                try {
                    while (true) {
                        String line = lineQueue.take();

                        if (repo.isEndGame(line, END_GAME)) break;

                        if (!repo.isValidLogLine(line)) continue;
                        validLines.incrementAndGet();

                        if (
                                repo.checkTimestamp(line, from, to, TIMESTAMP_PATTERN, DATETIME_FORMAT) &&
                                        repo.checkLogLevel(line, logLevel) &&
                                        repo.checkMessage(line, message)) {

                            writeQueue.put(line);
                            linesFound.incrementAndGet(); // Tăng biến đếm
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finished.countDown();
                }
            });
        }
        threadPool.shutdown();
    }


    public void readFile(Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> {
                try {
                    lineQueue.put(line);
                    linesRead.incrementAndGet();
                } catch (InterruptedException e) {
                    System.out.println("Lỗi khi đưa dòng vào hàng đợi: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            });
        } catch (IOException ex) {
            System.err.println("Lỗi khi đọc file: " + ex.getMessage());
        } finally {
            for (int i = 0; i < CONSUMER_THREAD; i++) {
                try {
                    lineQueue.put(END_GAME);
                } catch (InterruptedException e) {
                    System.out.println("Lỗi khi gửi tín hiệu kết thúc: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
        System.out.println("Đã đọc xong file: " + path.getFileName());
    }

    public void exportTo(Path path) {
        // Luôn tạo mới và ghi đè file kết quả
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            List<String> batch = new ArrayList<>(BATCH_SIZE);
            boolean done = false;
            while (!done) {
                // Poll với timeout để không bị block vĩnh viễn
                String line = writeQueue.poll(50, TimeUnit.MILLISECONDS);

                if (line != null) {
                    if (END_GAME.equals(line)) {
                        done = true;
                    } else {
                        batch.add(line);
                    }
                }
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
            System.out.println("Lỗi trong quá trình ghi file: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        System.out.println("Ghi kết quả ra file thành công!");
    }

    public AtomicInteger getValidLines() {
        return this.validLines;
    }

    public AtomicInteger getLinesRead() {
        return linesRead;
    }

    public AtomicInteger getLinesFound() {
        return linesFound;
    }
}