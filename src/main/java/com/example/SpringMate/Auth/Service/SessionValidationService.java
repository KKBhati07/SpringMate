package com.example.SpringMate.Auth.Service;

import com.example.SpringMate.Auth.Cache.AuthCacheService;
import com.example.SpringMate.Auth.Cache.CachedAuthentication;
import com.example.SpringMate.Auth.Entity.Session;
import com.example.SpringMate.Auth.Helper.SessionHelper;
import com.example.SpringMate.Auth.Helper.SessionManagementHelper;
import com.example.SpringMate.Auth.Repository.SessionRepository;
import com.example.SpringMate.Shared.Exception.UnauthorizedException;
import com.example.SpringMate.User.Entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionValidationService {

    private final AuthCacheService authCacheService;
    private final SessionRepository sessionRepository;
    private final SessionHelper sessionHelper;
    private final SessionManagementHelper sessionManagementHelper;

    /**
     * Single source of truth for auth.
     */
    public CachedAuthentication validate(String sessionId) {
        CachedAuthentication cached = authCacheService.get(sessionId);
        if (cached != null) {
            log.info("AUTH_CACHE_HIT sessionId={} userUuid={}",
                    sessionId, cached.getUserUuid());

            sessionHelper.updateSessionAccessTimestamp(
                    sessionId, LocalDateTime.now());

            return cached;
        }
        log.info("AUTH_CACHE_MISS sessionId={}", sessionId);

        Optional<Session> sessionOpt =
                sessionRepository.findBySessionId(sessionId);

        if (sessionOpt.isEmpty()) {
            log.warn("AUTH_SESSION_NOT_FOUND sessionId={}", sessionId);
            throw new UnauthorizedException("Session not found");
        }

        Session session = sessionOpt.get();

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            sessionRepository.delete(session);
            sessionManagementHelper.updateSessionLogoutTime(sessionId);

            log.warn("AUTH_SESSION_EXPIRED sessionId={} userUuid={}",
                    sessionId, session.getUser().getUuid());

            throw new UnauthorizedException("Session expired");
        }
        User user = session.getUser();

        sessionHelper.updateSessionAccessTimestamp(
                session, LocalDateTime.now());

        authCacheService.cacheAuthenticatedUser(
                sessionId, user, session.getExpiresAt());

        return authCacheService.get(sessionId);
    }
}
