package com.holytrinity.expenso.expense.application.port.out.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AiExtractionRequest {
    private String userId;
    private String expenseId;
    private String rawText;
    private MultipartFile file;
    private String currency;
    private String country;
    private String userLanguage;
    private Map<String, List<String>> categoriesMapping;
    private List<String> paymentMethods;
    private String webhookUrl;
}
