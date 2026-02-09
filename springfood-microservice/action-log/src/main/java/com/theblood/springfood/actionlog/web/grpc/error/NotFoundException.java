package com.theblood.springfood.actionlog.web.grpc.error;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException() {
        super("Entity doesn't exist");
    }
}
