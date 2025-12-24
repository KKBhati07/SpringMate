package com.example.SpringMate.Filter;

import com.example.SpringMate.Auth.Entity.Session;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.Auth.Helper.SessionHelper;
import com.example.SpringMate.Auth.Repository.SessionRepository;
import com.example.SpringMate.Config.JwtTokenProvider;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.Util.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionRepository sessionRepository;
    private final SessionHelper sessionHelper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthHelper authHelper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        String authToken = extractToken(request);

        if (authToken != null) {

            try {
                if (!jwtTokenProvider.validateToken(authToken)) {
                    log.warn("AUTH_INVALID_TOKEN");
                    authHelper.clearAuthCookie(response);
                    sendUnauthorized(response, "Invalid or expired token!");
                    return;
                }

            } catch (JwtException e) {
                log.warn("AUTH_TOKEN_EXCEPTION");
                authHelper.clearAuthCookie(response);
                sendUnauthorized(response, "Invalid token!");
                return;
            }

            String sessionId = jwtTokenProvider.getSessionIdFromToken(authToken);
            Optional<Session> sessionOpt = sessionRepository.findBySessionID(sessionId);

            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
                    authHelper.clearAuthCookie(response);
                    log.warn("AUTH_SESSION_EXPIRED");
                    sendUnauthorized(response, "Session expired. Please log in again.");
                    return;
                }
                sessionHelper.updateSession(session);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        session.getUser(),
                        null,
                        session.getUser().getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("AUTH_SESSION_NOT_FOUND");
                authHelper.clearAuthCookie(response);
                sendUnauthorized(response, "Session expired. Please log in again.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return Arrays.stream(Urls.FILTER_EXCLUDED_ENDPOINTS)
                .anyMatch(pattern ->
                        pathMatcher.match(pattern, path)
                );
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            return authHeader.split(" ")[1];
        }
        Cookie cookie = WebUtils.getCookie(request, "auth_token");
        return cookie != null ? cookie.getValue() : null;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Response<Void> res = new Response<>(null, message);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(res));
    }
}
