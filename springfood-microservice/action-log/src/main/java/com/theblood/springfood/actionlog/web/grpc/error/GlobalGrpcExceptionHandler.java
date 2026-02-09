package com.theblood.springfood.actionlog.web.grpc.error;

import build.buf.protovalidate.exceptions.ValidationException;
import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    @GrpcExceptionHandler(ValidationException.class)
    public Status handleValidationException(ValidationException ex) {
        return Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(RuntimeException.class)
    public Status handleRuntimeException(RuntimeException ex) {
        return Status.INTERNAL.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(AlreadyExistsException.class)
    public Status handleAlreadyExistException(AlreadyExistsException ex) {
        return Status.ALREADY_EXISTS.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(NotFoundException.class)
    public Status handleNotFoundException(NotFoundException ex) {
        return Status.NOT_FOUND.withDescription(ex.getMessage());
    }
}
