package com.example.SpringMate.Auth.Filter;

import com.example.SpringMate.Auth.Cache.AuthCacheService;
import com.example.SpringMate.Auth.Cache.CachedAuthentication;
import com.example.SpringMate.Auth.Helper.SessionManagementHelper;
import com.example.SpringMate.Util.AuthenticatedUser;
import com.example.SpringMate.Auth.Entity.Session;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.Auth.Helper.SessionHelper;
import com.example.SpringMate.Auth.Repository.SessionRepository;
import com.example.SpringMate.Auth.jwt.JwtTokenProvider;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.User.Entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionRepository sessionRepository;
    private final SessionHelper sessionHelper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthHelper authHelper;
    private final AuthCacheService authCacheService;
    private final SessionManagementHelper sessionManagementHelper;
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
            CachedAuthentication cachedAuthentication = authCacheService.get(sessionId);

            if (cachedAuthentication != null) {
                log.info(
                        "AUTH_CACHE_HIT sessionId={} user=[ UUID {}]",
                        sessionId,
                        cachedAuthentication.getUserUuid()
                );
                sessionHelper.updateSessionAccessTimestamp(sessionId, LocalDateTime.now());
                SecurityContextHolder.getContext().setAuthentication(
                        getAuthentication(
                                cachedAuthentication.getUserId(),
                                cachedAuthentication.getUserUuid(),
                                cachedAuthentication.getEmail(),
                                cachedAuthentication.getName(),
                                cachedAuthentication.isAdmin(),
                                cachedAuthentication.getAuthorities()
                        )
                );
                filterChain.doFilter(request, response);
                return;
            }
            log.info("AUTH_CACHE_MISS sessionId={}", sessionId);
            Optional<Session> sessionOpt = sessionRepository.findBySessionId(sessionId);

            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
                    authHelper.clearAuthCookie(response);
                    sessionRepository.delete(session);
                    sessionManagementHelper.updateSessionLogoutTime(sessionId);
                    log.warn(
                            "AUTH_SESSION_EXPIRED sessionId={} user=[ UUID {}]",
                            sessionId,
                            session.getUser().getUuid()
                    );
                    log.warn("AUTH_SESSION_EXPIRED");
                    sendUnauthorized(response, "Session expired. Please log in again.");
                    return;
                }
                User user = session.getUser();
                sessionHelper.updateSessionAccessTimestamp(session, LocalDateTime.now());
                authCacheService.cacheAuthenticatedUser(sessionId, user, session.getExpiresAt());
                SecurityContextHolder.getContext().setAuthentication(getAuthentication(user));

            } else {
                log.warn("AUTH_SESSION_NOT_FOUND sessionId={}", sessionId);
                authHelper.clearAuthCookie(response);
                sendUnauthorized(response, "Session expired. Please log in again.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @NotNull
    public static Authentication getAuthentication(
            Long id,
            UUID uuid,
            String email,
            String name,
            boolean isAdmin,
            List<String> authorities
    ) {
        AuthenticatedUser principal =
                new AuthenticatedUser(id, uuid, email, name, isAdmin);

        List<GrantedAuthority> grantedAuthorities =
                authorities.stream()
                        .map(a -> (GrantedAuthority) new SimpleGrantedAuthority(a))
                        .toList();


        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                grantedAuthorities
        );
    }

    @NotNull
    public static Authentication getAuthentication(
            User user
    ) {
        AuthenticatedUser principal =
                new AuthenticatedUser(
                        user.getId(),
                        user.getUuid(),
                        user.getEmail(),
                        user.getName(),
                        user.isAdmin()
                );

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                user.getAuthorities()
        );
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return Arrays.stream(Urls.Security.FILTER_EXCLUDED_ENDPOINTS)
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
        Response<Void> res = Response.error(message);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(res));
    }
}
