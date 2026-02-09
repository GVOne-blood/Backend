package com.theblood.springfood.actionlog.web.grpc;

import build.buf.protovalidate.ValidationResult;
import build.buf.protovalidate.Validator;
import build.buf.protovalidate.exceptions.ValidationException;
import com.google.protobuf.Message;
import lombok.SneakyThrows;

public class ProtoValidateUtils {

    static Validator validator = new Validator();

    @SneakyThrows
    public static void check(Message message) {
        ValidationResult result = validator.validate(message);
        if (!result.getViolations().isEmpty()) {
            throw new ValidationException(result.toString());
        }
    }
}
