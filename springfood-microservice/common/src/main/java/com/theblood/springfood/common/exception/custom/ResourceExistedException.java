package com.theblood.springfood.common.exception.custom;

public class ResourceExistedException extends ApplicationException {
    public ResourceExistedException(String code, Object... var2) {
        super(code, var2);
    }
}
