package com.holytrinity.expenso.user.adapter.out.persistence;

import com.holytrinity.expenso.user.domain.AssociatedUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAssociatedUserRepository extends JpaRepository<AssociatedUser, Long> {
    Optional<AssociatedUser> findByClientReferenceId(String clientReferenceId);

    List<AssociatedUser> findAllByUserUserId(Long userId);
}
