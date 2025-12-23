package com.example.SpringMate.Config;

import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.Auth.Helper.SessionManagementHelper;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Shared.Exception.UnauthorizedException;
import com.example.SpringMate.Util.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final SessionManagementHelper sessionManagementHelper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthHelper authHelper;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
        try {
            Map<String, String> requestBody = new ObjectMapper().readValue(req.getInputStream(), Map.class);
            String email = requestBody.get("email");
            String password = requestBody.get("password");

            if (email == null || password == null || password.isBlank() || email.isBlank()) {
                throw new UnauthorizedException();
            }
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
            return authenticationManager.authenticate(authenticationToken);

        } catch (IOException e) {
            logger.error("Error processing authentication request", e);
            try {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                res.setContentType("application/json");
                res.getWriter().write(new ObjectMapper().writeValueAsString(
                        Map.of("message", "Something went wrong while processing the request.")
                ));
            } catch (IOException ioException) {
                logger.error("Failed to send error response", ioException);
            }

            return null;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        String appContext = request.getHeader("X-App-Context");
        if(Constants.AppContext.ADMIN.equalsIgnoreCase(appContext)){
            boolean isAdmin = authResult.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ADMIN"));

            if (!isAdmin) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(
                        new ObjectMapper().writeValueAsString(
                                Map.of("message", "Admin access only!")
                        )
                );
                return;
            }
        }

        String sessionId = sessionManagementHelper.getUserAndCreateSession(authResult.getName(), request);
        response.setContentType("application/json");
        String authToken = jwtTokenProvider.generateToken(sessionId);
        authHelper.injectAuthCookie(response, authToken);

        Response<Map<String, Boolean>> res = new Response<>(Map.of("authenticated", true), "Logged in successfully!");
        response.getWriter().write(new ObjectMapper().writeValueAsString(res));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("authenticated", false);
        Response<Void> res = new Response<>(null, "Invalid credentials. Please try again.");
        response.getWriter().write(new ObjectMapper().writeValueAsString(res));
    }
}
