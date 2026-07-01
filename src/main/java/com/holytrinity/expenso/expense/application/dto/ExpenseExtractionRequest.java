package com.holytrinity.expenso.expense.application.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class ExpenseExtractionRequest {
    private MultipartFile file;
    private String text;
    @NotBlank(message = "expenseId is required")
    private String expenseId;
}
