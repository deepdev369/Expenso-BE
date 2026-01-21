package com.holytrinity.expenso.expense.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenseDTO {

    private Long expenseId;

    private Long version;

    @NotNull
    private Double amount;

    @NotNull
    @Size(max = 255)
    private String category;

    @Size(max = 255)
    private String subCategory;

    private List<@Size(max = 255) String> tags;

    @Size(max = 255)
    private String paymentMode;

    @Size(max = 255)
    private String transactionType;

    @Size(max = 255)
    private String merchantName;

    @Size(max = 255)
    private String source;

    private Boolean userConfirmed;

    @Size(max = 255)
    private String rawText;

    @Size(max = 255)
    private String normalizedText;

    @Size(max = 255)
    private String status;

    private Long expenseDate;

    @NotNull
    private Long userID;

}
