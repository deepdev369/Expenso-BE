package com.holytrinity.expenso.expense.application.port.in;

import com.holytrinity.expenso.expense.application.dto.ExpenseDTO;
import java.util.List;

public interface ExpenseUseCase {
    ExpenseDTO get(String expenseId);

    List<ExpenseDTO> findAllByUserEmail(String email);

    org.springframework.data.domain.Page<ExpenseDTO> findAll(org.springframework.data.domain.Pageable pageable);

    List<ExpenseDTO> processBulk(List<ExpenseDTO> expenseDTOs);

    void deleteBulk(List<String> expenseIds);

    void submitForExtraction(org.springframework.web.multipart.MultipartFile file, String text, String expenseId);

    void handleExtractionCallback(com.fasterxml.jackson.databind.JsonNode payload);

}
