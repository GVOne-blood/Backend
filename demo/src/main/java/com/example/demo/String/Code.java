package com.example.demo.String;

public class Code {
    public static void main(String[] args) {
        // immutable
        String str = "Hello";
        str = str + " World"; // bản chất str là 1 tham chiếu,
        // tham chiếu này trỏ đến vùng nhớ mới chứa "Hello World"
        System.out.println(str); // Hello World
        
        // init
        // trực tiếp
        String str1 = "Java"; // String literal
        String str2 = new String("Java"); // tạo trong heap memory
        // gián tiếp
        char[] arr = {'J', 'a', 'v', 'a'}; // from char array
        String str3 = new String(arr);
        byte[] bytes = {65, 66, 67, 68}; // from byte array
        String str4 = new String(bytes);
        String str5 = String.valueOf(123); // from other data types
        
        // String pool
        String s1 = "Java"; // tạo trong String pool
        String s2 = "Java"; // tham chiếu s2 trỏ đến vùng nhớ
        String s3 = new String("Java"); // tạo trong heap memory
        System.out.println(s1 == s2); // true
        System.out.println(s1 == s3); // false
    
    }

}

