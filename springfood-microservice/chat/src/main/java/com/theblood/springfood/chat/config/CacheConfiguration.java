package com.theblood.springfood.chat.config;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import org.redisson.Redisson;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.config.JHipsterProperties;
import tech.jhipster.config.cache.PrefixedKeyGenerator;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(CacheConfiguration.class);

    private GitProperties gitProperties;
    private BuildProperties buildProperties;

    /**
     * Simple in-memory cache when Redis is disabled
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "simple", matchIfMissing = false)
    public CacheManager simpleCacheManager() {
        LOG.info("Using Simple In-Memory Cache (Redis disabled)");
        return new ConcurrentMapCacheManager(
            com.theblood.springfood.chat.domain.Conversation.class.getName(),
            com.theblood.springfood.chat.domain.ConversationParticipant.class.getName(),
            com.theblood.springfood.chat.domain.Message.class.getName(),
            com.theblood.springfood.chat.domain.MessageAttachment.class.getName(),
            com.theblood.springfood.chat.domain.MessageReadReceipt.class.getName(),
            com.theblood.springfood.chat.domain.MessageReaction.class.getName(),
            com.theblood.springfood.chat.domain.UserPresence.class.getName(),
            com.theblood.springfood.chat.domain.TypingIndicator.class.getName(),
            com.theblood.springfood.chat.domain.BlockedUser.class.getName(),
            com.theblood.springfood.chat.domain.MessageReport.class.getName(),
            com.theblood.springfood.chat.domain.ConversationSettings.class.getName()
        );
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration(JHipsterProperties jHipsterProperties) {
        MutableConfiguration<Object, Object> jcacheConfig = new MutableConfiguration<>();

        URI redisUri = URI.create(jHipsterProperties.getCache().getRedis().getServer()[0]);

        Config config = new Config();
        // Fix Hibernate lazy initialization https://github.com/jhipster/generator-jhipster/issues/22889
        config.setCodec(new org.redisson.codec.SerializationCodec());
        if (jHipsterProperties.getCache().getRedis().isCluster()) {
            ClusterServersConfig clusterServersConfig = config
                .useClusterServers()
                .setMasterConnectionPoolSize(jHipsterProperties.getCache().getRedis().getConnectionPoolSize())
                .setMasterConnectionMinimumIdleSize(jHipsterProperties.getCache().getRedis().getConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(jHipsterProperties.getCache().getRedis().getSubscriptionConnectionPoolSize())
                .addNodeAddress(jHipsterProperties.getCache().getRedis().getServer());

            if (redisUri.getUserInfo() != null) {
                clusterServersConfig.setPassword(redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(':') + 1));
            }
        } else {
            SingleServerConfig singleServerConfig = config
                .useSingleServer()
                .setConnectionPoolSize(jHipsterProperties.getCache().getRedis().getConnectionPoolSize())
                .setConnectionMinimumIdleSize(jHipsterProperties.getCache().getRedis().getConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(jHipsterProperties.getCache().getRedis().getSubscriptionConnectionPoolSize())
                .setAddress(jHipsterProperties.getCache().getRedis().getServer()[0]);

            if (redisUri.getUserInfo() != null) {
                singleServerConfig.setPassword(redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(':') + 1));
            }
        }
        jcacheConfig.setStatisticsEnabled(true);
        jcacheConfig.setExpiryPolicyFactory(
            CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, jHipsterProperties.getCache().getRedis().getExpiration()))
        );
        return RedissonConfiguration.fromInstance(Redisson.create(config), jcacheConfig);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public JCacheManagerCustomizer cacheManagerCustomizer(javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration) {
        return cm -> {
            createCache(cm, com.theblood.springfood.chat.domain.Conversation.class.getName(), jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.Conversation.class.getName() + ".participants", jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.Conversation.class.getName() + ".messages", jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.ConversationParticipant.class.getName(), jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.Message.class.getName(), jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.Message.class.getName() + ".attachments", jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.Message.class.getName() + ".readReceipts", jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.Message.class.getName() + ".reactions", jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.MessageAttachment.class.getName(), jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.MessageReadReceipt.class.getName(), jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.MessageReaction.class.getName(), jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.UserPresence.class.getName(), jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.TypingIndicator.class.getName(), jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.BlockedUser.class.getName(), jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.MessageReport.class.getName(), jcacheConfiguration);
            createCache(cm, com.theblood.springfood.chat.domain.ConversationSettings.class.getName(), jcacheConfiguration);
            // jhipster-needle-redis-add-entry
        };
    }

    private void createCache(
        javax.cache.CacheManager cm,
        String cacheName,
        javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration
    ) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }

    @Autowired(required = false)
    public void setGitProperties(GitProperties gitProperties) {
        this.gitProperties = gitProperties;
    }

    @Autowired(required = false)
    public void setBuildProperties(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new PrefixedKeyGenerator(this.gitProperties, this.buildProperties);
    }
}
