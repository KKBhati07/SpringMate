package com.example.SpringMate.Helpers;

import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Repositoy.SessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

public class AuthHelper {


    private static SessionRepository sessionRepository;

    public static String failureResponse(String message, String status) throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put("message", message);
        map.put("status", status);
        return new ObjectMapper().writeValueAsString(map);
    }

    public static void setSessionRepository(SessionRepository sessionRepository) {
        AuthHelper.sessionRepository = sessionRepository;
    }

    public static boolean updateSession(Session session){
        try{
            session.setLastAccessedAt(System.currentTimeMillis());
            sessionRepository.save(session);
            return true;

        }catch (Exception e){
            return false;
        }
    }

    public User getUserDetails(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getPrincipal() instanceof User ? (User) authentication.getPrincipal() : null;
    }

    public boolean compareUserDetails(User user){
        User authenticatedUser = getUserDetails();
        if(authenticatedUser == null) return false;
        return authenticatedUser.getEmail().equals(user.getEmail())
                && authenticatedUser.getPassword().equals(user.getPassword())
                && authenticatedUser.getRole().equals(user.getRole())
                && authenticatedUser.getUuid().equals(user.getUuid());
    }
}
