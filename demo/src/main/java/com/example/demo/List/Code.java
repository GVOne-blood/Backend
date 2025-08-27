package com.example.demo.List;

import java.util.List;

public class Code {
    // init
    List<Integer> list1 = List.of(1, 2, 3, 4, 5); // immutable
    List<Integer> list2 = List.copyOf(list1); // immutable
    // mutable
    List<Integer> list3 = new java.util.ArrayList<>(list1);
    List<Integer> list4 = new java.util.LinkedList<>(list1);
    List<Integer> list5 = new java.util.Vector<>(list1);
    List<Integer> list6 = new java.util.Stack<>(); // LIFO
    // method
}
