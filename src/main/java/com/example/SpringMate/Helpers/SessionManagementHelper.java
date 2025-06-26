package com.example.SpringMate.Helpers;

import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Entity.SessionLog;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Repositoy.SessionLogRepository;
import com.example.SpringMate.Repositoy.SessionRepository;
import com.example.SpringMate.Repositoy.UserRepository;
import com.example.SpringMate.Util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionManagementHelper {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final SessionLogRepository sessionLogRepository;

    public Session checkIfSessionExists(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) return null;
        List<Session> sessions = sessionRepository.findByUserId(user.get().getId());
        return !sessions.isEmpty() ? sessions.get(sessions.size() - 1) : null;
    }

    public String getUserAndCreateSession(String email, HttpServletRequest request){
        return userRepository.findByEmail(email)
                .map(user -> createSession(user,request))
                .orElse(null);
    }

    public String createSession(User user, HttpServletRequest request) {
        String sessionId = CoreHelper.generateUUID().toUpperCase();
        Session session = Session.builder()
                .sessionID(sessionId)
                .user(user)
                .createdAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(Constants.SESSION_VALIDITY))
                .build();
        Session createdSession = sessionRepository.save(session);
        createSessionLog(createdSession.getCreatedAt(),user, sessionId, request);
        return createdSession.getSessionID();
//        return userRepository.findByEmail(email)
//                .map(user -> {
//                    String sessionId = CoreHelper.generateUUID().toUpperCase();
//                    Session session = Session.builder()
//                            .sessionID(sessionId)
//                            .user(user)
//                            .createdAt(LocalDateTime.now())
//                            .lastAccessedAt(LocalDateTime.now())
//                            .expiresAt(LocalDateTime.now().plusDays(Constants.SESSION_VALIDITY))
//                            .build();
//                    Session createdSession = sessionRepository.save(session);
//                    createSessionLog(createdSession.getCreatedAt(),user, sessionId, request);
//                    return createdSession.getSessionID();
//                })
//                .orElse(null);
    }
    public String createSession(String email, HttpServletRequest request) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    String sessionId = CoreHelper.generateUUID().toUpperCase();
                    Session session = Session.builder()
                            .sessionID(sessionId)
                            .user(user)
                            .createdAt(LocalDateTime.now())
                            .lastAccessedAt(LocalDateTime.now())
                            .expiresAt(LocalDateTime.now().plusDays(Constants.SESSION_VALIDITY))
                            .build();
                    Session createdSession = sessionRepository.save(session);
                    createSessionLog(createdSession.getCreatedAt(),user, sessionId, request);
                    return createdSession.getSessionID();
                })
                .orElse(null);
    }

    private boolean createSessionLog(LocalDateTime loginAt, User user,
                                     String sessionId, HttpServletRequest request) {
        try {
            String ipAddress = getClientIp(request);

            SessionLog sessionLog = SessionLog.builder()
                    .loginAt(loginAt)
                    .user(user)
                    .sessionId(sessionId)
                    .ipAddress(ipAddress)
                    .userAgent(request.getHeader("User-Agent"))
                    .build();

            sessionLogRepository.save(sessionLog);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isEmpty() && !"unknown".equalsIgnoreCase(header)) {
            return header.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
