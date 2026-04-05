package com.theblood.springfood.chat.web.rest.errors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.KafkaException;

/**
 * Unit tests for {@link WebSocketExceptionHandler}.
 */
class WebSocketExceptionHandlerTest {

    private WebSocketExceptionHandler exceptionHandler;

    @BeforeEach
    void setup() {
        exceptionHandler = new WebSocketExceptionHandler();
    }

    @Test
    void testHandleAuthenticationException() {
        // Given
        AuthenticationException ex = new AuthenticationException("Invalid JWT token");

        // When
        Map<String, Object> response = exceptionHandler.handleAuthenticationException(ex);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.get("error")).isEqualTo("AUTHENTICATION_FAILED");
        assertThat(response.get("message")).asString().contains("Invalid JWT token");
        assertThat(response.get("status")).isEqualTo(401);
        assertThat(response.get("timestamp")).isNotNull();
    }

    @Test
    void testHandleAuthorizationException() {
        // Given
        AuthorizationException ex = new AuthorizationException("Not a participant", "conv-123");

        // When
        Map<String, Object> response = exceptionHandler.handleAuthorizationException(ex);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.get("error")).isEqualTo("AUTHORIZATION_FAILED");
        assertThat(response.get("message")).asString().contains("Not a participant");
        assertThat(response.get("status")).isEqualTo(403);
        assertThat(response.get("conversationId")).isEqualTo("conv-123");
        assertThat(response.get("timestamp")).isNotNull();
    }

    @Test
    void testHandleAuthorizationExceptionWithoutConversationId() {
        // Given
        AuthorizationException ex = new AuthorizationException("Not authorized");

        // When
        Map<String, Object> response = exceptionHandler.handleAuthorizationException(ex);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.get("error")).isEqualTo("AUTHORIZATION_FAILED");
        assertThat(response.get("message")).asString().contains("Not authorized");
        assertThat(response.get("status")).isEqualTo(403);
        assertThat(response.get("conversationId")).isNull();
    }

    @Test
    void testHandleValidationException() {
        // Given
        ValidationException ex = new ValidationException("Invalid participant count", "participants", 3);

        // When
        Map<String, Object> response = exceptionHandler.handleValidationException(ex);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.get("error")).isEqualTo("VALIDATION_FAILED");
        assertThat(response.get("message")).asString().contains("Invalid participant count");
        assertThat(response.get("status")).isEqualTo(400);
        assertThat(response.get("field")).isEqualTo("participants");
        assertThat(response.get("rejectedValue")).isEqualTo(3);
        assertThat(response.get("timestamp")).isNotNull();
    }

    @Test
    void testHandleValidationExceptionWithoutFieldDetails() {
        // Given
        ValidationException ex = new ValidationException("Invalid input");

        // When
        Map<String, Object> response = exceptionHandler.handleValidationException(ex);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.get("error")).isEqualTo("VALIDATION_FAILED");
        assertThat(response.get("message")).asString().contains("Invalid input");
        assertThat(response.get("status")).isEqualTo(400);
        assertThat(response.get("field")).isNull();
        assertThat(response.get("rejectedValue")).isNull();
    }

    @Test
    void testHandleKafkaException() {
        // Given
        KafkaException ex = new KafkaException("Kafka broker unavailable");

        // When
        Map<String, Object> response = exceptionHandler.handleKafkaException(ex);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.get("error")).isEqualTo("MESSAGE_SEND_FAILED");
        assertThat(response.get("message")).isEqualTo("Failed to send message after retries. Please try again.");
        assertThat(response.get("status")).isEqualTo(500);
        assertThat(response.get("timestamp")).isNotNull();
    }

    @Test
    void testHandleGenericException() {
        // Given
        Exception ex = new RuntimeException("Unexpected error");

        // When
        Map<String, Object> response = exceptionHandler.handleGenericException(ex);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.get("error")).isEqualTo("INTERNAL_ERROR");
        assertThat(response.get("message")).isEqualTo("An unexpected error occurred. Please try again.");
        assertThat(response.get("status")).isEqualTo(500);
        assertThat(response.get("timestamp")).isNotNull();
    }
}
