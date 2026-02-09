package com.theblood.springfood.media;

import com.theblood.springfood.media.config.AsyncSyncConfiguration;
import com.theblood.springfood.media.config.EmbeddedRedis;
import com.theblood.springfood.media.config.EmbeddedSQL;
import com.theblood.springfood.media.config.JacksonConfiguration;
import com.theblood.springfood.media.config.TestSecurityConfiguration;
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
@SpringBootTest(classes = { MediaApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class, TestSecurityConfiguration.class })
@EmbeddedRedis
@EmbeddedSQL
public @interface IntegrationTest {
}
