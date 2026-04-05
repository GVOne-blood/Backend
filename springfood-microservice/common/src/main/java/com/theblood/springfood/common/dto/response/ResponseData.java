package com.theblood.springfood.common.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResponseData<T> {

    private int appStatus;
    private String message;
    private T data;

    public ResponseData(int appStatus, String message) {
        this.appStatus = appStatus;
        this.message = message;
    }

    public ResponseData(int appStatus, String message, T data) {
        this.appStatus = appStatus;
        this.message = message;
        this.data = data;
    }
}
