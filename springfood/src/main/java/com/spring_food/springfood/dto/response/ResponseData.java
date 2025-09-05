package com.spring_food.springfood.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor

public class ResponseData<T>{

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
