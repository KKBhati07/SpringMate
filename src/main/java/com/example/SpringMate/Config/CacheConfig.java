package com.example.SpringMate.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.SerializationException;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                if (exception instanceof SerializationException) {
                    log.warn(
                            "Cache deserialization error detected, evicting stale entry cache={} key={}",
                            cache.getName(),
                            key,
                            exception
                    );
                    cache.evict(key);
                } else {
                    log.error(
                            "Cache get error cache={} key={}",
                            cache.getName(),
                            key,
                            exception
                    );
                }
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.error(
                        "Cache put error cache={} key={}",
                        cache.getName(),
                        key,
                        exception
                );
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error(
                        "Cache evict error cache={} key={}",
                        cache.getName(),
                        key,
                        exception
                );
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error(
                        "Cache clear error cache={}",
                        cache.getName(),
                        exception
                );
            }
        };
    }
}
