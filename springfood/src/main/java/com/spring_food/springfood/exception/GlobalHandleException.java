package com.spring_food.springfood.exception;

import com.spring_food.springfood.dto.response.ResponseData;
import com.spring_food.springfood.dto.response.ResponseError;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Date;

@RestControllerAdvice
public class GlobalHandleException {
   
    @ExceptionHandler(value = {ExpiredJwtException.class, MalformedJwtException.class, SignatureException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseError handleJwtException(Exception e, WebRequest request) {
        return 
                ResponseError.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .message(e.getMessage())
                        .timestamp(new Date())
                        .path(request.getDescription(false).replace("uri=", ""))
                        .build();
    }

    // handle exception when some field in param is not valid
    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus (HttpStatus.BAD_GATEWAY)
    public ResponseError handleParamException(Exception e, WebRequest request){
        ResponseError errorResponse = new ResponseError();
        errorResponse.setStatus(HttpStatus.BAD_GATEWAY.value());

        String message = e.getMessage();
        //message.substring(message.lastIndexOf('.') + 1);
        errorResponse.setMessage(message); // lấy ra lỗi ở dạng text
        errorResponse.setError(HttpStatus.BAD_GATEWAY.getReasonPhrase()); // lấy ra lỗi ở dạng text (bad-getaway)
        errorResponse.setTimestamp( new Date());
        errorResponse.setPath(request.getDescription(false ).replace("url=", "")); // lấy ra đường dẫn request
        return errorResponse;
    }
}
