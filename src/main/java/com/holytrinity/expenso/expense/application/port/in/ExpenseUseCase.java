package com.holytrinity.expenso.expense.application.port.in;

import com.holytrinity.expenso.expense.application.dto.ExpenseDTO;
import java.util.List;

public interface ExpenseUseCase {
    ExpenseDTO create(ExpenseDTO expenseDTO);

    ExpenseDTO get(Long expenseId);

    List<ExpenseDTO> findAllByUserEmail(String email);

    org.springframework.data.domain.Page<ExpenseDTO> findAll(org.springframework.data.domain.Pageable pageable);

    ExpenseDTO update(Long expenseId, ExpenseDTO expenseDTO);

    void delete(Long expenseId);

    List<ExpenseDTO> processBulk(List<ExpenseDTO> expenseDTOs);

    void deleteBulk(List<Long> expenseIds);

}
