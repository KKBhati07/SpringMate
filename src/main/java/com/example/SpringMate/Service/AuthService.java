package com.example.SpringMate.Service;


import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Helpers.AuthHelper;
import com.example.SpringMate.Repositoy.SessionRepository;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Util.ResponseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private final SessionRepository sessionRepository;
    private final AwsS3Service awsS3Service;

    @Autowired
    public AuthService(SessionRepository sessionRepository, AwsS3Service awsS3Service) {
        this.sessionRepository = sessionRepository;
        this.awsS3Service = awsS3Service;
    }

    public ResponseEntity<Response> logoutUser() {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();
            List<Session> sessions = sessionRepository.findByUserId(user.getId());
            sessionRepository.deleteAll(sessions);
            SecurityContextHolder.clearContext();
            responseMap.put("status", 200);
            return ResponseEntity.ok(new Response(responseMap, "Logged out successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            responseMap.put("status", 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(responseMap, "Something went wrong"));
        }
    }

    public ResponseEntity<Response> authDetails() {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            responseMap.put("status", 200);
            responseMap.put("is_authenticated", true);
            responseMap.put("user_details", new ResponseMapper(awsS3Service)
                    .mapUser(new AuthHelper().getUserDetails()));
            return ResponseEntity.ok(new Response(responseMap, "Data fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(responseMap, "Internal server Error"));

        }

    }
}
