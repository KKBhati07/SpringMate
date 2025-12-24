package com.example.SpringMate.Auth.Helper;

import com.example.SpringMate.Auth.Entity.Session;
import com.example.SpringMate.Auth.Entity.SessionLog;
import com.example.SpringMate.Shared.Helper.CoreHelper;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Auth.Repository.SessionLogRepository;
import com.example.SpringMate.Auth.Repository.SessionRepository;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.User.Service.CoreUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionManagementHelper {

    private final CoreUserService coreUserService;
    private final SessionRepository sessionRepository;
    private final SessionLogRepository sessionLogRepository;

    public Session checkIfSessionExists(String email) {
        User user = coreUserService.getUserByEmail(email);
        if (user == null ) return null;
        List<Session> sessions = sessionRepository.findByUserId(user.getId());
        return !sessions.isEmpty() ? sessions.get(sessions.size() - 1) : null;
    }

    public String getUserAndCreateSession(String email, HttpServletRequest request){
        User user = coreUserService.getUserByEmail(email);
        return user != null ? createSession(user,request) : null;
    }

    public String createSession(User user, HttpServletRequest request) {
        String sessionId = CoreHelper.generateUUID().toString().toUpperCase();
        Session session = Session.builder()
                .sessionID(sessionId)
                .user(user)
                .createdAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(Constants.SESSION_VALIDITY))
                .build();
        Session createdSession = sessionRepository.save(session);
        createSessionLog(createdSession.getCreatedAt(),user, sessionId, request);
        log.info(
                "SESSION_CREATED user=[UUID {}]",
                user.getUuid()
        );
        return createdSession.getSessionID();
    }
    public String createSession(String email, HttpServletRequest request) {
        User user = coreUserService.getUserByEmail(email);
        if(user == null) return null;
        String sessionId = CoreHelper.generateUUID().toString().toUpperCase();
        Session session = Session.builder()
                .sessionID(sessionId)
                .user(user)
                .createdAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(Constants.SESSION_VALIDITY))
                .build();
        Session createdSession = sessionRepository.save(session);
        createSessionLog(createdSession.getCreatedAt(),user, sessionId, request);
        log.info(
                "SESSION_CREATED user=[UUID {}]",
                user.getUuid()
        );
        return createdSession.getSessionID();
    }

    private void createSessionLog(LocalDateTime loginAt, User user,
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
        } catch (Exception e) {
            log.info(
                    "Filed to create session log user=[UUID {}]",
                    user.getUuid()
            );
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
