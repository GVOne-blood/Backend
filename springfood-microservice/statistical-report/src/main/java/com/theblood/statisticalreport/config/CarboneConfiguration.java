package com.theblood.statisticalreport.config;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration cho Carbone report service.
 * Chỉ active khi có carbone.base-url trong application config.
 */
@Configuration
@ConditionalOnProperty(name = "carbone.base-url")
public class CarboneConfiguration {

    @Bean("carboneHttpClient")
    public OkHttpClient carboneHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        return new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    }

    @Bean("carboneGson")
    public Gson carboneGson() {
        return new Gson();
    }
}
