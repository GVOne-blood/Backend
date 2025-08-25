package com.example.demo.PrimitiveAndObjectDataType;

public class Code {
    public static int valInt;
    public static Integer valInteger;

    public static <T> void changeVal(T val) {
        val = null;
        System.out.println("Inside changeVal: " + val); // null
    }

    public static void main(String[] args) {
        // reference
        valInt = 100;
        valInteger = 200;
        System.out.println("Before change: " + valInt + " - " + valInteger); // 100 - 200
        changeVal(valInt);
        changeVal(valInteger);
        System.out.println("After change: " + valInt + " - " + valInteger); // 100 - 200

        // init
        int a = 10;
        Integer b = 20;
        String s = new String("Hello");
        // default value
        int c;
        Integer d = null;
        int[] arr = new int[5];
        for (int i : arr) {
            System.out.print(i + " "); // 0 0 0 0 0
        }

        // System.out.println(c); // Error: variable c might not have been initialized
        System.out.println(d); // null

        // OOP
        System.out.println(b.toString()); // 20
        System.out.println(s.length()); // 5

        // Wrapper
        // int -> Integer (Boxing)
        int num = 30;
        Integer boxedA = Integer.valueOf(num);
        // or
        Integer boxedB = new Integer(num); // Constructor (deprecated since Java 9)
        // or
        Integer boxedC = num; // Autoboxing

        // Integer -> int (Unboxing)
        Integer numObj = Integer.valueOf(40);
        int unboxedA = numObj.intValue();
        // or
        int unboxedB = numObj; // Autounboxing

        String strNum = "50";

        // Compareable
        int x = 10;
        Integer y = 10;
        Integer z = 10;
        System.out.println(x == y); // true (unboxing y to int)
        System.out.println(y.equals(x)); // true (boxing x to Integer)
        System.out.println(y == z);
    }
}