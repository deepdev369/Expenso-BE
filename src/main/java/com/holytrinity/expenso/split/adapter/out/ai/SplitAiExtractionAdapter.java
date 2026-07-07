package com.holytrinity.expenso.split.adapter.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.holytrinity.expenso.split.application.port.out.SplitAiExtractionPort;
import com.holytrinity.expenso.split.application.port.out.dto.SplitAiExtractionRequest;
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
public class SplitAiExtractionAdapter implements SplitAiExtractionPort {

    @Value("${ai.service.base-url}")
    private String baseUrl;

    @Value("${ai.service.internal-token}")
    private String internalToken;

    private final ObjectMapper objectMapper;
    private final RestClient.Builder restClientBuilder;

    @Override
    public JsonNode submitSplitForExtraction(SplitAiExtractionRequest request) {
        RestClient restClient = restClientBuilder.baseUrl(baseUrl).build();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("user_id", request.getUserId());

        if (request.getRawText() != null) {
            body.add("raw_text", request.getRawText());
        }
        if (request.getCurrency() != null) {
            body.add("currency", request.getCurrency());
        }

        try {
            if (request.getFriends() != null) {
                body.add("friends_json", objectMapper.writeValueAsString(request.getFriends()));
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
            log.error("Error processing Split AI request body", e);
            throw new RuntimeException("Failed to prepare multipart data for Split AI Service", e);
        }

        log.info("Submitting split extraction to AI synchronously for user {}", request.getUserId());

        try {
            String responseBody = restClient.post()
                    .uri("/api/v1/split/extract-split")
                    .header("X-Internal-Token", internalToken)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            
            JsonNode initialResponse = objectMapper.readTree(responseBody);
            
            String status = initialResponse.path("data").path("status").asText("");
            if ("FAILED".equals(status)) {
                log.error("AI Service failed the split extraction: {}", initialResponse.path("data").path("error").asText(""));
            } else if ("COMPLETED".equals(status)) {
                log.info("AI Service finished split processing synchronously.");
            }
            
            return initialResponse.path("data");
        } catch (Exception e) {
            log.error("Failed to submit request to Split AI Service", e);
            throw new RuntimeException("HTTP request to Split AI service failed: " + e.getMessage(), e);
        }
    }
}
