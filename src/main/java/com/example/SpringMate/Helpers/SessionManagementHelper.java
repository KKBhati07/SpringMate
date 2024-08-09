package com.example.SpringMate.Helpers;
import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Repositoy.SessionRepository;
import com.example.SpringMate.Repositoy.UserRepository;
import lombok.AllArgsConstructor;
import java.util.List;

@AllArgsConstructor
public class SessionManagementHelper {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    public Session checkIfSessionExists(String email) {
        User user = userRepository.findByEmail(email);
        List<Session> sessions = sessionRepository.findByUserId(user.getId());
        return !sessions.isEmpty() ? sessions.get(sessions.size()-1) : null;
    }

    public String createSession(String email) {
        User user = userRepository.findByEmail(email);
        String sessionId = CoreHelper.generateUUID().toUpperCase();
        Session session = new Session(sessionId, user, System.currentTimeMillis(), System.currentTimeMillis());
        sessionRepository.save(session);
        return sessionId;
    }
}
