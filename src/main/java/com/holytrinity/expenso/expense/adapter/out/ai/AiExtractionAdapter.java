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

    @Override
    public void submitExpenseForExtraction(AiExtractionRequest request) {
        RestClient restClient = restClientBuilder.baseUrl(baseUrl).build();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("user_id", request.getUserId());

        if (request.getWebhookUrl() != null) {
            body.add("webhook_url", request.getWebhookUrl());
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

        log.info("Submitting expense extraction to AI for user {}, webhook: {}", request.getUserId(),
                request.getWebhookUrl());

        restClient.post()
                .uri("/api/v1/expense/extract")
                .header("X-Internal-Token", internalToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(String.class); // Read string body
    }
}
