package com.example.demo.Thread;

public class Code {

    public static void main(String[] args) {
        double start = (System.currentTimeMillis());
        SubThread sub = new SubThread();
        Thread thread = new Thread(sub);

        thread.start();
        if (!thread.isAlive())
            System.out.println(System.currentTimeMillis() - start + "ms");

    }
}

class SubThread implements Runnable {

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) System.out.println("now, we are blood bound");

    }
}
