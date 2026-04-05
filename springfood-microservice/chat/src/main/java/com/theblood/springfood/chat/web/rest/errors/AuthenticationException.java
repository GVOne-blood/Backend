package com.theblood.springfood.chat.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import tech.jhipster.web.rest.errors.ProblemDetailWithCause;

/**
 * Exception thrown when authentication fails (invalid or expired JWT token).
 * Returns HTTP 401 Unauthorized for REST endpoints.
 * Returns STOMP ERROR frame for WebSocket connections.
 */
@SuppressWarnings("java:S110")
public class AuthenticationException extends ErrorResponseException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(
            HttpStatus.UNAUTHORIZED,
            ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                .withStatus(HttpStatus.UNAUTHORIZED.value())
                .withType(ErrorConstants.DEFAULT_TYPE)
                .withTitle("Authentication failed")
                .withDetail(message)
                .withProperty("message", "error.authentication")
                .build(),
            null
        );
    }

    public AuthenticationException(String message, Throwable cause) {
        super(
            HttpStatus.UNAUTHORIZED,
            ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                .withStatus(HttpStatus.UNAUTHORIZED.value())
                .withType(ErrorConstants.DEFAULT_TYPE)
                .withTitle("Authentication failed")
                .withDetail(message)
                .withProperty("message", "error.authentication")
                .build(),
            cause
        );
    }
}
