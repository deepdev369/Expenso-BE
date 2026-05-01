package com.holytrinity.expenso.expense.adapter.out.persistence;

import com.holytrinity.expenso.expense.domain.Expense;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataExpenseRepository extends JpaRepository<Expense, String> {
    Expense findFirstByUserUserId(String userId);

    List<Expense> findAllByUserEmail(String email);
}
