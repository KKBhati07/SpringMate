package com.example.SpringMate.Auth.Cache;

import com.example.SpringMate.User.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

//JPA @Cacheable caches entities inside Hibernate.
//Spring @Cacheable caches business results at service level.

@Service
@RequiredArgsConstructor
public class AuthCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "auth:session:";

    public CachedAuthentication get(String sessionId) {
        Object value = redisTemplate.opsForValue().get(KEY_PREFIX + sessionId);
        return (value instanceof CachedAuthentication) ? (CachedAuthentication) value : null;
    }

    public void put(String sessionId, CachedAuthentication authUser, Duration ttl) {
        redisTemplate.opsForValue()
                .set(KEY_PREFIX + sessionId, authUser, ttl);
    }

    public void evict(String sessionId) {
        redisTemplate.delete(KEY_PREFIX + sessionId);
    }

    public void cacheAuthenticatedUser(
            String sessionId,
            User user,
            LocalDateTime sessionExpiresAt
    ) {
        CachedAuthentication cachedAuthentication =
                CachedAuthentication.builder()
                        .userId(user.getId())
                        .userUuid(user.getUuid())
                        .name(user.getName())
                        .email(user.getEmail())
                        .admin(user.isAdmin())
                        .authorities(
                                user.getAuthorities()
                                        .stream()
                                        .map(GrantedAuthority::getAuthority)
                                        .toList()
                        )
                        .build();

        Duration ttl = Duration.between(
                LocalDateTime.now(),
                sessionExpiresAt
        );

        put(sessionId, cachedAuthentication, ttl);
    }
}
