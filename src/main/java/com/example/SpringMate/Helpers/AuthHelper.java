package com.example.SpringMate.Helpers;

import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Entity.User;
import com.example.SpringMate.Repositoy.SessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@NoArgsConstructor
public class AuthHelper {

    public static String failureResponse(String message, String status) throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put("message", message);
        map.put("status", status);
        return new ObjectMapper().writeValueAsString(map);
    }

    public User getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getPrincipal() instanceof User ? (User) authentication.getPrincipal() : null;
    }

    public boolean compareUserDetails(User user, User authenticatedUser) {
        if (authenticatedUser == null) return false;
        return authenticatedUser.getEmail().equals(user.getEmail())
                && authenticatedUser.getPassword().equals(user.getPassword())
                && authenticatedUser.getRole().getName().equals(user.getRole().getName())
                && authenticatedUser.getUuid().equals(user.getUuid());
    }

    public boolean isSelfUUID(String uuid, User authenticatedUser) {
        return uuid.equals(authenticatedUser.getUuid());
    }
}
