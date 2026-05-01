package com.holytrinity.expenso.expense.application.port.out;

import com.holytrinity.expenso.expense.application.port.out.dto.AiExtractionRequest;

public interface AiExtractionPort {
    void submitExpenseForExtraction(AiExtractionRequest request);
}
