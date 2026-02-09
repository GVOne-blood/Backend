package com.theblood.springfood.client.autoconf.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request interceptor for Feign clients
 * Adds common headers and propagates user context
 */
public class FeignRequestInterceptor implements RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(FeignRequestInterceptor.class);

    // Header constants
    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    @Override
    public void apply(RequestTemplate template) {

    }
}
