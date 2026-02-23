package com.example.SpringMate.Config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RedisCacheManagerConfig {

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            GenericJackson2JsonRedisSerializer serializer
    ) {
        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer())
                        )
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(serializer)
                        )
                        .entryTtl(Duration.ofMinutes(30));

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .build();

        return new ErrorTolerantCacheManager(redisCacheManager);
    }


    /**
     * Wraps RedisCacheManager to return ErrorTolerantRedisCache instances
     * that handle SerializationException gracefully.
     */
    private static class ErrorTolerantCacheManager implements CacheManager {
        private final RedisCacheManager delegate;
        private final Map<String, Cache> cacheMap = new ConcurrentHashMap<>();

        public ErrorTolerantCacheManager(RedisCacheManager delegate) {
            this.delegate = delegate;
        }

        @Override
        public Cache getCache(String name) {
            return cacheMap.computeIfAbsent(name, cacheName -> {
                Cache cache = delegate.getCache(cacheName);
                if (cache instanceof RedisCache) {
                    return new ErrorTolerantRedisCache((RedisCache) cache);
                }
                return cache;
            });
        }

        @Override
        public Collection<String> getCacheNames() {
            return delegate.getCacheNames();
        }
    }
}
