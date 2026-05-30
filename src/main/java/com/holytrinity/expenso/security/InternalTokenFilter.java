package com.holytrinity.expenso.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Validates the X-Internal-Token header for all internal webhook paths.
 * Uses constant-time comparison to prevent timing-based oracle attacks.
 *
 * Covers: /api/v1/webhook/** (all current and future webhook endpoints)
 */
@Component
@RequiredArgsConstructor
public class InternalTokenFilter extends OncePerRequestFilter {

    @Value("${ai.service.internal-token}")
    private String configuredToken;

    private static final String WEBHOOK_PATH_PREFIX = "/api/v1/webhook/";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith(WEBHOOK_PATH_PREFIX)) {
            String token = request.getHeader("X-Internal-Token");
            if (token == null || !constantTimeEquals(token, configuredToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or missing X-Internal-Token");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    /** Constant-time string comparison — prevents timing oracle attacks on the token. */
    private boolean constantTimeEquals(String a, String b) {
        return java.security.MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }
}
