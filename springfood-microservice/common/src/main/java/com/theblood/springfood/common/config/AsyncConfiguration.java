package com.theblood.springfood.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration to enable async method execution.
 * Required for async email sending in EmailServiceImpl.
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {
}
