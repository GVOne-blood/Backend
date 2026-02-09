package com.theblood.springfood.client.autoconf.feign;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Custom error decoder for Feign clients
 */
public class FeignErrorDecoder implements ErrorDecoder {

    private static final Logger logger = LoggerFactory.getLogger(FeignErrorDecoder.class);
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        String errorMessage = extractErrorMessage(response);

        switch (response.status()) {
            case 400:
                logger.error("Bad Request calling {}: {}", methodKey, errorMessage);
                return new ClientBadRequestException(errorMessage);

            case 401:
                logger.error("Unauthorized calling {}: {}", methodKey, errorMessage);
                return new ClientUnauthorizedException(errorMessage);

            case 403:
                logger.error("Forbidden calling {}: {}", methodKey, errorMessage);
                return new ClientForbiddenException(errorMessage);

            case 404:
                logger.error("Not Found calling {}: {}", methodKey, errorMessage);
                return new ClientNotFoundException(errorMessage);

            case 409:
                logger.error("Conflict calling {}: {}", methodKey, errorMessage);
                return new ClientConflictException(errorMessage);

            case 429:
                logger.error("Too Many Requests calling {}: {}", methodKey, errorMessage);
                return new ClientRateLimitException(errorMessage);

            case 500:
            case 502:
            case 503:
            case 504:
                logger.error("Server Error calling {} (status {}): {}",
                        methodKey, response.status(), errorMessage);
                return new ClientServerException(errorMessage, response.status());

            default:
                return defaultDecoder.decode(methodKey, response);
        }
    }

    private String extractErrorMessage(Response response) {
        if (response.body() != null) {
            try {
                return feign.Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            } catch (IOException e) {
                logger.warn("Failed to read error response body", e);
            }
        }
        return "Error " + response.status() + " - " + response.reason();
    }

    // Custom exception classes

    public static class ClientException extends RuntimeException {
        private final int statusCode;

        public ClientException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public static class ClientBadRequestException extends ClientException {
        public ClientBadRequestException(String message) {
            super(message, 400);
        }
    }

    public static class ClientUnauthorizedException extends ClientException {
        public ClientUnauthorizedException(String message) {
            super(message, 401);
        }
    }

    public static class ClientForbiddenException extends ClientException {
        public ClientForbiddenException(String message) {
            super(message, 403);
        }
    }

    public static class ClientNotFoundException extends ClientException {
        public ClientNotFoundException(String message) {
            super(message, 404);
        }
    }

    public static class ClientConflictException extends ClientException {
        public ClientConflictException(String message) {
            super(message, 409);
        }
    }

    public static class ClientRateLimitException extends ClientException {
        public ClientRateLimitException(String message) {
            super(message, 429);
        }
    }

    public static class ClientServerException extends ClientException {
        public ClientServerException(String message, int statusCode) {
            super(message, statusCode);
        }
    }
}
