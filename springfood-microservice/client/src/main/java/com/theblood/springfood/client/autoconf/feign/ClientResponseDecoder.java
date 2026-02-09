package com.theblood.springfood.client.autoconf.feign;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.theblood.springfood.client.api.ClientResponse;
import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom decoder that wraps the response in ClientResponse and unwraps ApiResponse
 */
public class ClientResponseDecoder implements Decoder {

    private final Decoder delegate;
    private final ObjectMapper objectMapper;

    public ClientResponseDecoder(Decoder delegate) {
        this.delegate = delegate;
        this.objectMapper = new ObjectMapper();
        // Register JavaTimeModule to handle Instant and other Java 8 time types
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        // Check if the return type is ClientResponse
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();

            if (rawType.equals(ClientResponse.class)) {
                // Extract the actual type parameter (the T in ClientResponse<T>)
                Type actualType = parameterizedType.getActualTypeArguments()[0];

                // Decode the body using the delegate decoder
                Object body = null;
                if (response.body() != null) {
                    // Check if body has content - length() can return null for some response types
                    Integer bodyLength = response.body().length();
                    if (bodyLength == null || bodyLength > 0) {
                        // First, decode as a generic type to check if it's ApiResponse
                        Object decodedBody = delegate.decode(response, Object.class);

                        // Check if the decoded body is an ApiResponse wrapper
                        if (decodedBody instanceof Map) {
                            Map<?, ?> bodyMap = (Map<?, ?>) decodedBody;
                            // Check if it has the structure of ApiResponse (has 'detail', 'code', 'message' fields)
                            if (bodyMap.containsKey("detail") && bodyMap.containsKey("code") && bodyMap.containsKey("message")) {
                                // It's an ApiResponse, extract the 'detail' field
                                Object detail = bodyMap.get("detail");
                                // Convert the detail to the actual expected type
                                JavaType javaType = objectMapper.getTypeFactory().constructType(actualType);
                                body = objectMapper.convertValue(detail, javaType);
                            } else {
                                // Not an ApiResponse wrapper, use the decoded body as is
                                JavaType javaType = objectMapper.getTypeFactory().constructType(actualType);
                                body = objectMapper.convertValue(decodedBody, javaType);
                            }
                        } else {
                            // Use the decoded body directly
                            body = decodedBody;
                        }
                    }
                }

                // Extract headers
                Map<String, String> headers = new HashMap<>();
                response.headers().forEach((key, values) -> {
                    if (values != null && !values.isEmpty()) {
                        headers.put(key, values.iterator().next());
                    }
                });

                // Build ClientResponse
                return ClientResponse.builder()
                        .body(body)
                        .statusCode(response.status())
                        .headers(headers)
                        .success(response.status() >= 200 && response.status() < 300)
                        .responseTime(System.currentTimeMillis()) // Can track actual response time if needed
                        .build();
            }
        }

        // If not ClientResponse, delegate to the original decoder
        return delegate.decode(response, type);
    }
}
