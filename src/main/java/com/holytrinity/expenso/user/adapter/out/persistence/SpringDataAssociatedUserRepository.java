package com.holytrinity.expenso.user.adapter.out.persistence;

import com.holytrinity.expenso.user.domain.AssociatedUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAssociatedUserRepository extends JpaRepository<AssociatedUser, String> {
}
