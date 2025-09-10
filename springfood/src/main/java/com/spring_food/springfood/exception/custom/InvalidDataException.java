package com.spring_food.springfood.exception.custom;

public class InvalidDataException extends RuntimeException {
    String message;

    public InvalidDataException(String message) {
        super(message);
    }
}
