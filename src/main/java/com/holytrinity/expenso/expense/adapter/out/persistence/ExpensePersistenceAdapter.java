package com.holytrinity.expenso.expense.adapter.out.persistence;

import com.holytrinity.expenso.expense.application.port.out.ExpensePort;
import com.holytrinity.expenso.expense.domain.Expense;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpensePersistenceAdapter implements ExpensePort {

    private final SpringDataExpenseRepository expenseRepository;

    @Override
    public Optional<Expense> loadExpense(String expenseId) {
        return expenseRepository.findById(expenseId);
    }

    @Override
    public List<Expense> loadAllExpenses() {
        return expenseRepository.findAll();
    }

    @Override
    public boolean existsByUserId(String userId) {
        return expenseRepository.existsByUserUserId(userId);
    }

    @Override
    public Expense saveExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Override
    public void deleteExpense(Expense expense) {
        expenseRepository.delete(expense);
    }

    @Override
    public org.springframework.data.domain.Page<Expense> findAll(org.springframework.data.domain.Pageable pageable) {
        return expenseRepository.findAll(pageable);
    }

    @Override
    public org.springframework.data.domain.Page<Expense> findAllByUserId(
            String userId, org.springframework.data.domain.Pageable pageable) {
        return expenseRepository.findByUserUserIdAndDeletedFalse(userId, pageable);
    }

    @Override
    public List<Expense> findAllWithDeletedByIdsAndUserId(List<String> ids, String userId) {
        return expenseRepository.findAllWithDeletedByIdsAndUserId(ids, userId);
    }
}
