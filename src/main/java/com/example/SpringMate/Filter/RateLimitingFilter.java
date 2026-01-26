package com.example.SpringMate.Filter;

import com.example.SpringMate.Shared.Urls;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to apply rate limiting to API endpoints using Resilience4j RateLimiter.
 */
@Slf4j
@Component
@Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE + 2)
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterRegistry rateLimiterRegistry;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String rateLimiterName = determineRateLimiter(path);

        try {
            RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterName);
            rateLimiter.acquirePermission();

            filterChain.doFilter(request, response);
        } catch (RequestNotPermitted e) {
            log.warn("RATE_LIMIT_EXCEEDED path={} limiter={} client={}",
                    path, rateLimiterName, getClientIdentifier(request));

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    String.format(
                            "{\"success\":false,\"message\":\"Rate limit exceeded. Please try again later.\",\"error_code\":\"RATE_LIMIT_EXCEEDED\"}"
                    )
            );
        }
    }

    /**
     * Determines which rate limiter to use based on the request path.
     */
    private String determineRateLimiter(String path) {
        if (path.startsWith(Urls.Auth.BASE)) {
            return "auth";
        }

        if (path.startsWith(Urls.Category.BASE) ||
            path.startsWith(Urls.Location.BASE) ||
            (path.startsWith(Urls.Listing.BASE) && path.contains("/get_all"))) {
            return "public";
        }

        return "api";
    }

    /**
     * Gets a client identifier for logging purposes.
     * Uses IP address or user identifier if available.
     */
    private String getClientIdentifier(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}