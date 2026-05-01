package com.holytrinity.expenso.user.adapter.out.persistence;

import com.holytrinity.expenso.user.application.port.out.AssociatedUserPort;
import com.holytrinity.expenso.user.domain.AssociatedUser;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssociatedUserPersistenceAdapter implements AssociatedUserPort {

    private final SpringDataAssociatedUserRepository repository;

    @Override
    public Optional<AssociatedUser> loadAssociatedUserByClientReferenceId(String clientReferenceId) {
        return repository.findByClientReferenceId(clientReferenceId);
    }

    @Override
    public List<AssociatedUser> loadAllByUserId(Long userId) {
        return repository.findAllByUserUserId(userId);
    }

    @Override
    public AssociatedUser saveAssociatedUser(AssociatedUser associatedUser) {
        return repository.save(associatedUser);
    }

    @Override
    public void deleteAssociatedUser(AssociatedUser associatedUser) {
        repository.delete(associatedUser);
    }
}
