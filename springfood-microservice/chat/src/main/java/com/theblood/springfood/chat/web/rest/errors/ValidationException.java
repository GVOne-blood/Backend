package com.theblood.springfood.chat.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import tech.jhipster.web.rest.errors.ProblemDetailWithCause;

/**
 * Exception thrown when input validation fails.
 * Returns HTTP 400 Bad Request for REST endpoints.
 * Returns STOMP ERROR frame for WebSocket operations.
 */
@SuppressWarnings("java:S110")
public class ValidationException extends ErrorResponseException {

    private static final long serialVersionUID = 1L;

    private final String field;
    private final Object rejectedValue;

    public ValidationException(String message) {
        this(message, null, null);
    }

    public ValidationException(String message, String field, Object rejectedValue) {
        super(
            HttpStatus.BAD_REQUEST,
            ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
                .withTitle("Validation failed")
                .withDetail(message)
                .withProperty("message", "error.validation")
                .withProperty("field", field)
                .withProperty("rejectedValue", rejectedValue)
                .build(),
            null
        );
        this.field = field;
        this.rejectedValue = rejectedValue;
    }

    public String getField() {
        return field;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }
}
