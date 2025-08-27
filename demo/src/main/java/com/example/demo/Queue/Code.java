package com.example.demo.Queue;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class Code {
    public static void main(String[] args) {
        Queue queue = new ArrayDeque();
        queue.addAll(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        System.out.println(queue);
    }
}
