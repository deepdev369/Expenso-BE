package com.holytrinity.expenso.expense.adapter.out.persistence;

import com.holytrinity.expenso.expense.domain.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataExpenseRepository extends JpaRepository<Expense, String> {
    boolean existsByUserUserId(String userId);
    /** Spring Data derives the query: SELECT e FROM Expense e WHERE e.user.userId = :userId AND e.deleted = false */
    Page<Expense> findByUserUserIdAndDeletedFalse(String userId, Pageable pageable);
}
