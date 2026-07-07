package com.holytrinity.expenso.split.application.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.Size;

@Data
public class SplitExtractionRequest {
    @Size(max = 2000)
    private String rawText;
    
    @Size(max = 3)
    private String currency;
    
    private String friendsJson;
    
    private MultipartFile file;
}
