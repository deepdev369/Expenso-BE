package com.holytrinity.expenso.expense.adapter.out.persistence;

import com.holytrinity.expenso.expense.domain.Expense;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataExpenseRepository extends JpaRepository<Expense, Long> {
    Expense findFirstByUserUserId(Long userId);

    List<Expense> findAllByUserUserId(Long userId);

    List<Expense> findAllByUserEmail(String email);

    org.springframework.data.domain.Page<Expense> findAllByUserUserId(Long userId,
            org.springframework.data.domain.Pageable pageable);
}
