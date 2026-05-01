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
    public Optional<Expense> loadExpense(Long expenseId) {
        return expenseRepository.findById(expenseId);
    }

    @Override
    public Optional<Expense> loadExpenseByClientReferenceId(String clientReferenceId) {
        return expenseRepository.findByClientReferenceId(clientReferenceId);
    }

    @Override
    public List<Expense> loadAllExpenses() {
        return expenseRepository.findAll();
    }

    @Override
    public Expense findFirstByUserId(Long userId) {
        return expenseRepository.findFirstByUserUserId(userId);
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
    public List<Expense> findAllByUserEmail(String email) {
        return expenseRepository.findAllByUserEmail(email);
    }

    @Override
    public org.springframework.data.domain.Page<Expense> findAllByUserUserId(Long userId,
            org.springframework.data.domain.Pageable pageable) {
        return expenseRepository.findAllByUserUserId(userId, pageable);
    }
}
