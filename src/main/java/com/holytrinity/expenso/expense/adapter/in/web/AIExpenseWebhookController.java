package com.holytrinity.expenso.expense.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.holytrinity.expenso.expense.application.port.in.ExpenseUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhook/expense-ai")
@RequiredArgsConstructor
@Slf4j
public class AIExpenseWebhookController {

    private final ExpenseUseCase expenseUseCase;

    @org.springframework.beans.factory.annotation.Value("${app.webhook.secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-Webhook-Secret", required = false) String secret,
            @RequestBody JsonNode payload) {
        if (secret == null || !java.security.MessageDigest.isEqual(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), webhookSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            log.warn("Unauthorized webhook attempt");
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Incoming AI Webhook payload received");
        expenseUseCase.handleExtractionCallback(payload);
        return ResponseEntity.ok().build();
    }
}
