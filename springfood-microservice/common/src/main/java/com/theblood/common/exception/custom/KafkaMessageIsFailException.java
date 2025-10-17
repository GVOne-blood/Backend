package com.theblood.common.exception.custom;

public class KafkaMessageIsFailException extends RuntimeException {
    public KafkaMessageIsFailException(String message) {
        super(message);
    }
}
