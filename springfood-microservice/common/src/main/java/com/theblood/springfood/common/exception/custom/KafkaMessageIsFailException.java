package com.theblood.springfood.common.exception.custom;

public class KafkaMessageIsFailException extends RuntimeException {
    public KafkaMessageIsFailException(String message) {
        super(message);
    }
}
