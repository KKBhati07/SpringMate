package com.example.SpringMate.Helpers;

import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Repositoy.SessionRepository;
import com.example.SpringMate.Repositoy.UserRepository;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class SessionManagementHelper {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    public Session checkIfSessionExists(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) return null;
        List<Session> sessions = sessionRepository.findByUserId(user.get().getId());
        return !sessions.isEmpty() ? sessions.get(sessions.size() - 1) : null;
    }

    public String createSession(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        String sessionId = CoreHelper.generateUUID().toUpperCase();
        Session session = new Session(sessionId, user.get(), System.currentTimeMillis(), System.currentTimeMillis());
        sessionRepository.save(session);
        return sessionId;
    }
}
