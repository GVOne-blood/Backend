package com.theblood.springfood.client.autoconf.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.springfood.client.api.ClientRequest;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.jackson.JacksonEncoder;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Custom Feign encoder that handles ClientRequest objects
 * Extracts the body from ClientRequest and delegates to JacksonEncoder
 */
public class ClientRequestEncoder implements Encoder {

    private final JacksonEncoder delegate;

    public ClientRequestEncoder(ObjectMapper objectMapper) {
        this.delegate = new JacksonEncoder(objectMapper);
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        // Check if the object is a ClientRequest
        if (object instanceof ClientRequest) {
            ClientRequest<?> clientRequest = (ClientRequest<?>) object;

            // Extract the actual body from ClientRequest
            Object body = clientRequest.getBody();

            // Add headers from ClientRequest to the template
            Map<String, String> headers = clientRequest.getHeaders();
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    template.header(entry.getKey(), entry.getValue());
                }
            }

            // Add query parameters from ClientRequest to the template
            Map<String, String> queryParams = clientRequest.getQueryParams();
            if (queryParams != null && !queryParams.isEmpty()) {
                for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                    template.query(entry.getKey(), entry.getValue());
                }
            }

            // If there's a body, encode it using the delegate encoder
            if (body != null) {
                delegate.encode(body, body.getClass(), template);
            }
        } else {
            // Not a ClientRequest, delegate directly
            delegate.encode(object, bodyType, template);
        }
    }
}
