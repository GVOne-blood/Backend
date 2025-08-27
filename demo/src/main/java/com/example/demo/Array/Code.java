package com.example.demo.Array;

public class Code {
    public static void main(String[] args) {
        int [] arr; // Java
        int arr2[]; // C, C++
        arr = new int[5]; // Khởi tạo mảng với 5 phần tử
        arr2 = new int[]{1,2,3,4,5};
        //method
        int[] temp = arr.clone();
        for (int val : temp) System.out.print(val + " ");
        System.out.println();
        System.out.println(arr.toString()); // in ra địa chỉ vùng nhớ
        System.out.println(arr.hashCode()); // in ra địa chỉ vùng nhớ dưới dạng số nguyên


    }
}
