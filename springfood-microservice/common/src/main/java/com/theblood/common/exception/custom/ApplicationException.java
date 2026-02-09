package com.theblood.common.exception.custom;


import com.theblood.common.util.MessageUtils;
import lombok.Getter;

public class ApplicationException extends RuntimeException {
    @Getter
    private String code;
    private String message;
    private StatusCode statusCode;

    public ApplicationException() {
    }

    public ApplicationException(StatusCode statusCode) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
    }

    public ApplicationException(StatusCode statusCode, String mess) {
        super(mess);
        this.statusCode = statusCode;
    }


    public ApplicationException(String code, Object... var2) {
        this.code = code;
        this.message = MessageUtils.getMessage(code, var2);
    }

    @Override
    public String getMessage() {
        return message;
    }

}
