package com.example.SpringMate.Config;

import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Helpers.SessionManagementHelper;
import com.example.SpringMate.Util.Response;
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
import java.util.Optional;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final SessionManagementHelper sessionManagementHelper;

    public AuthenticationFilter(AuthenticationManager authenticationManager,
                                SessionManagementHelper sessionManagementHelper) {
        this.authenticationManager = authenticationManager;
        this.sessionManagementHelper = sessionManagementHelper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
        try {
            Map<String, String> requestBody = new ObjectMapper().readValue(req.getInputStream(), Map.class);
            String email = requestBody.get("email");
            String password = requestBody.get("password");

            if (email == null || password == null || password.isBlank() || email.isBlank()) {
                throw new BadCredentialsException("Bad credentials");
            }
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
            return authenticationManager.authenticate(authenticationToken);

        } catch (IOException e) {
            logger.error("Error processing authentication request", e);
            try {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                res.setContentType("application/json");
                res.getWriter().write(new ObjectMapper().writeValueAsString(
                        Map.of("error", "Internal Server Error", "message", "Something went wrong while processing the request.")
                ));
            } catch (IOException ioException) {
                logger.error("Failed to send error response", ioException);
            }

            return null;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        String sessionId = Optional.ofNullable(sessionManagementHelper.checkIfSessionExists(authResult.getName()))
                .map(Session::getSessionID)
                .orElseGet(() -> sessionManagementHelper.createSession(authResult.getName()));


        response.setContentType("application/json");
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("sessionId", sessionId);
        resMap.put("authenticated",true);
        resMap.put("user_details",authResult.getPrincipal());
        Response res=new Response(resMap,"Logged in successfully!");
        response.getWriter().write(new ObjectMapper().writeValueAsString(res));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("authenticated",false);
        Response res = new Response(resMap, "Invalid credentials. Please try again.");
        response.getWriter().write(new ObjectMapper().writeValueAsString(res));
    }
}
