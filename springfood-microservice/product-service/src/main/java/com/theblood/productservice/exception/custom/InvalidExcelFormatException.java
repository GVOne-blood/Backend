package com.theblood.productservice.exception.custom;

public class InvalidExcelFormatException extends RuntimeException {
    String MESSAGE_INVALID_EXCEL_FORMAT = "Invalid Excel file format.";
    String MESSAGE_EMPTY_EXCEL_FILE = "The Excel file is empty.";

    public InvalidExcelFormatException(String message) {
        super(message);
    }
}
