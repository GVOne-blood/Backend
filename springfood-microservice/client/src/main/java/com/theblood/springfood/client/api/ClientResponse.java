package com.theblood.springfood.client.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Generic response wrapper for client calls
 */
public class ClientResponse<T> {

    private T body;
    private int statusCode;
    private Map<String, String> headers = new HashMap<>();
    private boolean success;
    private String errorMessage;
    private long responseTime;

    // Default constructor for Jackson deserialization
    public ClientResponse() {
    }

    // Constructor for Jackson deserialization with all fields
    @JsonCreator
    public ClientResponse(
            @JsonProperty("body") T body,
            @JsonProperty("statusCode") int statusCode,
            @JsonProperty("headers") Map<String, String> headers,
            @JsonProperty("success") boolean success,
            @JsonProperty("errorMessage") String errorMessage,
            @JsonProperty("responseTime") long responseTime) {
        this.body = body;
        this.statusCode = statusCode;
        this.headers = headers != null ? headers : new HashMap<>();
        this.success = success;
        this.errorMessage = errorMessage;
        this.responseTime = responseTime;
    }

    private ClientResponse(Builder<T> builder) {
        this.body = builder.body;
        this.statusCode = builder.statusCode;
        this.headers = builder.headers;
        this.success = builder.success;
        this.errorMessage = builder.errorMessage;
        this.responseTime = builder.responseTime;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static <T> ClientResponse<T> success(T body) {
        return ClientResponse.<T>builder()
                .body(body)
                .statusCode(200)
                .success(true)
                .build();
    }

    public static <T> ClientResponse<T> error(String errorMessage) {
        return ClientResponse.<T>builder()
                .errorMessage(errorMessage)
                .statusCode(500)
                .success(false)
                .build();
    }

    public static <T> ClientResponse<T> error(int statusCode, String errorMessage) {
        return ClientResponse.<T>builder()
                .errorMessage(errorMessage)
                .statusCode(statusCode)
                .success(false)
                .build();
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public Optional<T> getBodyOptional() {
        return Optional.ofNullable(body);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public static class Builder<T> {
        private T body;
        private int statusCode = 200;
        private Map<String, String> headers = new HashMap<>();
        private boolean success = true;
        private String errorMessage;
        private long responseTime;

        public Builder<T> body(T body) {
            this.body = body;
            return this;
        }

        public Builder<T> statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder<T> headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder<T> addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder<T> errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder<T> responseTime(long responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        public ClientResponse<T> build() {
            return new ClientResponse<>(this);
        }
    }
}
