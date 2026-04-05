package com.theblood.springfood.chat.web.rest.errors;

import java.net.URI;

public final class ErrorConstants {

    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String ERR_AUTHENTICATION = "error.authentication";
    public static final String ERR_AUTHORIZATION = "error.authorization";
    public static final String ERR_KAFKA = "error.kafka";
    public static final String ERR_DATABASE = "error.database";
    public static final String ERR_REDIS = "error.redis";
    public static final String PROBLEM_BASE_URL = "https://www.jhipster.tech/problem";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
    public static final URI AUTHENTICATION_TYPE = URI.create(PROBLEM_BASE_URL + "/authentication");
    public static final URI AUTHORIZATION_TYPE = URI.create(PROBLEM_BASE_URL + "/authorization");

    private ErrorConstants() {}
}
