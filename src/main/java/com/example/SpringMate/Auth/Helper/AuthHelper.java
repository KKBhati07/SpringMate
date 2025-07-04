package com.example.SpringMate.Auth.Helper;

import com.example.SpringMate.User.Entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

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

    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public boolean isSelfUUID(UUID uuid, User authenticatedUser) {
        return uuid.equals(authenticatedUser.getUuid());
    }
}
