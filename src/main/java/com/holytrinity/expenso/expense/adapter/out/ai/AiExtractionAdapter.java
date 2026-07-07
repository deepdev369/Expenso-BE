package com.holytrinity.expenso.expense.adapter.out.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.holytrinity.expenso.expense.application.port.out.AiExtractionPort;
import com.holytrinity.expenso.expense.application.port.out.dto.AiExtractionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.holytrinity.expenso.expense.application.port.in.ExpenseUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.beans.factory.annotation.Qualifier;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiExtractionAdapter implements AiExtractionPort {

    @Value("${ai.service.base-url}")
    private String baseUrl;

    @Value("${ai.service.internal-token}")
    private String internalToken;

    private final ObjectMapper objectMapper;
    private final RestClient.Builder restClientBuilder;

    @Autowired
    @Lazy
    private ExpenseUseCase expenseUseCase;

    @Autowired
    @Qualifier("applicationTaskExecutor")
    private TaskExecutor taskExecutor;

    private final Semaphore extractionSemaphore = new Semaphore(10, true);

    @Override
    public com.fasterxml.jackson.databind.JsonNode submitExpenseForExtraction(AiExtractionRequest request) {
        RestClient restClient = restClientBuilder.baseUrl(baseUrl).build();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("user_id", request.getUserId());
        if (request.getExpenseId() != null) {
            body.add("expense_id", request.getExpenseId());
        }

        if (request.getRawText() != null) {
            body.add("raw_text", request.getRawText());
        }
        if (request.getCurrency() != null) {
            body.add("currency", request.getCurrency());
        }
        if (request.getCountry() != null) {
            body.add("country", request.getCountry());
        }
        if (request.getUserLanguage() != null) {
            body.add("user_language", request.getUserLanguage());
        }

        try {
            if (request.getCategoriesMapping() != null) {
                body.add("categories_mapping_json", objectMapper.writeValueAsString(request.getCategoriesMapping()));
            }
            if (request.getPaymentMethods() != null) {
                body.add("payment_methods_json", objectMapper.writeValueAsString(request.getPaymentMethods()));
            }
            if (request.getFile() != null && !request.getFile().isEmpty()) {
                ByteArrayResource fileResource = new ByteArrayResource(request.getFile().getBytes()) {
                    @Override
                    public String getFilename() {
                        return request.getFile().getOriginalFilename() != null ? request.getFile().getOriginalFilename()
                                : "upload.file";
                    }
                };
                body.add("file", fileResource);
            }
        } catch (IOException e) {
            log.error("Error processing AI request body", e);
            throw new RuntimeException("Failed to prepare multipart data for AI Service", e);
        }

        log.info("Submitting expense extraction to AI synchronously for user {}", request.getUserId());

        try {
            boolean acquired = extractionSemaphore.tryAcquire(60, TimeUnit.SECONDS);
            if (!acquired) {
                throw new RuntimeException("Server is currently busy processing other extractions. Please try again later.");
            }
            
            try {
                String responseBody = restClient.post()
                        .uri("/api/v1/expense/extract")
                        .header("X-Internal-Token", internalToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(body)
                        .retrieve()
                        .body(String.class);
                com.fasterxml.jackson.databind.JsonNode initialResponse = objectMapper.readTree(responseBody);
                
                String status = initialResponse.path("data").path("status").asText("");
                if ("FAILED".equals(status)) {
                    log.error("AI Service failed the extraction: {}", initialResponse.path("data").path("error").asText(""));
                } else if ("COMPLETED".equals(status)) {
                    log.info("AI Service finished processing synchronously.");
                }
                
                return initialResponse.path("data");
            } finally {
                extractionSemaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting to process AI extraction", e);
        } catch (Exception e) {
            log.error("Failed to submit request to AI Service", e);
            throw new RuntimeException("HTTP request to AI service failed: " + e.getMessage(), e);
        }
    }
}
