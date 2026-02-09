package com.theblood.common.exception.custom;

public class NotFoundException extends ApplicationException {
    public NotFoundException(String code, Object... var2) {
        super(code, var2);
    }
}
