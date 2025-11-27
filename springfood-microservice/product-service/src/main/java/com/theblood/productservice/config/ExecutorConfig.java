package com.theblood.productservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration

public class ExecutorConfig {

    @Bean(name = "uploadFileExecutor")
    public ExecutorService uploadFileExecutor() {
        return new ThreadPoolExecutor(
                1,
                3,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(50),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
