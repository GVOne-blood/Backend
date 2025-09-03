package com.example.demo.SyncAndAsync;

import java.util.ArrayList;
import java.util.List;

public class Code {

    private static int cnt = 0;

    public static void main(String[] args) throws InterruptedException {
        int numThreads = 200;
        int incrementsPerThread = 100;

        List<Thread> threads = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    cnt++; // Chỉ tăng thôi để thấy rõ lỗi
                }
            });
            threads.add(thread);
            thread.start();
        }

        // Đảm bảo luồng main chờ TẤT CẢ các luồng con chạy xong
        for (Thread t : threads) {
            t.join();
        }

        System.out.println("Expected count: " + (numThreads * incrementsPerThread));
        System.out.println("Actual count  : " + cnt);
    }
}

class StopThread {
    private volatile static boolean stopRequested = false;

    public static void main(String[] args) throws InterruptedException {

        Thread backgroundThread = new Thread(() -> {
            int i = 0;
            while (!stopRequested) { // Luồng này liên tục kiểm tra biến stopRequested
                i++;
            }
            System.out.println("Background thread stopped.");
        });
        backgroundThread.start();

        Thread.sleep(1000); // Chờ 1 giây

        System.out.println("Requesting stop...");
        stopRequested = true; // Luồng main thay đổi giá trị
        backgroundThread.join();
        // System.out.println("Stopped");
    }
}