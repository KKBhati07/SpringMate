package com.example.SpringMate.Config;
import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Helpers.SessionHelper;
import com.example.SpringMate.Repositoy.SessionRepository;
import com.example.SpringMate.Util.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionRepository sessionRepository;
    private final SessionHelper sessionHelper;

    public SessionAuthenticationFilter(SessionRepository sessionRepository,
                                       SessionHelper sessionHelper) {
        this.sessionRepository = sessionRepository;
        this.sessionHelper = sessionHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        String sessionId = request.getHeader("sessionId");

        if (sessionId != null) {
            Optional<Session> sessionOpt = sessionRepository.findBySessionID(sessionId);

            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", HttpStatus.UNAUTHORIZED.value());
                    map.put("message", "Session expired. Please log in again.");

                    Response res = new Response(map, "Session expired. Please log in again.");
                    response.setContentType("application/json");
                    response.getWriter().write(new ObjectMapper().writeValueAsString(res));
                    return;

                }
                sessionHelper.updateSession(session);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        session.getUser(),
                        null,
                        session.getUser().getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
