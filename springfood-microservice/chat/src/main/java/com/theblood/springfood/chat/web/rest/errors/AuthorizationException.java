package com.theblood.springfood.chat.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import tech.jhipster.web.rest.errors.ProblemDetailWithCause;

/**
 * Exception thrown when user is not authorized to perform an action.
 * Returns HTTP 403 Forbidden for REST endpoints.
 * Returns STOMP ERROR frame for WebSocket operations.
 */
@SuppressWarnings("java:S110")
public class AuthorizationException extends ErrorResponseException {

    private static final long serialVersionUID = 1L;

    private final String conversationId;

    public AuthorizationException(String message) {
        this(message, null);
    }

    public AuthorizationException(String message, String conversationId) {
        super(
            HttpStatus.FORBIDDEN,
            ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                .withStatus(HttpStatus.FORBIDDEN.value())
                .withType(ErrorConstants.DEFAULT_TYPE)
                .withTitle("Authorization failed")
                .withDetail(message)
                .withProperty("message", "error.authorization")
                .withProperty("conversationId", conversationId)
                .build(),
            null
        );
        this.conversationId = conversationId;
    }

    public String getConversationId() {
        return conversationId;
    }
}
