package com.holytrinity.expenso.shared.idempotency;

import com.holytrinity.expenso.security.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final UserContext userContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // Skip webhooks and GET requests (GET is naturally idempotent)
        if (uri.startsWith("/api/v1/webhook/") || "GET".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getHeader("Idempotency-Key");
        if (key == null || key.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = userContext.getCurrentUserIdOrNull();
        if (userId == null) {
            // Unauthenticated request (e.g., login/register), skip idempotency
            filterChain.doFilter(request, response);
            return;
        }

        Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findByIdempotencyKeyAndUserId(key, userId);
        if (existingKey.isPresent()) {
            IdempotencyKey stored = existingKey.get();
            if (stored.getExpiresAt().isAfter(LocalDateTime.now())) {
                log.info("Idempotency key hit for user {}: {}", userId, key);
                response.setStatus(stored.getResponseStatus());
                response.setContentType("application/json");
                if (stored.getResponseBody() != null) {
                    response.getWriter().write(stored.getResponseBody());
                }
                return;
            } else {
                idempotencyKeyRepository.delete(stored);
            }
        }

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);

        int status = responseWrapper.getStatus();
        
        // Only cache successful responses (2xx). 
        // We don't cache 4xx or 5xx so the client can safely retry if needed.
        if (status >= 200 && status < 300) {
            String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);

            IdempotencyKey newKey = new IdempotencyKey(
                    UUID.randomUUID().toString(),
                    userId,
                    key,
                    status,
                    responseBody,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusHours(24));
            idempotencyKeyRepository.save(newKey);
        }

        responseWrapper.copyBodyToResponse();
    }
}
