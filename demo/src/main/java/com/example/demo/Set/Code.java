package com.example.demo.Set;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Code {

    static Set<Integer> set = new HashSet<>(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    static List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        long stop = 0;
        long total = 0;
        if (set.contains(8)) {
            stop = System.currentTimeMillis();
            total = stop - start;
            System.out.println("Tim kiem thanh cong SET " + total + " ms");
        }
        if (list.contains(8)) {
            stop = System.currentTimeMillis();
            total = stop - start;
            System.out.println("Tim kiem thanh cong LIST " + total + " ms");
        }

    }
}
