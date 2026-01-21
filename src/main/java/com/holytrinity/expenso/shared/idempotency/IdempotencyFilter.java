package com.holytrinity.expenso.shared.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String key = request.getHeader("Idempotency-Key");
        if (key == null || key.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findById(key);
        if (existingKey.isPresent()) {
            IdempotencyKey stored = existingKey.get();
            if (stored.getExpiresAt().isAfter(LocalDateTime.now())) {
                log.info("Idempotency key hit: {}", key);
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

        // Only save successful or client error responses? Or all? User said "Prevent
        // duplicate processing".
        // Generally we cache 2xx, maybe 4xx.
        // Let's cache everything for simplicity as per requirement.

        String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);

        IdempotencyKey newKey = new IdempotencyKey(
                key,
                responseWrapper.getStatus(),
                responseBody,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(24));
        idempotencyKeyRepository.save(newKey);

        responseWrapper.copyBodyToResponse();
    }
}
