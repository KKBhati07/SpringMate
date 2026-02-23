package com.example.SpringMate.Filter;

import com.example.SpringMate.Config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private final AppProperties appProperties;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        var security = appProperties.getSecurity();
        if (security != null && security.getHeaders() != null) {
            var headers = security.getHeaders();

            // HSTS
            if (headers.getHsts() != null && headers.getHsts().isEnabled()) {
                String value = "max-age=" + headers.getHsts().getMaxAgeSeconds();
                if (headers.getHsts().isIncludeSubdomains()) {
                    value += "; includeSubDomains";
                }
                response.setHeader("Strict-Transport-Security", value);
            }

            // Clickjacking protection
            if (headers.getFrameOptions() != null) {
                response.setHeader("X-Frame-Options", headers.getFrameOptions());
            }

            // MIME sniffing protection
            if (headers.isContentTypeOptions()) {
                response.setHeader("X-Content-Type-Options", "nosniff");
            }

            // Legacy XSS filter
            if (headers.isXssProtection()) {
                response.setHeader("X-XSS-Protection", "1; mode=block");
            }

            // Referrer policy
            if (headers.getReferrerPolicy() != null) {
                response.setHeader("Referrer-Policy", headers.getReferrerPolicy());
            }

            // Content Security Policy (Primary XSS protection)
            response.setHeader(
                    "Content-Security-Policy",
                    headers.getCsp() != null
                            ? headers.getCsp()
                            : "default-src 'self'; frame-ancestors 'none';"
            );
        }

        filterChain.doFilter(request, response);
    }
}
