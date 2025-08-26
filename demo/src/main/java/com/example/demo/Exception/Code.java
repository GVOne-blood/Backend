package com.example.demo.Exception;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Code {

    public static void withdraw(double amount) throws InsufficientFundsException {
        double currentBalance = 100;
        if (amount > currentBalance) {
            throw new InsufficientFundsException(
                    "Số dư không đủ", currentBalance, amount);
        }
        // ...
    }

    public static void main(String[] args) {

        // checked exception
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // unchecked exception
        int[] a = new int[4];
        System.out.println(a[5]); // ArrayIndexOutOfBoundsException

        // try-catch-finally problem
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("data.txt"));
            String line = br.readLine();
            System.out.println(line);
        } catch (IOException e) {
            // Xử lý lỗi khi đọc file
            e.printStackTrace();
        } finally {
            // Khối finally để đảm bảo tài nguyên được đóng
            if (br != null) {
                try {
                    br.close(); // Phương thức close() cũng có thể ném IOException
                } catch (IOException ex) {
                    // Xử lý lỗi khi đóng tài nguyên
                    ex.printStackTrace();
                }
            }
        }

        // try-with-resources
        try (BufferedReader br2 = new BufferedReader(new FileReader("data.txt"))) {
            String line = br2.readLine();
            System.out.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // handle custom exception
        try {
            withdraw(5000.0);
        } catch (InsufficientFundsException e) {
            // Logic xử lý lỗi cụ thể
            System.err.println("Lỗi: " + e.getMessage());
            System.err.println("Số dư hiện tại: " + e.getCurrentBalance());
            // ... có thể hiển thị thông báo cho người dùng hoặc ghi log
        }

    }
}
