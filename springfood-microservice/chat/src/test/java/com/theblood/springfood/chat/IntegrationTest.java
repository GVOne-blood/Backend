package com.theblood.springfood.chat;

import com.theblood.springfood.chat.config.AsyncSyncConfiguration;
import com.theblood.springfood.chat.config.EmbeddedRedis;
import com.theblood.springfood.chat.config.EmbeddedSQL;
import com.theblood.springfood.chat.config.JacksonConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { ChatApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class })
@EmbeddedRedis
@EmbeddedSQL
public @interface IntegrationTest {
}
