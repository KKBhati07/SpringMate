package com.example.SpringMate.Helpers;
import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Repositoy.SessionRepository;
import com.example.SpringMate.Repositoy.UserRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SessionManagementHelper {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    public Session checkIfSessionExists(String email) {
        User user = userRepository.findByEmail(email);
        Session list = sessionRepository.findByUserId(user.getId());
        System.out.println("List>>>>"+list.toString());
        return null;
    }

    public String createSession(String email) {
        User user = userRepository.findByEmail(email);
        String sessionId = CoreHelper.generateUUID();
        Session session = new Session(sessionId, user, System.currentTimeMillis(), System.currentTimeMillis());
        sessionRepository.save(session);
        return sessionId;
    }



}
