package com.holytrinity.expenso.split.application.port.out.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class SplitAiExtractionRequest {
    private String userId;
    private String rawText;
    private String currency;
    private List<Map<String, String>> friends;
    private MultipartFile file;
}
