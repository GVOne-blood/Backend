package com.theblood.common.exception.custom;

public class BadRequestException extends ApplicationException {
    public BadRequestException(String code, Object... var2) {
        super(code, var2);
    }
}
