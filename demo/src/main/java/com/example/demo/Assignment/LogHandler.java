package com.example.demo.Assignment;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class LogHandler implements Serializable {
    public static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(100);
    public static final String stopSingal = "STOP";
    public static int core = Runtime.getRuntime().availableProcessors() + 10;

    static AtomicInteger cnt = new AtomicInteger(0);

    public static LogLevel toEnum(String level) {
        return switch (level.toUpperCase()) {
            case "WARN" -> LogLevel.WARM;
            case "INFO" -> LogLevel.INFO;
            case "ERROR" -> LogLevel.ERROR;
            default -> LogLevel.UNKNOW;
        };
    }

    public static String[] extractLog(String log) {
        String[] logs = new String[4];
        log = (String) log.subSequence(1, log.indexOf("-"));
        logs = log.split(" ");
        cnt.incrementAndGet();
        return logs;
    }

    public static String searchByLogLevel(LogLevel level) {

        return "";
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        Path inpFile = Paths.get("src/main/java/com/example/demo/Assignment/inp.txt");
        String logLevel = null;
        String timeStamp = null;
        String service = null;
        String message = null;

        try {
            List<String> lines = new ArrayList<>(4);
            lines = Files.readAllLines(inpFile);
            logLevel = lines.get(0);
            timeStamp = lines.get(1);
            service = lines.get(2);
            message = lines.get(3);
            System.out.println(logLevel + "  " + timeStamp + " " + service + " " + message);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Current cores : " + core);
        ExecutorService consumerPool = Executors.newFixedThreadPool(core);

        ReadFile reader = new ReadFile();
        Thread readFile = new Thread(reader);
        readFile.start();

        for (int i = 0; i < core; i++) {
            String finalLogLevel = logLevel;
            consumerPool.submit(() -> {
                while (true) {
                    String line = null;
                    try {
                        // Lấy một dòng từ queue
                        line = queue.take();

                        // Kiểm tra tín hiệu dừng
                        if (line.equals(stopSingal)) {
                            queue.put(stopSingal);
                            break;
                        }

                        // Bỏ qua các dòng trống
                        if (line.trim().isEmpty()) {
                            continue;
                        }

                        // Xử lý dòng log
                        String[] temp = extractLog(line);
                        // System.out.println(line);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {

                        System.err.println("Lỗi khi xử lý dòng: '" + line + "' -> Bỏ qua.");
                    }
                }
            });
        }

        try {
            readFile.join();
            consumerPool.shutdown();
            if (!consumerPool.awaitTermination(1, TimeUnit.HOURS)) {
                System.err.println("Pool không thể tắt, buộc phải tắt ngay.");
                consumerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            consumerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        long stop = System.currentTimeMillis();
        long totalTime = stop - start;
        System.out.println("=============================================");
        System.out.println("Total time : " + totalTime + "ms");
        System.out.println("Logs : " + cnt.get());
        System.out.println("=============================================");
    }

    // Giữ nguyên class ReadFile của bạn
    static class ReadFile implements Runnable {
        @Override
        public void run() {
            Path filePath = Paths.get("src/main/java/com/example/demo/Assignment/log.txt");

            try (Stream<String> lines = Files.lines(filePath)) {
                for (String line : (Iterable<String>) lines::iterator) {
                    try {
                        queue.put(line);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Luồng đọc file bị lỗi");
                        System.out.println(e.getMessage());
                        break;
                    }
                }
            } catch (IOException ex) {
                System.out.println("Có lỗi trong quá trình đọc ghi file ");
                System.out.println(ex.getMessage());
            } finally {
                System.out.println("Đã đọc xong file, gửi tín hiệu kết thúc đọc file");
                for (int i = 0; i < core; i++) {
                    try {
                        queue.put(stopSingal);
                    } catch (InterruptedException e) {
                        System.out.println("Cant add stopSignal");
                        System.out.println(e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}