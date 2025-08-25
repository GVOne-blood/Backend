package com.example.demo.PrimitiveAndObjectDataType;

public class Code {
public static void main(String[] args) {
    // init
    int a = 10; 
    Integer b = 20; 
    String s = new String("Hello"); 
    // default value
    int c;
    Integer d = null;
    
    /**
     * {@code
     * System.out.println(c); // Error: variable c might not have been initialized
     * }
     */

    System.out.println(d); // null
}
}
