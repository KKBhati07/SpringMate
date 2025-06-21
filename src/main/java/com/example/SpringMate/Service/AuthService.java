package com.example.SpringMate.Service;


import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Repositoy.SessionLogRepository;
import com.example.SpringMate.Repositoy.SessionRepository;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Util.ResponseMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SessionRepository sessionRepository;
    private final SessionLogRepository sessionLogRepository;
    private final ResponseMapper responseMapper;

    @Transactional
    public ResponseEntity<Response> logoutUser(String sessionId) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            Optional<Session> sessionOpt = sessionRepository.findBySessionID(sessionId);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                sessionRepository.delete(session);
                sessionLogRepository.updateLogoutTime(session.getSessionID(), LocalDateTime.now());

                SecurityContextHolder.clearContext();
                responseMap.put("status", HttpStatus.OK.value());
                return ResponseEntity.ok(new Response(responseMap, "Logged out successfully"));
            }
            responseMap.put("status", 400);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response(responseMap, "Bad Request"));

        } catch (Exception e) {
            e.printStackTrace();
            responseMap.put("status", 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new Response(responseMap, "Something went wrong"));
        }
    }

    public ResponseEntity<Response> authDetails(User authenticateUser) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            responseMap.put("status", HttpStatus.OK.value());
            responseMap.put("is_authenticated", true);
            responseMap.put("user_details", responseMapper
                    .mapUser(authenticateUser));
            return ResponseEntity.ok(new Response(responseMap, "Data fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new Response(responseMap, "Internal server Error"));

        }

    }
}
