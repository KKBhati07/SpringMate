package com.example.SpringMate.Helpers;

import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Repositoy.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionHelper {

    private final SessionRepository sessionRepository;

    @Autowired
    public SessionHelper(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public boolean updateSession(Session session) {
        try {
            session.setLastAccessedAt(System.currentTimeMillis());
            sessionRepository.save(session);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

}
