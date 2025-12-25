package com.example.SpringMate.Auth.Helper;

import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.User.Entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Duration;
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

    public void clearAuthCookie(HttpServletResponse response) {
        injectCookie(response,
                "auth_token",
                "",
                Duration.ZERO,
                Constants.COOKIE_DOMAIN);
    }

    public void injectAuthCookie(HttpServletResponse response, String authToken) {
        injectCookie(response,
                "auth_token",
                authToken,
                Duration.ofDays(Constants.JWT_VALIDITY),
                Constants.COOKIE_DOMAIN);
    }

    private void injectCookie(HttpServletResponse response,
                              String name,
                              String value,
                              Duration maxAge,
                              String domain) {
        // Jkarta Cookie does not support sameSite attribute, hence will blocked by browser in cross site
        ResponseCookie cookie = ResponseCookie.from(name, value)
//                .httpOnly(true)
                .path("/")
                .maxAge(maxAge)
//                .sameSite("None")    // required for cross-site cookies // Not required anymore as local setup is samesite now
                .domain(domain)
                .secure(true)        // required for SameSite=None
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }


    public boolean isSelfUUID(UUID uuid, User authenticatedUser) {
        return uuid.equals(authenticatedUser.getUuid());
    }

    public boolean isSelfUUID(UUID uuid, UUID authenticatedUserUUid) {
        return uuid.equals(authenticatedUserUUid);
    }
}
