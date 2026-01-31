package com.example.SpringMate.Auth.Helper;

import com.example.SpringMate.Auth.Entity.Session;
import com.example.SpringMate.Auth.Entity.SessionLog;
import com.example.SpringMate.Config.AppProperties;
import com.example.SpringMate.Shared.Helper.CoreHelper;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Auth.Repository.SessionLogRepository;
import com.example.SpringMate.Auth.Repository.SessionRepository;
import com.example.SpringMate.User.Service.CoreUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages user sessions and security audit logging.
 * Tracks IP addresses and user agents for security monitoring.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionManagementHelper {

    private final CoreUserService coreUserService;
    private final SessionRepository sessionRepository;
    private final SessionLogRepository sessionLogRepository;
    private final AppProperties appProperties;

    public Session checkIfSessionExists(String email) {
        User user = coreUserService.getUserByEmail(email);
        if (user == null) return null;
        List<Session> sessions = sessionRepository.findByUserId(user.getId());
        return !sessions.isEmpty() ? sessions.get(sessions.size() - 1) : null;
    }

    public User getUser(String email) {
        return coreUserService.getUserByEmail(email);
    }

    public Session createSession(User user, HttpServletRequest request) {
        String sessionId = CoreHelper.generateUUID().toString().toUpperCase();
        int sessionValidityDays = appProperties.getAuth().getSession().getValidityDays();

        Session session = Session.builder()
                .sessionId(sessionId)
                .user(user)
                .createdAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(sessionValidityDays))
                .build();
        Session createdSession = sessionRepository.save(session);
        createSessionLog(createdSession.getCreatedAt(), user, sessionId, request);
        log.info(
                "SESSION_CREATED user=[UUID {}]",
                user.getUuid()
        );
        return createdSession;
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

    public void updateSessionLogoutTime(String sessionId) {
        try {
            sessionLogRepository.updateLogoutTime(sessionId, LocalDateTime.now());
        } catch (Exception e) {
            log.info(
                    "[SESSION LOG] Filed to update logout time log sessionId=[{}]",
                    sessionId
            );
        }
    }

    /**
     * Extracts client IP considering proxy headers for accurate security logging.
     * Uses first X-Forwarded-For value when present to handle load balancers.
     */
    private String getClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isEmpty() && !"unknown".equalsIgnoreCase(header)) {
            return header.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
