package com.theblood.springfood.client.autoconf.feign;

import com.theblood.springfood.client.config.ClientProperties;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * OkHttp interceptor for collecting metrics on Feign requests
 */
public class FeignMetricsInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(FeignMetricsInterceptor.class);

    private final ClientProperties clientProperties;

    public FeignMetricsInterceptor(ClientProperties clientProperties) {
        this.clientProperties = clientProperties;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (!clientProperties.getObservability().isMetricsEnabled()) {
            return chain.proceed(chain.request());
        }

        Request request = chain.request();
        long startTime = System.currentTimeMillis();

        String method = request.method();
        String path = request.url().encodedPath();
        String host = request.url().host();

        Response response = null;
        try {
            response = chain.proceed(request);

            long duration = System.currentTimeMillis() - startTime;
            recordMetrics(host, method, path, response.code(), duration, null);

            return response;
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;
            recordMetrics(host, method, path, 0, duration, e);
            throw e;
        }
    }

    private void recordMetrics(String host, String method, String path,
                               int statusCode, long duration, Exception error) {
        // In a real implementation, this would integrate with Micrometer
        // For now, just log the metrics
        if (error != null) {
            logger.debug("HTTP Request Failed - Host: {}, Method: {}, Path: {}, Duration: {}ms, Error: {}",
                    host, method, path, duration, error.getClass().getSimpleName());
        } else {
            logger.debug("HTTP Request - Host: {}, Method: {}, Path: {}, Status: {}, Duration: {}ms",
                    host, method, path, statusCode, duration);
        }
    }
}
