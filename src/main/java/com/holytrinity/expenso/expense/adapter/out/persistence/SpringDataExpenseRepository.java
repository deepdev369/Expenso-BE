package com.holytrinity.expenso.expense.adapter.out.persistence;

import com.holytrinity.expenso.expense.domain.Expense;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataExpenseRepository extends JpaRepository<Expense, String> {
    boolean existsByUserUserId(String userId);
}
