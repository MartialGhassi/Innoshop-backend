package com.innowise.academy.userservice.config;

import com.innowise.academy.userservice.model.dto.UserDetailsResponse;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

/**
 * Enables Spring's cache abstraction and configures the Redis caches.
 *
 * <p>Cached values are stored as JSON, so they stay readable in Redis and do not depend on
 * Java serialization. The cache manager is transaction-aware: puts and evictions made inside
 * a transaction are applied only after it commits.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Name of the cache holding {@link UserDetailsResponse} entries, keyed by user id.
     */
    public static final String USERS_CACHE = "users";

    /**
     * Configures the {@value #USERS_CACHE} cache with a JSON serializer and the time-to-live
     * set in {@code spring.cache.redis.time-to-live}, and makes the cache manager
     * transaction-aware.
     *
     * @param cacheProperties the {@code spring.cache.*} properties
     * @return the customizer applied to the auto-configured Redis cache manager
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer usersCacheCustomizer(CacheProperties cacheProperties) {
        RedisCacheConfiguration usersCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(cacheProperties.getRedis().getTimeToLive())
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(UserDetailsResponse.class)));
        return builder -> builder.withCacheConfiguration(USERS_CACHE, usersCacheConfig)
                .transactionAware();
    }
}
