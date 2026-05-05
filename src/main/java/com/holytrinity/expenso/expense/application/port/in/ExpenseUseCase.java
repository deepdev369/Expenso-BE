package com.holytrinity.expenso.expense.application.port.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.holytrinity.expenso.expense.application.dto.ExpenseDTO;
import java.util.List;

public interface ExpenseUseCase {
    ExpenseDTO get(String expenseId);

    ExpenseDTO update(String expenseId, ExpenseDTO expenseDTO);

    void delete(String expenseId);

    Page<ExpenseDTO> findAll(Pageable pageable);

    List<ExpenseDTO> processBulk(List<ExpenseDTO> expenseDTOs);

    void deleteBulk(List<String> expenseIds);

    void submitForExtraction(MultipartFile file, String text, String expenseId);

    void handleExtractionCallback(JsonNode payload);

}
