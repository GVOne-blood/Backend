package com.theblood.springfood.actionlog.web.grpc.error;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
