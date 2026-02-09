package com.theblood.springfood.client.autoconf.feign;

import com.theblood.springfood.client.api.ClientRequest;
import feign.Param;
import feign.QueryMapEncoder;
import feign.codec.EncodeException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom parameter processor for ClientRequest in GET requests
 * Converts ClientRequest payload to query parameters
 */
public class ClientRequestParameterProcessor implements Param.Expander, QueryMapEncoder {

    @Override
    public String expand(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof ClientRequest) {
            ClientRequest<?> request = (ClientRequest<?>) value;
            Object body = request.getBody();
            if (body != null) {
                // Convert the body to a query string representation
                // This is a simple implementation, you might need more complex handling
                return body.toString();
            }
        }

        return value.toString();
    }

    @Override
    public Map<String, Object> encode(Object object) throws EncodeException {
        Map<String, Object> params = new HashMap<>();

        if (object == null) {
            return params;
        }

        if (object instanceof ClientRequest) {
            ClientRequest<?> request = (ClientRequest<?>) object;
            Object body = request.getBody();

            if (body != null) {
                // Convert body object to map of query parameters
                return objectToMap(body);
            }

            // If body is null, return query params if any
            if (!request.getQueryParams().isEmpty()) {
                return new HashMap<>(request.getQueryParams());
            }
        }

        return objectToMap(object);
    }

    private Map<String, Object> objectToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();

        if (obj == null) {
            return map;
        }

        // Handle Map directly
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }

        // Use reflection to convert object fields to map
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    map.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                // Skip fields that can't be accessed
            }
        }

        return map;
    }
}
