package com.example.SpringMate.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.concurrent.Callable;

/**
 * Wrapper to handle SerializationException with Redis.
 */
@Slf4j
public class ErrorTolerantRedisCache implements Cache {

    private final RedisCache delegate;

    public ErrorTolerantRedisCache(RedisCache delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        try {
            return delegate.get(key);
        } catch (RuntimeException e) {
            if (isSerializationError(e)) {
                log.warn(
                        "Cache deserialization error for key={} in cache={}. " +
                                "Evicting stale entry and treating as cache miss.",
                        key,
                        getName(),
                        e
                );
                try {
                    delegate.evict(key);
                } catch (Exception evictEx) {
                    log.warn("Failed to evict cache entry", evictEx);
                }
                return null;
            }
            // Re-throw if it's not a serialization error
            throw e;
        }
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        try {
            return delegate.get(key, type);
        } catch (RuntimeException e) {
            if (isSerializationError(e)) {
                log.warn(
                        "Cache deserialization error for key={} in cache={}. " +
                                "Evicting stale entry and treating as cache miss.",
                        key,
                        getName(),
                        e
                );
                try {
                    delegate.evict(key);
                } catch (Exception evictEx) {
                    log.warn("Failed to evict cache entry", evictEx);
                }
                return null;
            }
            throw e;
        }
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        try {
            return delegate.get(key, valueLoader);
        } catch (RuntimeException e) {
            if (isSerializationError(e)) {
                log.warn(
                        "Cache deserialization error for key={} in cache={}. " +
                                "Evicting stale entry and treating as cache miss.",
                        key,
                        getName(),
                        e
                );
                try {
                    delegate.evict(key);
                } catch (Exception evictEx) {
                    log.warn("Failed to evict cache entry", evictEx);
                }
                try {
                    return valueLoader.call();
                } catch (Exception ex) {
                    throw new RuntimeException("Error loading cache value", ex);
                }
            }
            throw e;
        }
    }

    /**
     * Checks if the exception or its cause is a SerializationException
     */
    private boolean isSerializationError(RuntimeException e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof SerializationException) {
                return true;
            }
            // Also check for Jackson's MismatchedInputException which indicates deserialization issues
            if (cause.getClass().getName().contains("MismatchedInputException") ||
                    cause.getMessage() != null && cause.getMessage().contains("type id")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Override
    public void put(Object key, Object value) {
        delegate.put(key, value);
    }

    @Override
    public void evict(Object key) {
        delegate.evict(key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }
}
