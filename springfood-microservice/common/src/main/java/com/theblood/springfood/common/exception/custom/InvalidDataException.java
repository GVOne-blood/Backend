package com.theblood.springfood.common.exception.custom;

public class InvalidDataException extends ApplicationException {
    public InvalidDataException(String code, Object... var2) {
        super(code, var2);
    }
}
