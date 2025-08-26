package com.example.demo.Exception;

public class InsufficientFundsException extends Exception {

    private static final long serialVersionUID = 7718828512143293558L;

    // Các trường dữ liệu ngữ cảnh, nên là final để đảm bảo tính bất biến.
    private final double currentBalance;
    private final double amountRequested;

    public InsufficientFundsException(String message, double currentBalance, double amountRequested) {
        super(message);
        this.currentBalance = currentBalance;
        this.amountRequested = amountRequested;
    }

    public InsufficientFundsException(String message, Throwable cause, double currentBalance, double amountRequested) {
        super(message, cause);
        this.currentBalance = currentBalance;
        this.amountRequested = amountRequested;
    }

    // Các phương thức getter để mã nguồn xử lý lỗi truy cập dữ liệu ngữ cảnh.
    public double getCurrentBalance() {
        return currentBalance;
    }

    public double getAmountRequested() {
        return amountRequested;
    }
}