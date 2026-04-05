package com.theblood.springfood.media.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class FileExecutor {

    @Bean(name = "uploadFileExecutor")
    public ThreadPoolExecutor threadPoolProcessFile() {
        int coreCount = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
            coreCount * 2,
            coreCount * 4,
            60L,
            java.util.concurrent.TimeUnit.SECONDS,
            new java.util.concurrent.LinkedBlockingQueue<>(200),
            new ThreadFactoryBuilder()
                .setNameFormat("file-processor-%d")
                .setDaemon(true)
                .build(),
            new ThreadPoolExecutor.CallerRunsPolicy() //reject policy
        );
    }
    

}
