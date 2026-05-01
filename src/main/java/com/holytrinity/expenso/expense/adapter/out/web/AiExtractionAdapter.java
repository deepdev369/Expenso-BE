package com.holytrinity.expenso.expense.adapter.out.web;

import com.holytrinity.expenso.expense.application.port.out.AiExtractionPort;
import com.holytrinity.expenso.expense.application.port.out.dto.AiExtractionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiExtractionAdapter implements AiExtractionPort {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.microservice.url:http://localhost:8000/api/v1/extract}")
    private String aiMicroserviceUrl;

    @Override
    public void submitExpenseForExtraction(AiExtractionRequest request) {
        log.info("Queueing extraction request to AI Microservice for User: {}", request.getUserId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("user_id", request.getUserId());
        if (request.getClientReferenceId() != null) {
            body.add("clientReferenceId", request.getClientReferenceId());
        }
        if (request.getWebhookUrl() != null) {
            body.add("webhook_url", request.getWebhookUrl());
        }
        
        if (request.getRawText() != null) {
            body.add("text", request.getRawText());
        }
        if (request.getFile() != null) {
            body.add("file", request.getFile().getResource());
        }
        if (request.getCurrency() != null) {
            body.add("currency", request.getCurrency());
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
        } catch (Exception e) {
            log.error("Failed to serialize collections for AI request: {}", e.getMessage());
        }
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        // Execute asynchronously so the Controller can return 202 instantly
        CompletableFuture.runAsync(() -> {
            try {
                log.info("Executing async POST to {}", aiMicroserviceUrl);
                restTemplate.postForEntity(aiMicroserviceUrl, requestEntity, String.class);
                log.info("Successfully handed off extraction request to AI Microservice.");
            } catch (Exception e) {
                log.error("Failed to submit request to AI Microservice: {}", e.getMessage());
            }
        });
    }
}
