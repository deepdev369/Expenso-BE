package com.holytrinity.expenso.user.application.port.out;

import com.holytrinity.expenso.user.domain.AssociatedUser;
import java.util.Optional;

public interface AssociatedUserPort {
    Optional<AssociatedUser> loadAssociatedUser(String associatedUserId);

    org.springframework.data.domain.Page<AssociatedUser> loadAll(org.springframework.data.domain.Pageable pageable);

    AssociatedUser saveAssociatedUser(AssociatedUser associatedUser);

    void deleteAssociatedUser(AssociatedUser associatedUser);
}
