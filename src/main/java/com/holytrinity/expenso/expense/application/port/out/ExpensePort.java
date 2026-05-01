package com.holytrinity.expenso.expense.application.port.out;

import com.holytrinity.expenso.expense.domain.Expense;
import java.util.List;
import java.util.Optional;

public interface ExpensePort {
    Optional<Expense> loadExpense(String expenseId);

    List<Expense> loadAllExpenses();

    org.springframework.data.domain.Page<Expense> findAll(org.springframework.data.domain.Pageable pageable);

    Expense findFirstByUserId(String userId);

    Expense saveExpense(Expense expense);

    void deleteExpense(Expense expense);

    List<Expense> findAllByUserEmail(String email);
}
