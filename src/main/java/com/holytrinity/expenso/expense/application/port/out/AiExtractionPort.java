package com.holytrinity.expenso.expense.application.port.out;

import com.holytrinity.expenso.expense.application.port.out.dto.AiExtractionRequest;

import com.fasterxml.jackson.databind.JsonNode;

public interface AiExtractionPort {
    JsonNode submitExpenseForExtraction(AiExtractionRequest request);
}
