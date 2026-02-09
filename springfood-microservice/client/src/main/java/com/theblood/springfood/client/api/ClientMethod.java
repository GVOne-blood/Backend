package com.theblood.springfood.client.api;

import java.lang.annotation.*;

/**
 * Annotation to mark client methods with metadata for both REST and gRPC
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ClientMethod {

    /**
     * The HTTP method for REST calls (GET, POST, PUT, DELETE, etc.)
     * Ignored for gRPC
     */
    String httpMethod() default "POST";

    /**
     * The path for REST endpoint (can include path variables like {id})
     * Ignored for gRPC
     */
    String path() default "";

    /**
     * The gRPC method name
     * Ignored for REST
     */
    String grpcMethod() default "";

    /**
     * Whether this method is idempotent (safe to retry)
     */
    boolean idempotent() default false;

    /**
     * Custom timeout in milliseconds (0 means use default)
     */
    long timeout() default 0;

    /**
     * Whether to include user context in the request
     */
    boolean includeUserContext() default true;
}
