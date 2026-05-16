package com.theblood.cartservice.common;

import com.theblood.springfood.common.dto.response.ResponseData;
import com.theblood.springfood.common.exception.custom.InvalidDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralised exception handling for cart-service so that bad inputs surface
 * as proper HTTP 4xx responses instead of leaking through as a generic 500.
 *
 * <p>Earlier callers that posted a non-UUID {@code productId} or {@code shopId}
 * (e.g. mock/demo product cards using integer IDs) caused
 * {@link IllegalArgumentException} from {@code UUID.fromString}, which the
 * default Spring handler mapped to {@code 500 Internal Server Error}. The FE
 * couldn't distinguish that from a genuine outage, so we now translate it to
 * a clear {@code 400 Bad Request}.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** Domain-level validation: missing fields, malformed identifiers, etc. */
    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ResponseData<Object>> handleInvalidData(InvalidDataException ex) {
        log.warn("Bad request rejected by cart-service: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseData<>(400, ex.getMessage(), null));
    }

    /**
     * Generic guard for any other {@link IllegalArgumentException} (e.g. raw
     * {@code UUID.fromString} calls deeper in the stack we haven't wrapped
     * yet) — still surfaces as 400, never as 500.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseData<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException in cart-service: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseData<>(400, ex.getMessage(), null));
    }

    /** Fallback so unexpected errors are still logged with a stable shape. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData<Object>> handleGeneric(Exception ex) {
        log.error("Unhandled exception in cart-service", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseData<>(500, "Internal server error", null));
    }
}
