package com.example.SpringMate.Auth.Helper;

import com.example.SpringMate.Auth.Entity.Session;
import com.example.SpringMate.Auth.Repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionHelper {

    private final SessionRepository sessionRepository;

    public void updateSession(Session session) {
        try {
            session.setLastAccessedAt(LocalDateTime.now());
            sessionRepository.save(session);

        } catch (Exception e) {
            log.error(
                    "SESSION_LAST_ACCESSED_UPDATE_FAILED sessionId={}",
                    session.getSessionID(),
                    e
            );
        }
    }

}
