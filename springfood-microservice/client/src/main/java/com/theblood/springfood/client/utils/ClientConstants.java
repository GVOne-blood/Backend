package com.theblood.springfood.client.utils;

import org.json.JSONObject;

/**
 * Constants for the client module
 */
public class ClientConstants {

    // Header constants
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_SESSION_ID = "X-Session-Id";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    // Timeout constants
    public static final long DEFAULT_CONNECT_TIMEOUT = 10000; // 10 seconds
    public static final long DEFAULT_READ_TIMEOUT = 30000; // 30 seconds
    public static final long DEFAULT_WRITE_TIMEOUT = 30000; // 30 seconds

    // Retry constants
    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final long DEFAULT_RETRY_DELAY = 1000; // 1 second

    // Circuit breaker constants
    public static final int DEFAULT_FAILURE_RATE_THRESHOLD = 50;
    public static final int DEFAULT_SLIDING_WINDOW_SIZE = 100;
    public static final int DEFAULT_MINIMUM_NUMBER_OF_CALLS = 20;

    // Legacy support - will be removed in future versions
    @Deprecated
    public static final JSONObject GRPC_CONFIG_JSON_OBJECT = new JSONObject();

    private ClientConstants() {
        // Prevent instantiation
    }
}

