package com.holytrinity.expenso.split.application.port.out;

import com.fasterxml.jackson.databind.JsonNode;
import com.holytrinity.expenso.split.application.port.out.dto.SplitAiExtractionRequest;

public interface SplitAiExtractionPort {
    JsonNode submitSplitForExtraction(SplitAiExtractionRequest request);
}
