package com.example.SpringMate.Auth.Filter;

import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.Auth.Service.SessionValidationService;
import com.example.SpringMate.Auth.jwt.JwtTokenProvider;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.Util.AuthenticatedUser;
import com.example.SpringMate.Util.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthHelper authHelper;
    private final SessionValidationService sessionValidationService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String authToken = extractToken(request);

        if (authToken != null) {
            try {
                if (!jwtTokenProvider.validateToken(authToken)) {
                    authHelper.clearAuthCookie(response);
                    sendUnauthorized(response, "Invalid or expired token!");
                    return;
                }

                String sessionId =
                        jwtTokenProvider.getSessionIdFromToken(authToken);

                var cached = sessionValidationService.validate(sessionId);

                SecurityContextHolder.getContext().setAuthentication(
                        getAuthentication(
                                cached.getUserId(),
                                cached.getUserUuid(),
                                cached.getEmail(),
                                cached.getName(),
                                cached.isAdmin(),
                                cached.getAuthorities()
                        )
                );

            } catch (Exception e) {
                log.warn("AUTH_FAILURE reason={}", e.getMessage());
                authHelper.clearAuthCookie(response);
                sendUnauthorized(response, e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return Arrays.stream(Urls.Security.FILTER_EXCLUDED_ENDPOINTS)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
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
        response.getWriter().write(objectMapper.writeValueAsString(res));
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

        var grantedAuthorities =
                authorities.stream()
                        .map(a -> (GrantedAuthority) () -> a)
                        .toList();

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                grantedAuthorities
        );
    }
}
