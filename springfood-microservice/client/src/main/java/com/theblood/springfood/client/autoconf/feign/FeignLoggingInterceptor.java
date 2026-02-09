package com.theblood.springfood.client.autoconf.feign;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * OkHttp interceptor for logging Feign requests and responses
 */
public class FeignLoggingInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(FeignLoggingInterceptor.class);

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (logger.isDebugEnabled()) {
            logRequest(request);
        }

        long startTime = System.nanoTime();
        Response response = chain.proceed(request);
        long duration = (System.nanoTime() - startTime) / 1_000_000;

        if (logger.isDebugEnabled()) {
            logResponse(response, duration);
        }

        return response;
    }

    private void logRequest(Request request) {
        logger.debug("---> {} {}", request.method(), request.url());

        // Log headers
        request.headers().names().forEach(name -> {
            if (!isSensitiveHeader(name)) {
                logger.debug("{}: {}", name, request.header(name));
            } else {
                logger.debug("{}: [REDACTED]", name);
            }
        });

        // Log body if present
        if (request.body() != null && logger.isTraceEnabled()) {
            try {
                Buffer buffer = new Buffer();
                request.body().writeTo(buffer);
                logger.trace("Request body: {}", buffer.readUtf8());
            } catch (IOException e) {
                logger.trace("Failed to log request body", e);
            }
        }

        logger.debug("---> END {}", request.method());
    }

    private void logResponse(Response response, long duration) throws IOException {
        logger.debug("<--- {} {} ({}ms)",
                response.code(),
                response.message().isEmpty() ? "" : response.message(),
                duration);

        // Log headers
        response.headers().names().forEach(name -> {
            if (!isSensitiveHeader(name)) {
                logger.debug("{}: {}", name, response.header(name));
            } else {
                logger.debug("{}: [REDACTED]", name);
            }
        });

        // Log body if present and trace is enabled
        if (response.body() != null && logger.isTraceEnabled()) {
            ResponseBody responseBody = response.peekBody(Long.MAX_VALUE);
            String body = responseBody.string();
            if (body.length() > 1000) {
                logger.trace("Response body (truncated): {}...", body.substring(0, 1000));
            } else {
                logger.trace("Response body: {}", body);
            }
        }

        logger.debug("<--- END HTTP");
    }

    private boolean isSensitiveHeader(String headerName) {
        String lower = headerName.toLowerCase();
        return lower.contains("authorization") ||
                lower.contains("token") ||
                lower.contains("secret") ||
                lower.contains("password") ||
                lower.contains("api-key");
    }
}
