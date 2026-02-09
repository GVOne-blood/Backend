package com.theblood.springfood.actionlog.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.config.JHipsterConstants;
import tech.jhipster.config.JHipsterProperties;
import tech.jhipster.config.apidoc.customizer.JHipsterOpenApiCustomizer;

@Configuration
public class OpenApiConfiguration {

    public static final String API_FIRST_PACKAGE = "com.theblood.springfood.actionlog.web.**";

    @Bean
    @ConditionalOnMissingBean(name = "apiFirstGroupedOpenAPI")
    public GroupedOpenApi apiFirstGroupedOpenAPI(
            JHipsterOpenApiCustomizer jhipsterOpenApiCustomizer,
            JHipsterProperties jHipsterProperties
    ) {
        JHipsterProperties.ApiDocs properties = jHipsterProperties.getApiDocs();
        return GroupedOpenApi.builder()
                .group("openapi")
                .addOpenApiCustomizer(jhipsterOpenApiCustomizer)
                .packagesToScan(API_FIRST_PACKAGE)
                .pathsToMatch(properties.getDefaultIncludePattern())
                .build();
    }
}
