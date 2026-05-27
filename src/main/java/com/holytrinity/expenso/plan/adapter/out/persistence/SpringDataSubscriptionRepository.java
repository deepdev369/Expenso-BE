package com.holytrinity.expenso.plan.adapter.out.persistence;

import com.holytrinity.expenso.plan.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataSubscriptionRepository extends JpaRepository<Subscription, String> {
    List<Subscription> findByUser_UserId(String userId);
}
