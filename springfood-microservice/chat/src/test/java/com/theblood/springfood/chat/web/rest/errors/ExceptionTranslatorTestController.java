package com.theblood.springfood.chat.web.rest.errors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exception-translator-test")
public class ExceptionTranslatorTestController {

    @GetMapping("/concurrency-failure")
    public void concurrencyFailure() {
        throw new ConcurrencyFailureException("test concurrency failure");
    }

    @PostMapping("/method-argument")
    public void methodArgument(@Valid @RequestBody TestDTO testDTO) {
        // empty method
    }

    @GetMapping("/missing-servlet-request-part")
    public void missingServletRequestPartException(@RequestPart("part") String part) {
        // empty method
    }

    @GetMapping("/missing-servlet-request-parameter")
    public void missingServletRequestParameterException(@RequestParam("param") String param) {
        // empty method
    }

    @GetMapping("/access-denied")
    public void accessdenied() {
        throw new AccessDeniedException("test access denied!");
    }

    @GetMapping("/unauthorized")
    public void unauthorized() {
        throw new BadCredentialsException("test authentication failed!");
    }

    @GetMapping("/response-status")
    public void exceptionWithResponseStatus() {
        throw new TestResponseStatusException();
    }

    @GetMapping("/internal-server-error")
    public void internalServerError() {
        throw new RuntimeException();
    }

    @GetMapping("/authentication-exception")
    public void authenticationException() {
        throw new AuthenticationException("Invalid or expired JWT token");
    }

    @GetMapping("/authorization-exception")
    public void authorizationException() {
        throw new AuthorizationException("User is not a participant of this conversation", "conv-123");
    }

    @GetMapping("/validation-exception")
    public void validationException() {
        throw new ValidationException("DIRECT conversation must have exactly 2 participants", "participants", 3);
    }

    @GetMapping("/kafka-exception")
    public void kafkaException() {
        throw new org.springframework.kafka.KafkaException("Failed to publish message to Kafka");
    }

    @GetMapping("/database-exception")
    public void databaseException() {
        throw new org.springframework.dao.DataAccessResourceFailureException("Database connection failed");
    }

    @GetMapping("/redis-exception")
    public void redisException() {
        throw new org.springframework.data.redis.RedisConnectionFailureException("Redis connection failed");
    }

    public static class TestDTO {

        @NotNull
        private String test;

        public String getTest() {
            return test;
        }

        public void setTest(String test) {
            this.test = test;
        }
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "test response status")
    @SuppressWarnings("serial")
    public static class TestResponseStatusException extends RuntimeException {}
}
