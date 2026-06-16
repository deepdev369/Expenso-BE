package com.holytrinity.expenso.expense.adapter.out.persistence;

import com.holytrinity.expenso.expense.domain.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataExpenseRepository extends JpaRepository<Expense, String> {
    boolean existsByUserUserId(String userId);
    /** Spring Data derives the query: SELECT e FROM Expense e WHERE e.user.userId = :userId AND e.deleted = false */
    Page<Expense> findByUserUserIdAndDeletedFalse(String userId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM expense WHERE expense_id IN (:ids) AND user_id = :userId", nativeQuery = true)
    java.util.List<Expense> findAllWithDeletedByIdsAndUserId(@org.springframework.data.repository.query.Param("ids") java.util.List<String> ids, @org.springframework.data.repository.query.Param("userId") String userId);
}
