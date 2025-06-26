package com.example.SpringMate.Helpers;

import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Repositoy.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SessionHelper {

    private final SessionRepository sessionRepository;

    public boolean updateSession(Session session) {
        try {
            session.setLastAccessedAt(LocalDateTime.now());
            sessionRepository.save(session);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

}
