package com.theblood.springfood.actionlog.exception;


import com.theblood.common.dto.response.ApiResponse;
import com.theblood.common.exception.custom.BadRequestException;
import com.theblood.common.exception.custom.NotFoundException;
import com.theblood.common.exception.custom.ResourceExistedException;
import com.theblood.common.util.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse
                .error(MessageUtils.getMessage(ex.getCode().replace(".message", ".code")),
                        MessageUtils.getMessage(ex.getMessage())));
    }

    @ExceptionHandler(ResourceExistedException.class)
    public ResponseEntity<?> handleResourceExistedException(ResourceExistedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse
                .error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse
                .error(MessageUtils.getMessage("error.400.code"), errors.toString()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse
                .error(MessageUtils.getMessage("error.400.code"),
                        MessageUtils.getMessage("error.400.message")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse
                .error(MessageUtils.getMessage("error.400.code"),
                        MessageUtils.getMessage(ex.getMessage())));
    }
}

