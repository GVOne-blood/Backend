package com.example.demo.Exception;

public class Code {
    public static void main(String[] args) {

        //checked exception
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //unchecked exception
        int[] a = new int[4];
        System.out.println(a[5]); // ArrayIndexOutOfBoundsException
    }
}
