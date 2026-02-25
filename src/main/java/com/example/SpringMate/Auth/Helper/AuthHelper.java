package com.example.SpringMate.Auth.Helper;

import com.example.SpringMate.Config.AppProperties;
import com.example.SpringMate.User.Entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AuthHelper {

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    public String failureResponse(String message, String status) throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put("message", message);
        map.put("status", status);
        return objectMapper.writeValueAsString(map);
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

    /**
     * Generates 6-digit OTP for authentication.
     * Range 100000-999999 to ensure consistent length and prevent leading zeros.
     */
    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public void clearAuthCookie(HttpServletResponse response) {
        AppProperties.Cookie cookieConfig = appProperties.getCookie();
        injectCookie(response,
                "auth_token",
                "",
                Duration.ZERO,
                cookieConfig.getDomain(),
                cookieConfig.isSecure());
    }

    public void injectAuthCookie(HttpServletResponse response, String authToken) {
        AppProperties.Cookie cookieConfig = appProperties.getCookie();
        int jwtValidityDays = appProperties.getAuth().getJwt().getValidityDays();

        injectCookie(response,
                "auth_token",
                authToken,
                Duration.ofDays(jwtValidityDays),
                cookieConfig.getDomain(),
                cookieConfig.isSecure());
    }

    /**
     * Sets authentication cookie with security attributes.
     * httpOnly disabled to allow client-side access for SPA authentication flow.
     */
    private void injectCookie(HttpServletResponse response,
                              String name,
                              String value,
                              Duration maxAge,
                              String domain,
                              boolean secure) {
        // Jakarta Cookie does not support sameSite attribute, hence will be blocked by browser in cross site
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .path(appProperties.getCookie().getPath())
                .maxAge(maxAge)
//                .sameSite("None")    // required for cross-site cookies // Not required anymore as local setup is samesite now
                .domain(domain)
                .secure(secure)
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
