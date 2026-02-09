package com.theblood.springfood.actionlog.web.grpc.error;

public class AlreadyExistsException extends RuntimeException {

    public AlreadyExistsException(String message) {
        super(message);
    }
}
