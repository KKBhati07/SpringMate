package com.example.SpringMate.Auth.Helper;

import com.example.SpringMate.Auth.Entity.Session;
import com.example.SpringMate.Auth.Repository.SessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionHelper {

    private final SessionRepository sessionRepository;

    @Async("appDefault")
    public void updateSessionAccessTimestamp(Session session, LocalDateTime timestamp) {
        try {
            session.setLastAccessedAt(timestamp);
            sessionRepository.save(session);

        } catch (Exception e) {
            log.error(
                    "SESSION_LAST_ACCESSED_UPDATE_FAILED sessionId={}",
                    session.getSessionId(),
                    e
            );
        }
    }

    @Async("appDefault")
    @Transactional
    public void updateSessionAccessTimestamp(String sessionId, LocalDateTime timestamp) {
        try {
            sessionRepository.updateLastAccessedAt(sessionId, timestamp);

        } catch (Exception e) {
            log.error(
                    "SESSION_LAST_ACCESSED_UPDATE_FAILED sessionId={}",
                    sessionId,
                    e
            );
        }
    }

}
