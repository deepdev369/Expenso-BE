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

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody JsonNode payload) {
        log.info("Incoming AI Webhook payload received");
        expenseUseCase.handleExtractionCallback(payload);
        return ResponseEntity.ok().build();
    }
}
