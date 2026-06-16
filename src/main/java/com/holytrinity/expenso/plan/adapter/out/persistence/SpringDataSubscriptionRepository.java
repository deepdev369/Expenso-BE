package com.holytrinity.expenso.plan.adapter.out.persistence;

import com.holytrinity.expenso.plan.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataSubscriptionRepository extends JpaRepository<Subscription, String> {
    List<Subscription> findByUser_UserId(String userId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM subscriptions WHERE subscription_id IN (:ids) AND user_id = :userId", nativeQuery = true)
    List<Subscription> findAllWithDeletedByIdsAndUserId(@org.springframework.data.repository.query.Param("ids") List<String> ids, @org.springframework.data.repository.query.Param("userId") String userId);
}
