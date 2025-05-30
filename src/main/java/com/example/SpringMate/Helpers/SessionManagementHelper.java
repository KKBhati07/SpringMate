package com.example.SpringMate.Helpers;

import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Repositoy.SessionRepository;
import com.example.SpringMate.Repositoy.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
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
        return userRepository.findByEmail(email)
                .map(user -> {
                    String sessionId = CoreHelper.generateUUID().toUpperCase();
                    Session session = new Session(sessionId, user, System.currentTimeMillis(), System.currentTimeMillis());
                    sessionRepository.save(session);
                    return sessionId;
                })
                .orElse(null);
    }
}
