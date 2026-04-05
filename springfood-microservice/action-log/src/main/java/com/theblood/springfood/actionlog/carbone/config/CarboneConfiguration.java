package com.theblood.springfood.actionlog.carbone.config;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnProperty(name = "carbone.base-url")
public class CarboneConfiguration {

    @Bean
    public OkHttpClient carboneHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    }

    @Bean
    public Gson carboneGson() {
        return new Gson();
    }
}
