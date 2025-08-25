package com.example.demo.StaticAndFinal;

import java.util.ArrayList;
import java.util.List;

public class Code {
    static int count = 0;

    public static int increment() {
        return ++count;
    }

    // Count: 1
    static final double PI = 3.14;

    public static void main(String[] args) {
        // final
        final int MAX = 100;
        // MAX = 200; // Error: cannot assign a value to final variable MAX
        System.out.println(MAX); // 100

        System.out.println(PI); // 3.14

        final List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
        list.add(4); // OK
        list.remove(0); // OK
        System.out.println(list); // [2, 3, 4]

        // Static
        // biến static (class variable)
        System.out.println(increment()); // Count: 1
        System.out.println(increment()); // Count: 2
        System.out.println(increment()); // Count: 3

    }
}
