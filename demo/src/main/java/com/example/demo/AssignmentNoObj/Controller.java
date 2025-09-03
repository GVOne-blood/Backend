package com.example.demo.AssignmentNoObj;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

public class Controller {

    // ================== CẤU HÌNH ==================
    static final Path LOG_FILE_PATH = Paths.get("src/main/java/com/example/demo/Assignment/log.txt");
    static final Path OUTPUT_PATH = Paths.get("src/main/java/com/example/demo/Assignment/res_no_obj.txt");

    private static final int CONSUMER_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int QUEUE_CAPACITY = 10000;
    private static final int BATCH_SIZE = 100;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("\\[(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\]");
    private static final String END_GAME = "::END_OF_STREAM::";

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        // ================== THAM SỐ TÌM KIẾM ==================
        String logLevelToSearch = "INFO";
        String messageKeywordToSearch = "User";
        LocalDateTime searchFrom = LocalDateTime.of(2023, 11, 21, 9, 0, 0);
        LocalDateTime searchTo = LocalDateTime.of(2023, 11, 23, 10, 0, 0);

        //worst case
//        String logLevelToSearch = null;
//        String messageKeywordToSearch = null;
//        LocalDateTime searchFrom = null;
//        LocalDateTime searchTo = null;

//        System.out.println("==============================TÌM KIẾM==============================");
//        System.out.println("Nhập gia trị cho các field, nhấn enter để bỏ trống field đó : ");
//        Scanner sc = new Scanner(System.in);
//        System.out.print("Log Level : ");
//        logLevelToSearch = sc.nextLine();
//        System.out.print("Message : ");
//        messageKeywordToSearch = sc.nextLine();
//        System.out.print("From : ");
//        String temp = sc.nextLine().trim();
//        if (temp.isBlank())
//            searchFrom = null;
//        searchFrom = LocalDateTime.parse(temp, DATE_TIME_FORMATTER);
//        System.out.print("To : ");
//        temp = sc.nextLine().trim();
//        if (temp.isBlank()) searchTo = LocalDateTime.now();
//        searchTo = LocalDateTime.parse(sc.nextLine().trim(), DATE_TIME_FORMATTER);
//        startTime = System.currentTimeMillis();

        System.out.println("Bắt đầu quá trình tìm kiếm log...");
        System.out.println("Tiêu chí: Level='" + logLevelToSearch + "', Message có chứa='" + messageKeywordToSearch + "'");
        System.out.println("Thời gian từ " + searchFrom + " đến " + searchTo);
        System.out.println("Số luồng xử lý (Consumer): " + CONSUMER_THREADS);
        System.out.println("----------------------------------------------");

        // 1. Khởi tạo các thành phần cần thiết
        LinkedBlockingQueue<String> lineQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        LinkedBlockingQueue<String> writeQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

        CountDownLatch consumersFinished = new CountDownLatch(CONSUMER_THREADS);

        // 2. Khởi tạo Service với các cấu hình đã định nghĩa
        Service service = new Service(BATCH_SIZE, CONSUMER_THREADS, TIMESTAMP_PATTERN, DATE_TIME_FORMATTER, lineQueue, writeQueue);


        // 3. Tạo các luồng (Producer, Writer)
        Thread producerThread = new Thread(() -> {
            service.readFile(LOG_FILE_PATH);
        });

        Thread writerThread = new Thread(() -> {
            service.exportTo(OUTPUT_PATH);
        });

        // 4. Bắt đầu thực thi
        writerThread.start();
        producerThread.start();

        // Khởi chạy các luồng consumer và truyền đúng CountDownLatch
        service.search(searchFrom, searchTo, logLevelToSearch, messageKeywordToSearch, consumersFinished);

        // 5. Điều phối và chờ các luồng hoàn thành
        try {

            producerThread.join();
            System.out.println("Producer đã hoàn thành.");

            consumersFinished.await();
            System.out.println("Tất cả Consumers đã hoàn thành.");

            // Gửi tín hiệu kết thúc cho Writer
            writeQueue.put(END_GAME);

            // Chờ Writer ghi file xong
            writerThread.join();
            System.out.println("Writer đã hoàn thành.");

        } catch (InterruptedException e) {
            System.err.println("Luồng chính bị gián đoạn.");
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();

        // 6. In kết quả tổng quan
        System.out.println("\n============== KẾT QUẢ TỔNG QUAN ==============");
        System.out.println("Tổng số dòng đã đọc: " + service.getLinesRead().get());
        System.out.println("Tổng số dòng hợp lệ: " + service.getValidLines().get());
        System.out.println("Tổng số kết quả tìm thấy: " + service.getLinesFound().get());
        System.out.println("Tổng thời gian thực thi: " + (endTime - startTime) + " ms");
        System.out.println("Kết quả đã được xuất ra file: " + OUTPUT_PATH.toAbsolutePath());
        System.out.println("===============================================");
    }
}