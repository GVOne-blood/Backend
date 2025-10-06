package com.theblood.common.dto.response;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseError {
    private String error;
    private int status;
    private String message;
    private Date timestamp;
    private String path;

}
