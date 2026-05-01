package com.holytrinity.expenso.expense.application.port.out;

import com.holytrinity.expenso.expense.domain.Expense;
import java.util.List;
import java.util.Optional;

public interface ExpensePort {
    Optional<Expense> loadExpense(Long expenseId);

    Optional<Expense> loadExpenseByClientReferenceId(String clientReferenceId);

    List<Expense> loadAllExpenses();

    Expense findFirstByUserId(Long userId);

    Expense saveExpense(Expense expense);

    void deleteExpense(Expense expense);

    List<Expense> findAllByUserEmail(String email);

    org.springframework.data.domain.Page<Expense> findAllByUserUserId(Long userId,
            org.springframework.data.domain.Pageable pageable);
}
