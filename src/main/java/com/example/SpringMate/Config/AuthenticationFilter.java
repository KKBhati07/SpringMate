package com.example.SpringMate.Config;

import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Helpers.SessionManagementHelper;
import com.example.SpringMate.Repositoy.SessionRepository;
import com.example.SpringMate.Repositoy.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public AuthenticationFilter(AuthenticationManager authenticationManager, SessionRepository sessionRepository, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {

        try {
            Map<String, String> requestBody = new ObjectMapper().readValue(req.getInputStream(), Map.class);
            String email = requestBody.get("email");
            String password = requestBody.get("password");
            if (email == null || password == null || password.isBlank() || email.isBlank()) {
                throw new BadCredentialsException("Invalid email or password");
            }
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
            return authenticationManager.authenticate(authenticationToken);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        SessionManagementHelper sessionManagementHelper = new SessionManagementHelper(userRepository, sessionRepository);
        Session existingSession = sessionManagementHelper.checkIfSessionExists(authResult.getName());
        String sessionId;
        if (existingSession == null) {
            sessionId = sessionManagementHelper.createSession(authResult.getName());
        } else sessionId = existingSession.getSessionID();

        response.setContentType("application/json");
        response.getWriter().write("{\"sessionId\": \"" + sessionId + "\", \"message\": \"Login successful\"}");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Authentication failed: " + failed.getMessage());
        response.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));
    }
}
