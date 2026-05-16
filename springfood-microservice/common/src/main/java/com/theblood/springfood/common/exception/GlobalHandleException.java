package com.theblood.springfood.common.exception;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.theblood.springfood.common.dto.response.ResponseError;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.security.SignatureException;
import java.util.Date;

@RestControllerAdvice
public class GlobalHandleException {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseError> handleNoResourceFoundException(NoResourceFoundException e, WebRequest request) {
        ResponseError error = ResponseError.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .timestamp(new Date())
                .message("Endpoint khong ton tai :  " + e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<ResponseError> handleRedisConnectionFailureException(RedisConnectionFailureException e, WebRequest request) {
        ResponseError error = ResponseError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .timestamp(new Date())
                .message("Redis Connection Error : " + e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseError> handleAuthenticationException(AuthenticationException e, WebRequest request) {
        ResponseError error = ResponseError.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .timestamp(new Date())
                .message(e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseError> handleAccessDeniedException(AccessDeniedException e, WebRequest request) {
        ResponseError error = ResponseError.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .timestamp(new Date())
                .message(e.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ResponseError> handleException(InvalidDataAccessApiUsageException e, WebRequest request) {
        // Sanitize error message to prevent SQL query disclosure
        String sanitizedMessage = "Invalid database operation";
        
        // Log the actual error for debugging (server-side only)
        System.err.println("Database Error: " + e.getMessage());
        
        ResponseError error = ResponseError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .timestamp(new Date())
                .message(sanitizedMessage)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(InvalidDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handleInvalidDataException(InvalidDataException e, WebRequest request) {
        return ResponseError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(e.getMessage())
                .timestamp(new Date())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseError handleSqlException(Exception e, WebRequest request) {
        // Sanitize SQL error message to prevent information disclosure
        String sanitizedMessage = "Database constraint violation occurred";
        
        // Log the actual error for debugging (server-side only)
        System.err.println("SQL Error: " + e.getMessage());
        
        // Check for common constraint violations and provide user-friendly messages
        String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (errorMsg.contains("duplicate") || errorMsg.contains("unique")) {
            sanitizedMessage = "Duplicate entry. This record already exists.";
        } else if (errorMsg.contains("foreign key")) {
            sanitizedMessage = "Cannot perform operation due to related data constraints.";
        } else if (errorMsg.contains("not null")) {
            sanitizedMessage = "Required field is missing.";
        }
        
        return ResponseError.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(sanitizedMessage)
                .timestamp(new Date())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }

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
    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handleParamException(Exception e, WebRequest request) {
        ResponseError errorResponse = new ResponseError();
        errorResponse.setStatus(HttpStatus.BAD_GATEWAY.value());

        String message = e.getMessage();
        //message.substring(message.lastIndexOf('.') + 1);
        errorResponse.setMessage(message); // lấy ra lỗi ở dạng text
        errorResponse.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorResponse.setTimestamp(new Date());
        errorResponse.setPath(request.getDescription(false).replace("url=", "")); // lấy ra đường dẫn request
        return errorResponse;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handleJsonParseException(HttpMessageNotReadableException e, WebRequest request) {
        String message = "Định dạng JSON không hợp lệ hoặc dữ liệu không đọc được.";

        // Cố gắng lấy thông tin chi tiết hơn từ lỗi gốc (root cause)
        Throwable cause = e.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            // Lấy tên trường bị lỗi
            String fieldName = ife.getPath().stream()
                    .map(ref -> ref.getFieldName())
                    .findFirst().orElse("không xác định");
            // Lấy giá trị không hợp lệ
            Object invalidValue = ife.getValue();
            message = String.format("Giá trị '%s' không hợp lệ cho trường '%s'.", invalidValue, fieldName);
        }

        return ResponseError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .timestamp(new Date())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }
}
