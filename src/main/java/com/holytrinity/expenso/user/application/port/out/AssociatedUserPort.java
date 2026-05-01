package com.holytrinity.expenso.user.application.port.out;

import com.holytrinity.expenso.user.domain.AssociatedUser;
import java.util.List;
import java.util.Optional;

public interface AssociatedUserPort {
    Optional<AssociatedUser> loadAssociatedUserByClientReferenceId(String clientReferenceId);

    List<AssociatedUser> loadAllByUserId(Long userId);

    AssociatedUser saveAssociatedUser(AssociatedUser associatedUser);

    void deleteAssociatedUser(AssociatedUser associatedUser);
}
