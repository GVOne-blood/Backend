package com.theblood.springfood.client.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic request wrapper for client calls
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequest<T> {

    private T body;
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();
    @Builder.Default
    private Map<String, String> queryParams = new HashMap<>();
    @Builder.Default
    private Map<String, String> pathParams = new HashMap<>();

    public ClientRequest(T body) {
        this.body = body;
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
        this.pathParams = new HashMap<>();
    }

    public static <T> ClientRequest<T> of(T body) {
        return new ClientRequest<>(body);
    }

    public static ClientRequest<Void> empty() {
        return new ClientRequest<>();
    }

    public T getBody() {
        return body;
    }

    public ClientRequest<T> setBody(T body) {
        this.body = body;
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public ClientRequest<T> setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public ClientRequest<T> addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public ClientRequest<T> setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
        return this;
    }

    public ClientRequest<T> addQueryParam(String key, String value) {
        this.queryParams.put(key, value);
        return this;
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public ClientRequest<T> setPathParams(Map<String, String> pathParams) {
        this.pathParams = pathParams;
        return this;
    }

    public ClientRequest<T> addPathParam(String key, String value) {
        this.pathParams.put(key, value);
        return this;
    }
}
