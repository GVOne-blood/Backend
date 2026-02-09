package com.theblood.common.exception.custom;

public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int errorCode;
    private String errorMessage;
    private String detailErrorMessage;
    private String errorInfo;


    public CustomException(int errorCode, String errorMessage) {
        this(errorCode, errorMessage, null);
    }

    public CustomException(int errorCode, String errorMessage, String detailErrorMessage) {
        this(errorCode, errorMessage, detailErrorMessage, null);
    }

    public CustomException(int errorCode, String errorMessage, String detailErrorMessage, String errorInfo) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.detailErrorMessage = detailErrorMessage;
        this.errorInfo = errorInfo;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDetailErrorMessage() {
        return detailErrorMessage;
    }

    public void setDetailErrorMessage(String detailErrorMessage) {
        this.detailErrorMessage = detailErrorMessage;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }
}
