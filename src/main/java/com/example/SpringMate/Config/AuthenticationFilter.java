package com.example.SpringMate.Config;

import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Helpers.SessionManagementHelper;
import com.example.SpringMate.Repositoy.SessionRepository;
import com.example.SpringMate.Repositoy.UserRepository;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Util.ResponseMapper;
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
            if (email == null || password == null || password.isBlank() || email.isBlank() || userRepository.findByEmail(email) == null) {
                throw new BadCredentialsException("Bad credentials");
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
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("sessionId", sessionId);
        resMap.put("authenticated",true);
        resMap.put("user_details",new ResponseMapper().mapUser(userRepository.findByEmail(authResult.getName())));
        Response res=new Response(resMap,"Logged in successfully!");
        response.getWriter().write(new ObjectMapper().writeValueAsString(res));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("authenticated",false);
        Response res=new Response(resMap,"Authentication failed: " + failed.getMessage());
        response.getWriter().write(new ObjectMapper().writeValueAsString(res));
    }
}
